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
import static com.google.common.collect.Sets.filter;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.PageAssetPredicate;
import org.bonitasoft.web.designer.experimental.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.experimental.mapping.FormScope;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

@RestController
@RequestMapping("/rest/pages")
public class PageResource extends AssetResource<Page> {

    protected static final Logger logger = LoggerFactory.getLogger(PageResource.class);
    private PageRepository pageRepository;
    private SimpMessagingTemplate messagingTemplate;
    private ContractToPageMapper contractToPageMapper;

    @Inject
    public PageResource(
            PageRepository pageRepository,
            SimpMessagingTemplate messagingTemplate,
            ContractToPageMapper contractToPageMapper,
            AssetService<Page> pageAssetService,
            AssetVisitor assetVisitor) {
        super(pageAssetService, pageRepository, assetVisitor);
        this.pageRepository = pageRepository;
        this.messagingTemplate = messagingTemplate;
        this.contractToPageMapper = contractToPageMapper;
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
    public ResponseEntity<Page> create(@RequestBody Page page, @RequestParam(value = "duplicata", required = false) String sourcePageId) throws RepositoryException {
        // the page should not have an ID. If it has one, we ignore it and generate one
        String pageId = UUID.randomUUID().toString();
        page.setId(pageId);
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
        return create(contractToPageMapper.createFormPage(name, contract, FormScope.valueOf(scope.toUpperCase())), null);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.PUT)
    public void save(@PathVariable("pageId") String pageId, @RequestBody Page page) throws RepositoryException {
        // the page should have the same ID as pageId.
        page.setId(pageId);
        page.setAssets(filter(page.getAssets(), new PageAssetPredicate()));
        pageRepository.updateLastUpdateAndSave(page);
        // send notification of update
        messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, pageId);
    }

    @RequestMapping(value = "/{pageId}/name", method = RequestMethod.PUT)
    public void rename(@PathVariable("pageId") String pageId, @RequestBody String name) throws RepositoryException {
        Page page = pageRepository.get(pageId);
        page.setName(name);
        pageRepository.updateLastUpdateAndSave(page);
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
        Page page = pageRepository.get(pageId);
        page.setAssets(assetVisitor.visit(page));
        return page;
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("pageId") String pageId) throws RepositoryException {
        pageRepository.delete(pageId);
    }

}
