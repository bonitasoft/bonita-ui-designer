/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.any;
import static java.lang.String.format;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_REMOVAL;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.ResponseHeadersHelper.getMovedResourceResponse;
import static org.bonitasoft.web.designer.repository.exception.NotAllowedException.checkNotAllowed;
import static org.jsoup.helper.StringUtil.isBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.FragmentChangeVisitor;
import org.bonitasoft.web.designer.visitor.PageHasValidationErrorVisitor;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import org.bonitasoft.web.designer.controller.asset.PageAssetPredicate;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AbstractRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/fragments")
public class FragmentResource {

    private final FragmentService fragmentService;
    private FragmentChangeVisitor fragmentChangeVisitor;
    private FragmentRepository fragmentRepository;
    private JacksonObjectMapper objectMapper;
    private SimpMessagingTemplate messagingTemplate;
    private AssetVisitor assetVisitor;
    private List<Repository> usedByRepositories;
    private PageRepository pageRepository;
    private PageHasValidationErrorVisitor pageHasValidationErrorVisitor;

    @Inject
    public FragmentResource(FragmentRepository fragmentRepository, FragmentService fragmentService, JacksonObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate, AssetVisitor assetVisitor, PageRepository pageRepository, FragmentChangeVisitor fragmentChangeVisitor, PageHasValidationErrorVisitor pageHasValidationErrorVisitor) {
        this.fragmentRepository = fragmentRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.assetVisitor = assetVisitor;
        this.pageRepository = pageRepository;
        this.fragmentService = fragmentService;
        this.fragmentChangeVisitor = fragmentChangeVisitor;
        this.pageHasValidationErrorVisitor = pageHasValidationErrorVisitor;
    }

    /**
     * List cannot be injected in constructor with @Inject so we use setter and @Resource to inject them
     */
    @Resource(name = "fragmentsUsedByRepositories")
    public void setUsedByRepositories(List<Repository> usedByRepositories) {
        this.usedByRepositories = usedByRepositories;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Fragment> create(@RequestBody Fragment newFragment) throws RepositoryException {

        checkNameIsUnique(fragmentRepository.getAll(), newFragment);

        newFragment.setId(fragmentRepository.getNextAvailableId(newFragment.getName()));
        newFragment.setAssets(Sets.filter(newFragment.getAssets(), new PageAssetPredicate()));
        fragmentRepository.updateLastUpdateAndSave(newFragment);
        return new ResponseEntity<>(newFragment, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{fragmentId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> save(HttpServletRequest request, @PathVariable("fragmentId") String fragmentId, @RequestBody Fragment newFragment) throws RepositoryException {

        checkNameIsUnique(
                // make sure we don't check against the fragment itself
                filter(fragmentRepository.getAll(), not(Predicates.propertyEqualTo("id", newFragment.getId()))),
                newFragment);

        String newFragmentId;
        try {
            Fragment currentFragment = fragmentService.get(fragmentId);
            Optional<ResponseEntity<Object>> objectResponseEntity = checkIfFragmentIsCompatible(currentFragment);
            if (objectResponseEntity.isPresent()) {
                ResponseEntity response = objectResponseEntity.get();
                return response;
            }

            if (currentFragment.getHasValidationError() != newFragment.getHasValidationError() || !currentFragment.getName().equals(newFragment.getName())) {
                if (!currentFragment.getName().equals(newFragment.getName())) {
                    newFragmentId = fragmentRepository.getNextAvailableId(newFragment.getName());
                } else {
                    newFragmentId = fragmentId;
                }
                updateReferencesParentArtifacts(currentFragment, newFragmentId, newFragment.getHasValidationError());
            } else {
                newFragmentId = fragmentId;
            }

        } catch (NotFoundException e) {
            newFragmentId = fragmentRepository.getNextAvailableId(newFragment.getName());
        }

        newFragment.setId(newFragmentId);
        newFragment.setAssets(Sets.filter(newFragment.getAssets(), new PageAssetPredicate()));
        fragmentRepository.updateLastUpdateAndSave(newFragment);
        ResponseEntity<Void> responseEntity;
        if (!newFragmentId.equals(fragmentId)) {
            // Rename link to Fragment in other artifact
            fragmentRepository.delete(fragmentId);
            responseEntity = getMovedResourceResponse(request, newFragmentId);
            messagingTemplate.convertAndSend(PREVIEWABLE_REMOVAL, fragmentId);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
            messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, fragmentId);
        }
        return responseEntity;
    }


    private void updateReferencesParentArtifacts(Fragment currentFragment, String newFragmentId, boolean newHasValidationError) {
        fillWithUsedBy(currentFragment);
        if (currentFragment.getUsedBy() == null) {
            return;
        }
        String oldFragmentId = currentFragment.getId();

        for (Map.Entry<String, List<Identifiable>> entry : currentFragment.getUsedBy().entrySet()) {
            List<? extends Identifiable> identifiables = entry.getValue();

            List<? extends Identifiable> pages = identifiables
                    .stream()
                    .filter(identifiable -> identifiable instanceof Page)
                    .collect(Collectors.toList());
            updateReference(pages, newFragmentId, oldFragmentId, pageRepository, newHasValidationError);

            List<? extends Identifiable> fragments = identifiables
                    .stream()
                    .filter(identifiable -> identifiable.getType().equals("fragment"))
                    .collect(Collectors.toList());

            updateReference(fragments, newFragmentId, oldFragmentId, fragmentRepository, newHasValidationError);
        }
    }

    private <T extends AbstractPage> void updateReference(List<? extends Identifiable> artifacts, String newFragmentId, String oldFragmentId, AbstractRepository repo, boolean newHasValidationError) {

        fragmentChangeVisitor.setNewFragmentId(newFragmentId);
        fragmentChangeVisitor.setFragmentToReplace(oldFragmentId);
        fragmentChangeVisitor.setNewHasValidationError(newHasValidationError);

        // set the has valid error
        artifacts.forEach(identifiable -> {
            AbstractPage artifact = (T) repo.get(identifiable.getId());

            if (newHasValidationError != artifact.getHasValidationError()) {
                updateArtifactValidationError(artifact, newHasValidationError, newFragmentId);
                if (artifact.getClass().equals(Fragment.class)) {
                    updateReferencesParentArtifacts((Fragment) artifact, artifact.getId(), newHasValidationError);
                }
            }

            // Traverse tree to detect fragment
            List<List<Element>> rows = artifact.getRows();
            fragmentChangeVisitor.visitRows(rows);

            repo.updateLastUpdateAndSave(artifact);
        });
    }

    private void updateArtifactValidationError(AbstractPage artifact, boolean newHasValidationError, String newFragmentId) {
        if (newHasValidationError) {
            artifact.setHasValidationError(newHasValidationError);
        } else {
            if (!containsValidationError(artifact, newFragmentId)) {
                artifact.setHasValidationError(newHasValidationError);
            }
        }
    }

    private boolean containsValidationError(AbstractPage page, String newFragmentId) {
        boolean[] hasValidationError = new boolean[]{false};

        List<List<Element>> rows = page.getRows();

        rows.stream().forEach(row -> {
            List<Element> elements = row;
            elements.stream()
                    .forEach(element -> {
                        if (element.getClass().equals(FragmentElement.class)) {
                            if (!((FragmentElement) element).getId().equals(newFragmentId)) {
                                hasValidationError[0] = hasValidationError[0] || element.getHasValidationError();
                            }
                        } else {
                            hasValidationError[0] = hasValidationError[0] || pageHasValidationErrorVisitor.visit((Component) element);
                        }
                    });
        });
        return hasValidationError[0];
    }

    private Optional<ResponseEntity<Object>> checkIfFragmentIsCompatible(Fragment fragment) {
        if (fragment.getStatus() != null && !fragment.getStatus().isCompatible()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return Optional.of(new ResponseEntity(String.format("Fragment %s is in an incompatible version. Newer UI Designer version is required.", fragment.getId()), headers, HttpStatus.UNPROCESSABLE_ENTITY));
        }
        return Optional.empty();
    }

    @RequestMapping(value = "/{fragmentId}/name", method = RequestMethod.PUT)
    public ResponseEntity<Void> rename(HttpServletRequest request, @PathVariable("fragmentId") String fragmentId,
                                       @RequestBody String name) throws RepositoryException {
        Fragment fragment = fragmentService.get(fragmentId);
        Optional<ResponseEntity<Object>> objectResponseEntity = checkIfFragmentIsCompatible(fragment);
        if (objectResponseEntity.isPresent()) {
            ResponseEntity response = objectResponseEntity.get();
            return response;
        }
        ResponseEntity<Void> responseEntity;
        if (!name.equals(fragment.getName())) {
            responseEntity = renameExistingFragment(request, fragmentId, name, fragment);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        }
        return responseEntity;
    }

    private ResponseEntity<Void> renameExistingFragment(HttpServletRequest request, String fragmentId, String name,
                                                        Fragment fragment) {
        ResponseEntity<Void> responseEntity;
        String newFragmentId = fragmentRepository.getNextAvailableId(name);
        updateReferencesParentArtifacts(fragment, newFragmentId, fragment.getHasValidationError());
        fragment.setId(newFragmentId);
        fragment.setName(name);

        checkNameIsUnique(
                // make sure we don't check against the fragment itself
                filter(fragmentRepository.getAll(), not(Predicates.propertyEqualTo("id", fragmentId))),
                fragment);

        fragmentRepository.updateLastUpdateAndSave(fragment);
        fragmentRepository.delete(fragmentId);
        responseEntity = getMovedResourceResponse(request, newFragmentId, "/name");
        return responseEntity;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getAll(@RequestParam(value = "view", defaultValue = "full") String view,
                                         @RequestParam(value = "notUsedBy", required = false) String fragmentId)
            throws RepositoryException, IOException {
        byte[] json;
        List<Fragment> fragments = getFragments(fragmentId);
        fragments.stream().map(f -> {
            f.setStatus(fragmentService.getStatus(f));
            return f;
        }).collect(Collectors.toList());
        fillWithUsedBy(fragments);

        if ("light".equals(view)) {
            json = objectMapper.toJson(fragments, JsonViewLight.class);
        } else {
            json = objectMapper.toJson(transform(fragments, new Function<Fragment, Fragment>() {
                @Override
                public Fragment apply(Fragment fragment) {
                    fragment.setAssets(assetVisitor.visit(fragment));
                    return fragment;
                }
            }));
        }
        //In our case we don't know the view asked outside this method. So like we can't know which JsonView used, I
        //build the json manually but in the return I must specify the mime-type in the header
        //{@link ResourceControllerAdvice#getHttpHeaders()}
        return new ResponseEntity<>(new String(json), ResourceControllerAdvice.getHttpHeaders(), HttpStatus.OK);
    }

    private List<Fragment> getFragments(String fragmentId) {
        if (!isBlank(fragmentId)) {
            return fragmentRepository.getAllNotUsingElement(fragmentId);
        } else {
            return fragmentRepository.getAll();
        }
    }

    private void fillWithUsedBy(Fragment fragment) {
        for (Repository repo : usedByRepositories) {
            fragment.addUsedBy(repo.getComponentName(), repo.findByObjectId(fragment.getId()));
        }
    }

    private void fillWithUsedBy(List<Fragment> fragments) {
        List<String> fragmentIds = new ArrayList<>();
        for (Fragment fragment : fragments) {
            fragmentIds.add(fragment.getId());
        }
        Map<String, List<Identifiable>> map;
        for (Repository repo : usedByRepositories) {
            map = repo.findByObjectIds(fragmentIds);
            for (Fragment fragment : fragments) {
                fragment.addUsedBy(repo.getComponentName(), map.get(fragment.getId()));
            }
        }
    }

    @RequestMapping(value = "/{fragmentId}", method = RequestMethod.GET)
    public ResponseEntity<Object> get(@PathVariable("fragmentId") String fragmentId) throws
            NotFoundException, RepositoryException {
        Fragment fragment = fragmentService.get(fragmentId);

        if (fragment.getStatus() != null && !fragment.getStatus().isCompatible()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            return new ResponseEntity(String.format("Fragment %s is in an incompatible version. Newer UI Designer version is required.", fragmentId), headers, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        fragment.setAssets(assetVisitor.visit(fragment));

        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("valueAsArray", SimpleBeanPropertyFilter.serializeAllExcept("value"));
        MappingJacksonValue mapping = new MappingJacksonValue(fragment);
        mapping.setFilters(filters);

        return new ResponseEntity(mapping, HttpStatus.OK);
    }

    @RequestMapping(value = "/{fragmentId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("fragmentId") String fragmentId) throws RepositoryException {

        Fragment fragment = new Fragment();
        fragment.setId(fragmentId);
        fillWithUsedBy(fragment);

        //if this fragment is used elsewhere we prevent the deletion. A fragment can be used in a page
        //or in an another fragment
        if (fragment.isUsed()) {
            throw new InUseException(buildErrorMessage(fragment));
        }

        fragmentRepository.delete(fragmentId);
    }

    @RequestMapping(value = "/{fragmentId}/favorite", method = RequestMethod.PUT)
    public void favorite(@PathVariable("fragmentId") String pageId, @RequestBody Boolean favorite) throws
            RepositoryException {
        if (favorite) {
            fragmentRepository.markAsFavorite(pageId);
        } else {
            fragmentRepository.unmarkAsFavorite(pageId);
        }
    }

    private String buildErrorMessage(Fragment fragment) {
        //if an error occurred it's useful for user to know which components use this widget
        StringBuilder msg = new StringBuilder("The fragment cannot be deleted because it is used in");

        for (Map.Entry<String, List<Identifiable>> entry : fragment.getUsedBy().entrySet()) {
            List<? extends Identifiable> elements = entry.getValue();
            if (!elements.isEmpty()) {
                msg.append(" ").append(elements.size()).append(" " + entry.getKey()).append(elements.size() > 1 ? "s" : "");
                for (Identifiable element : elements) {
                    msg.append(", <").append(element.getName()).append(">");
                }
            }
        }
        return msg.toString();
    }

    private void checkNameIsUnique(Collection<Fragment> fragments, Fragment fragment) {
        checkNotAllowed(any(fragments, Predicates.propertyEqualTo("name", fragment.getName())),
                format("A fragment with name %s already exists", fragment.getName()));
    }
}
