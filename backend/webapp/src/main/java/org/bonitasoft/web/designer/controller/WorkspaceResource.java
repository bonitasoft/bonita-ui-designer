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

import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/indexing")
public class WorkspaceResource {

    private final Workspace workspace;
    private final PageRepository pageRepository;

    @Autowired
    public WorkspaceResource(PageRepository pageRepository, Workspace workspace) {
        this.pageRepository = pageRepository;
        this.workspace = workspace;
    }

    @PostMapping
    public String indexing() {
        var pages = pageRepository.getAll();
        workspace.indexingArtifacts(pages);
        return "Workspace Indexing has been triggered.";
    }
}
