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

import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_REMOVAL;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.ResponseHeadersHelper.getMovedResourceResponse;
import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.generator.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.generator.mapping.FormScope;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.exception.IncompatibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

@RestController
@RequestMapping("/rest/pages")
public class PageResource extends AssetResource<Page, PageService> {

    protected static final Logger logger = LoggerFactory.getLogger(PageResource.class);

    private final ContractToPageMapper contractToPageMapper;

    @Autowired
    public PageResource(JsonHandler jsonHandler,
                        PageService pageService,
                        ContractToPageMapper contractToPageMapper, SimpMessagingTemplate messagingTemplate) {
        super(jsonHandler, pageService, messagingTemplate);
        this.contractToPageMapper = contractToPageMapper;
    }

    /**
     * Lists all the pages in the repository
     */
    @GetMapping
    @JsonView(JsonViewLight.class)
    public List<Page> list() throws RepositoryException {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<Page> create(@RequestBody Page page,
                                       @RequestParam(value = "duplicata", required = false) String sourcePageId)
            throws RepositoryException {

        Page savedPage;
        if (hasText(sourcePageId)) {
            savedPage = service.createFrom(sourcePageId, page);
        } else {
            savedPage = service.create(page);
        }

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedPage.getId()).toUri();

        return ResponseEntity.created(location).body(savedPage);
    }

    @PostMapping(value = "/contract/{scope}/{name}")
    public ResponseEntity<Page> create(@RequestBody Contract contract, @PathVariable("scope") String scope, @PathVariable("name") String name)
            throws RepositoryException {
        var formPage = contractToPageMapper.createFormPage(name, contract, FormScope.valueOf(scope.toUpperCase(Locale.ENGLISH)));
        return create(formPage, null);
    }

    @PutMapping(value = "/{pageId}")
    public ResponseEntity<?> save(HttpServletRequest request, @PathVariable("pageId") String pageId, @RequestBody Page page) throws RepositoryException {
        try {
            var savedPage = service.save(pageId, page);

            if (!savedPage.getId().equals(pageId)) {
                // send notification of removal
                messagingTemplate.ifPresent(template -> template.convertAndSend(PREVIEWABLE_REMOVAL, pageId));
                return getMovedResourceResponse(request, savedPage.getId());
            } else {
                // send notification of update
                messagingTemplate.ifPresent(template -> template.convertAndSend(PREVIEWABLE_UPDATE, pageId));
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).build();
            }

        } catch (IncompatibleException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(e.getMessage());
        }
    }

    @PutMapping(value = "/{pageId}/name")
    public ResponseEntity<?> rename(HttpServletRequest request, @PathVariable("pageId") String pageId, @RequestBody String name) throws RepositoryException {

        var page = service.get(pageId);

        if (!page.isCompatible()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Page " + page.getId() + " is in an incompatible version. Newer UI Designer version is required.");
        }

        if (!page.getName().equals(name)) {
            var renamedPage = service.rename(pageId, name);
            // send notification of removal
            messagingTemplate.ifPresent(template -> template.convertAndSend(PREVIEWABLE_REMOVAL, pageId));
            return getMovedResourceResponse(request, renamedPage.getId(), "/name");
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{pageId}/favorite")
    public void favorite(@PathVariable("pageId") String pageId, @RequestBody boolean favorite)
            throws RepositoryException {
        service.markAsFavorite(pageId, favorite);
    }

    @GetMapping(value = "/{pageId}")
    public ResponseEntity<?> get(@PathVariable("pageId") String pageId) throws NotFoundException, RepositoryException {
        var page = service.getWithAsset(pageId);

        if (!page.isCompatible()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Page " + page.getId() + " is in an incompatible version. Newer UI Designer version is required.");
        }

        FilterProvider filters = new SimpleFilterProvider()
                .addFilter("valueAsArray", SimpleBeanPropertyFilter.serializeAllExcept("value"));
        var mapping = new MappingJacksonValue(page);
        mapping.setFilters(filters);

        return ResponseEntity.ok(mapping);
    }

    @DeleteMapping(value = "/{pageId}")
    public void delete(@PathVariable("pageId") String pageId) throws RepositoryException {
        service.delete(pageId);
        // send notification of removal
        messagingTemplate.ifPresent(template -> template.convertAndSend(PREVIEWABLE_REMOVAL, pageId));
    }

    @GetMapping(value = "/{pageId}/resources")
    public List<String> getResources(@PathVariable("pageId") String pageId) {
        var page = service.get(pageId);
        return service.getResources(page);
    }

    @PostMapping(value = "/autoWebResources")
    public List<WebResource> getWebResources(@RequestBody Page page) {
        return service.detectAutoWebResources(page);
    }


    @GetMapping(value = "/{artifactId}/assets")
    @JsonView(Asset.JsonViewAsset.class)
    public Set<Asset> getAssets(@PathVariable("artifactId") String id) {
        var page = service.get(id);
        return service.listAsset(page);
    }

}
