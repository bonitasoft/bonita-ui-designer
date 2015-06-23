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

import static java.lang.Boolean.TRUE;
import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.experimental.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.experimental.mapping.FormScope;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.Asset.JsonViewAsset;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rest/pages")
public class PageResource {

    protected static final Logger logger = LoggerFactory.getLogger(PageResource.class);
    private AssetService<Page> pageAssetService;
    private PageRepository pageRepository;
    private SimpMessagingTemplate messagingTemplate;
    private ContractToPageMapper contractToPageMapper;
    private AssetVisitor assetVisitor;

    @Inject
    public PageResource(
            PageRepository pageRepository,
            SimpMessagingTemplate messagingTemplate,
            ContractToPageMapper contractToPageMapper,
            AssetService<Page> pageAssetService,
            AssetVisitor assetVisitor) {
        this.pageRepository = pageRepository;
        this.messagingTemplate = messagingTemplate;
        this.contractToPageMapper = contractToPageMapper;
        this.pageAssetService = pageAssetService;
        this.assetVisitor = assetVisitor;
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
    public ResponseEntity<Page> create(@RequestBody Page content) throws RepositoryException {
        // the page should not have an ID. If it has one, we ignore it and generate one
        String pageId = UUID.randomUUID().toString();
        content.setId(pageId);
        pageRepository.save(content);
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/contract/{scope}/{name}", method = RequestMethod.POST)
    public ResponseEntity<Page> create(@RequestBody Contract contract, @PathVariable("scope") String scope, @PathVariable("name") String name)
            throws RepositoryException {
        return create(contractToPageMapper.createPage(name, contract, FormScope.valueOf(scope.toUpperCase())));
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.PUT)
    public void save(@PathVariable("pageId") String pageId, @RequestBody Page content) throws RepositoryException {
        // the page should have the same ID as pageId.
        content.setId(pageId);
        pageRepository.save(content);
        // send notification of update
        messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, pageId);
    }

    @RequestMapping(value = "/{pageId}/name", method = RequestMethod.PUT)
    public void rename(@PathVariable("pageId") String pageId, @RequestBody String name) throws RepositoryException {
        Page page = pageRepository.get(pageId);
        page.setName(name);
        pageRepository.save(page);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.GET)
    public Page get(@PathVariable("pageId") String pageId) throws NotFoundException, RepositoryException {
        return pageRepository.get(pageId);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("pageId") String pageId) throws RepositoryException {
        pageRepository.delete(pageId);
    }

    @RequestMapping(value = "/{pageId}/assets/{type}", method = RequestMethod.POST)
    public ResponseEntity<ErrorMessage> uploadAsset(@RequestParam("file") MultipartFile file, @PathVariable("pageId") String id,
                                                    @PathVariable("type") String type) {
        try{
            pageAssetService.upload(file, pageRepository.get(id), type);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (RepositoryException | IllegalArgumentException e){
            logger.error(e.getMessage(),e);
            return new ResponseEntity<>(new ErrorMessage(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{pageId}/assets/{assetId}", method = RequestMethod.PUT)
    public void incrementOrder(
            @PathVariable("pageId") String pageId,
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "increment", required = false) Boolean increment,
            @RequestParam(value = "decrement", required = false) Boolean decrement,
            @RequestParam(value = "active", required = false) Boolean active) {
        if (increment != null || decrement != null) {
            pageAssetService.changeAssetOrderInComponent(pageRepository.get(pageId), assetId, TRUE.equals(increment) ? INCREMENT : DECREMENT);
        }
        if (active != null) {
            pageAssetService.changeAssetStateInPreviewable(pageRepository.get(pageId), assetId, active);
        }
    }

    @RequestMapping(value = "/{pageId}/assets", method = RequestMethod.POST)
    public void saveAsset(@RequestBody Asset asset, @PathVariable("pageId") String id) {
        pageAssetService.save(pageRepository.get(id), asset);
    }

    @RequestMapping(value = "/{pageId}/assets/{assetId}", method = RequestMethod.DELETE)
    public void deleteAsset(@PathVariable("pageId") String pageId, @PathVariable("assetId") String assetId) throws RepositoryException {
        pageAssetService.delete(pageRepository.get(pageId), assetId);
    }

    @RequestMapping(value = "/{pageId}/assets")
    @JsonView(JsonViewAsset.class)
    public Set<Asset> assets(@PathVariable("pageId") String id) {
        return assetVisitor.visit(pageRepository.get(id));
    }
}
