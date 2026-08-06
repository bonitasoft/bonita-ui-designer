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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_REMOVAL;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.ResponseHeadersHelper.getMovedResourceResponse;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.service.FragmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@RestController
@RequestMapping("/rest/fragments")
public class FragmentResource {

    private final SimpMessagingTemplate messagingTemplate;

    private final FragmentService fragmentService;

    private final JsonHandler jsonHandler;

    @Autowired
    public FragmentResource(FragmentService fragmentService, JsonHandler jsonHandler, SimpMessagingTemplate messagingTemplate) {
        this.jsonHandler = jsonHandler;
        this.messagingTemplate = messagingTemplate;
        this.fragmentService = fragmentService;
    }

    @PostMapping
    public ResponseEntity<Fragment> create(@RequestBody Fragment newFragment) throws RepositoryException {

        var savedFragment = fragmentService.create(newFragment);

        return ResponseEntity.status(CREATED).body(savedFragment);
    }

    @PutMapping(value = "/{fragmentId}")
    public ResponseEntity<?> save(HttpServletRequest request, @PathVariable("fragmentId") String fragmentId, @RequestBody Fragment newFragment) throws RepositoryException {

        Fragment savedFragment;
        try {
            savedFragment = fragmentService.save(fragmentId, newFragment);
        } catch (ModelException e) {
            return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        }

        if (!fragmentId.equals(savedFragment.getId())) {
            // Redirect to new fragment location
            messagingTemplate.convertAndSend(PREVIEWABLE_REMOVAL, fragmentId);
            return getMovedResourceResponse(request, savedFragment.getId());
        } else {
            messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, fragmentId);
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping(value = "/{fragmentId}/name")
    public ResponseEntity<?> rename(HttpServletRequest request, @PathVariable("fragmentId") String fragmentId, @RequestBody String name) throws RepositoryException {
        var fragment = fragmentService.get(fragmentId);
        if (!name.equals(fragment.getName())) {
            try {
                var savedFragment = fragmentService.rename(fragment, name);
                // Redirect to new fragment location
                return getMovedResourceResponse(request, savedFragment.getId(), "/name");
            } catch (ModelException e) {
                return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<String> getAll(
            @RequestParam(value = "view", defaultValue = "full") String view,
            @RequestParam(value = "notUsedBy", required = false) String fragmentId)
            throws RepositoryException, IOException {

        var fragments = fragmentService.getAllNotUsingFragment(fragmentId);

        String json;
        if ("light".equals(view)) {
            json = jsonHandler.toJsonString(fragments, JsonViewLight.class);
        } else {
            json = jsonHandler.toJsonString(
                    fragments.stream().map(fragment -> {
                        fragment.setAssets(fragmentService.listAsset(fragment));
                        return fragment;
                    }).collect(toList())
            );
        }
        //In our case we don't know the view asked outside this method. So like we can't know which JsonView used, I
        //build the json manually but in the return I must specify the mime-type in the header
        //{@link ResourceControllerAdvice#getHttpHeaders()}
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(json);
    }

    @GetMapping(value = "/{fragmentId}")
    public ResponseEntity<?> get(@PathVariable("fragmentId") String fragmentId) throws NotFoundException, RepositoryException {

        //@TODO Why don't we use "getWithAsset"? Possible side effect?
        var fragment = fragmentService.get(fragmentId);

        if (fragment.getStatus() != null && !fragment.getStatus().isCompatible()) {
            return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(format("Fragment %s is in an incompatible version. Newer UI Designer version is required.", fragmentId));
        }
        fragment.setAssets(fragmentService.listAsset(fragment));

        var filters = new SimpleFilterProvider()
                .addFilter("valueAsArray", SimpleBeanPropertyFilter.serializeAllExcept("value"));
        var mapping = new MappingJacksonValue(fragment);
        mapping.setFilters(filters);
        return ResponseEntity.ok(mapping);
    }

    @DeleteMapping(value = "/{fragmentId}")
    public void delete(@PathVariable("fragmentId") String fragmentId) throws RepositoryException {
        fragmentService.delete(fragmentId);
    }

    @PutMapping(value = "/{fragmentId}/favorite")
    public void favorite(@PathVariable("fragmentId") String fragmentId, @RequestBody Boolean favorite) throws
            RepositoryException {
        fragmentService.markAsFavorite(fragmentId, favorite);
    }

    @PostMapping(value = "/autoWebResources")
    public List<WebResource> getWebResources(@RequestBody Fragment fragment) {
        return fragmentService.detectAutoWebResources(fragment);
    }
}
