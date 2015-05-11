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

import java.util.Map;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Data resource.
 * Path is set in parent resources as it is a sub-resource
 */
public abstract class DataResource<T extends Identifiable & Previewable> {

    private Repository<T> repository;

    public DataResource(Repository<T> repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET)
    public Map<String, Data> getPageData(@PathVariable("id") String id) throws NotFoundException, RepositoryException {
        return repository.get(id).getData();
    }

    @RequestMapping(value = "/{id}/data/{dataName}", method = RequestMethod.PUT)
    public Map<String, Data> saveData(@PathVariable("id") String id, @PathVariable("dataName") String dataName, @RequestBody Data data)
            throws NotFoundException, RepositoryException {
        T previewable = repository.get(id);
        previewable.addData(dataName, data);
        repository.save(previewable);
        return previewable.getData();
    }

    @RequestMapping(value = "/{id}/data/{dataName}", method = RequestMethod.DELETE)
    public Map<String, Data> deleteData(@PathVariable("id") String id, @PathVariable("dataName") String dataName) throws NotFoundException,
            RepositoryException {
        T previewable = repository.get(id);
        previewable.removeData(dataName);
        repository.save(previewable);
        return previewable.getData();
    }

    public Repository<T> getRepository() {
        return repository;
    }

}
