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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.filterValues;
import static com.google.common.collect.Sets.filter;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_REMOVAL;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.ResponseHeadersHelper.getMovedResourceResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Optional;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.PageAssetPredicate;
import org.bonitasoft.web.designer.controller.export.properties.BonitaResourcePredicate;
import org.bonitasoft.web.designer.controller.export.properties.BonitaResourceTransformer;
import org.bonitasoft.web.designer.controller.export.properties.ConstantPropertyValuePredicate;
import org.bonitasoft.web.designer.experimental.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.experimental.mapping.FormScope;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.AuthRulesCollector;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/pages")
public class PageResource extends AssetResource<Page> {

    protected static final Logger logger = LoggerFactory.getLogger(PageResource.class);
    public static final String BONITA_RESOURCE_REGEX = ".+/API/(?!extension)([^ /]*)/([^ /?]*)[\\S+]*";// matches ..... /API/{}/{}?...
    public static final String EXTENSION_RESOURCE_REGEX = ".+/API/(?=extension)([^ /]*)/([^ (?|{)]*).*";

    private PageRepository pageRepository;
    private SimpMessagingTemplate messagingTemplate;
    private ContractToPageMapper contractToPageMapper;
    private final PageService pageService;
    private final ComponentVisitor componentVisitor;
    private final AuthRulesCollector authRulesCollector;

    @Inject
    public PageResource(
            PageService pageService,
            PageRepository pageRepository,
            SimpMessagingTemplate messagingTemplate,
            ContractToPageMapper contractToPageMapper,
            AssetService<Page> pageAssetService,
            AssetVisitor assetVisitor,
            ComponentVisitor componentVisitor, AuthRulesCollector authRulesCollector) {
        super(pageAssetService, pageRepository, assetVisitor, Optional.of(messagingTemplate));
        this.pageRepository = pageRepository;
        this.messagingTemplate = messagingTemplate;
        this.contractToPageMapper = contractToPageMapper;
        this.pageService = pageService;
        this.componentVisitor = componentVisitor;
        this.authRulesCollector = authRulesCollector;
    }

    @Override
    protected void checkArtifactId(String artifactId) {
        checkNotNull(artifactId);
    }

    /**
     * Lists all the pages in the repository
     */
    @RequestMapping(method = RequestMethod.GET)
    @JsonView(JsonViewLight.class)
    public List<Page> list() throws RepositoryException {
        return pageRepository.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Page> create(@RequestBody Page page, @RequestParam(value = "duplicata", required = false) String sourcePageId)
            throws RepositoryException {
        // the page should not have an ID. If it has one, we ignore it and generate one using the name
        String pageId = pageRepository.getNextAvailableId(page.getName());
        page.setId(pageId);
        // the page should not have an UUID. If it has one, we ignore it and generate one
        page.setUUID(UUID.randomUUID().toString());
        page.setAssets(filter(page.getAssets(), new PageAssetPredicate()));
        pageRepository.updateLastUpdateAndSave(page);
        if (isNotEmpty(sourcePageId)) {
            assetService.duplicateAsset(pageRepository.resolvePath(sourcePageId), pageRepository.resolvePath(sourcePageId), sourcePageId, pageId);
        } else {
            assetService.loadDefaultAssets(page);
        }
        return new ResponseEntity<>(page, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/contract/{scope}/{name}", method = RequestMethod.POST)
    public ResponseEntity<Page> create(@RequestBody Contract contract, @PathVariable("scope") String scope, @PathVariable("name") String name)
            throws RepositoryException {
        return create(contractToPageMapper.createFormPage(name, contract, FormScope.valueOf(scope.toUpperCase(Locale.ENGLISH))), null);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> save(HttpServletRequest request, @PathVariable("pageId") String pageId, @RequestBody Page page) throws RepositoryException {
        String newPageId;
        try {
            Page currentPage = pageService.get(pageId);
            if (currentPage.getName().equals(page.getName())) {
                // the page should have the same ID as pageId.
                newPageId = pageId;
            } else {
                newPageId = pageRepository.getNextAvailableId(page.getName());
            }
        } catch (NotFoundException e) {
            newPageId = pageRepository.getNextAvailableId(page.getName());
        }
        page.setId(newPageId);
        setPageUUIDIfNotSet(page);
        page.setAssets(filter(page.getAssets(), new PageAssetPredicate()));
        pageRepository.updateLastUpdateAndSave(page);
        ResponseEntity<Void> responseEntity;
        if(!newPageId.equals(pageId)) {
            assetService.duplicateAsset(pageRepository.resolvePath(pageId), pageRepository.resolvePath(pageId), pageId, newPageId);
            pageRepository.delete(pageId);
            responseEntity = getMovedResourceResponse(request, newPageId);
            // send notification of removal
            messagingTemplate.convertAndSend(PREVIEWABLE_REMOVAL, pageId);
        } else {
            // send notification of update
            messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, pageId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            responseEntity = new ResponseEntity<>(headers, HttpStatus.OK);
        }
        return responseEntity;
    }

    protected void setPageUUIDIfNotSet(Page page) {
        if(StringUtils.isEmpty(page.getUUID())) {
            //it is a new page so we set its UUID
            String pageUUID = UUID.randomUUID().toString();
            page.setUUID(pageUUID);
        }
    }

    @RequestMapping(value = "/{pageId}/name", method = RequestMethod.PUT)
    public ResponseEntity<Void> rename(HttpServletRequest request, @PathVariable("pageId") String pageId, @RequestBody String name) throws RepositoryException {
        Page page = pageService.get(pageId);
        ResponseEntity<Void> responseEntity;
        if(!page.getName().equals(name)) {
            String newPageId = pageRepository.getNextAvailableId(name);
            page.setId(newPageId);
            page.setName(name);
            pageRepository.updateLastUpdateAndSave(page);
            assetService.duplicateAsset(pageRepository.resolvePath(pageId), pageRepository.resolvePath(pageId), pageId, newPageId);
            pageRepository.delete(pageId);
            responseEntity = getMovedResourceResponse(request, newPageId, "/name");
            // send notification of removal
            messagingTemplate.convertAndSend(PREVIEWABLE_REMOVAL, pageId);
        } else {
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        }
        return responseEntity;
    }

    @RequestMapping(value = "/{pageId}/favorite", method = RequestMethod.PUT)
    public void favorite(@PathVariable("pageId") String pageId, @RequestBody Boolean favorite) throws RepositoryException {
        if (favorite) {
            pageRepository.markAsFavorite(pageId);
        } else {
            pageRepository.unmarkAsFavorite(pageId);
        }
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.GET)
    public Page get(@PathVariable("pageId") String pageId) throws NotFoundException, RepositoryException {
        Page page = pageService.get(pageId);
        page.setAssets(assetVisitor.visit(page));
        return page;
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("pageId") String pageId) throws RepositoryException {
        pageRepository.delete(pageId);
        // send notification of removal
        messagingTemplate.convertAndSend(PREVIEWABLE_REMOVAL, pageId);
    }

    @RequestMapping(value = "/{pageId}/resources", method = RequestMethod.GET)
    public List<String> getResources(@PathVariable("pageId") String pageId){
        Page page = pageService.get(pageId);
        List<String> resources = newArrayList(transform(
                filterValues(page.getData(), new BonitaResourcePredicate(BONITA_RESOURCE_REGEX)).values(),
                new BonitaResourceTransformer(BONITA_RESOURCE_REGEX)));

        List<String> extension = newArrayList(transform(
                filterValues(page.getData(), new BonitaResourcePredicate(EXTENSION_RESOURCE_REGEX)).values(),
                new BonitaResourceTransformer(EXTENSION_RESOURCE_REGEX)));

        resources.addAll(extension);

        Iterable<Component> components = componentVisitor.visit(page);

        if (any(components, new ConstantPropertyValuePredicate("Start process"))) {
            resources.add("POST|bpm/process");
        }

        if (any(components, new ConstantPropertyValuePredicate("Submit task"))) {
            resources.add("POST|bpm/userTask");
        }

        resources.addAll(authRulesCollector.visit(page));

        return resources.stream().distinct().collect(Collectors.toList());
    }

}
