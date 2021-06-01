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
package org.bonitasoft.web.designer.utils;

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

import java.util.HashMap;
import java.util.Map;


public class FakePageRepository extends PageRepository {

    private final Map<String, Page> repo;

    public FakePageRepository() {
        super(new WorkspaceProperties(), new WorkspaceUidProperties(), null, null, null,null);
        repo = new HashMap<>();
    }

    @Override
    public Page save(Page page) throws RepositoryException {
        repo.put(page.getId(), page);
        return page;
    }

    @Override
    public Page get(String id) throws NotFoundException, RepositoryException {
        Page page = repo.get(id);
        if (page == null) {
            throw new NotFoundException(String.format("Page with id %s not found", page.getId()));
        }
        return page;
    }
}
