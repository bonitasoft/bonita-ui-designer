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
package org.bonitasoft.web.designer.rest;

import static org.bonitasoft.web.designer.config.WebSocketConfig.PREVIEWABLE_UPDATE;

import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.experimental.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/pages")
public class PageResource extends DataResource<Page> {

    private SimpMessagingTemplate messagingTemplate;
    private ContractToPageMapper contractToPageMapper;

    @Inject
    public PageResource(PageRepository pageRepository, SimpMessagingTemplate messagingTemplate, ContractToPageMapper contractToPageMapper) {
        super(pageRepository);
        this.messagingTemplate = messagingTemplate;
        this.contractToPageMapper = contractToPageMapper;
    }

    /**
     * Lists all the pages in the repository
     */
    @RequestMapping(method = RequestMethod.GET)
    @JsonView(JsonViewLight.class)
    public List<Page> list() throws RepositoryException {
        return getRepository().getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Page> create(@RequestBody Page content) throws RepositoryException {
        // the page should not have an ID. If it has one, we ignore it and generate one
        String pageId = UUID.randomUUID().toString();
        content.setId(pageId);
        getRepository().save(content);
        return new ResponseEntity<>(content, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/contract/{name}", method = RequestMethod.POST)
    public ResponseEntity<Page> create(@RequestBody Contract contract, @PathVariable("name") String name) throws RepositoryException {
        return create(contractToPageMapper.createPage(name, contract));
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.PUT)
    public void save(@PathVariable("pageId") String pageId, @RequestBody Page content) throws RepositoryException {
        // the page should have the same ID as pageId.
        content.setId(pageId);
        getRepository().save(content);
        // send notification of update
        messagingTemplate.convertAndSend(PREVIEWABLE_UPDATE, pageId);
    }

    @RequestMapping(value = "/{pageId}/name", method = RequestMethod.PUT)
    public void rename(@PathVariable("pageId") String pageId, @RequestBody String name) throws RepositoryException {
        Page page = getRepository().get(pageId);
        page.setName(name);
        getRepository().save(page);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.GET)
    public Page get(@PathVariable("pageId") String pageId) throws NotFoundException, RepositoryException {
        return getRepository().get(pageId);
    }

    @RequestMapping(value = "/{pageId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("pageId") String pageId) throws RepositoryException {
        getRepository().delete(pageId);
    }
}
