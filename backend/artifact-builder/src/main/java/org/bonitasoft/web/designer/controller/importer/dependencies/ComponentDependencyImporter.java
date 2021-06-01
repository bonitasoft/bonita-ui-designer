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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Repository;

public abstract class ComponentDependencyImporter<T extends Identifiable> implements DependencyImporter<T> {

    private final Repository<T> repository;

    protected ComponentDependencyImporter(Repository<T> repository) {
        this.repository = repository;
    }

    public String getComponentName() {
        return repository.getComponentName();
    }

    public boolean exists(T element) {
        return repository.exists(element.getId());
    }

    public T getOriginalElementFromRepository(T element) {
        return repository.get(element.getId());
    }
}
