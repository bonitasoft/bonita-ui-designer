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
package org.bonitasoft.web.designer.controller.importer.report;

import org.bonitasoft.web.designer.controller.importer.dependencies.ComponentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.Identifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dependencies {

    private Map<String, List<Object>> added;
    private Map<String, List<Object>> overwritten;

    public static Dependencies from(Map<DependencyImporter, List<?>> dependenciesAsList) {
        var dependencies = new Dependencies();
        for (Map.Entry<DependencyImporter, List<?>> entry : dependenciesAsList.entrySet()) {
            DependencyImporter<?> dependencyImporter = entry.getKey();
            if (dependencyImporter instanceof ComponentDependencyImporter<?>) {
                dependencies.add((List<Identifiable>) entry.getValue(), (ComponentDependencyImporter<Identifiable>) dependencyImporter);
            }
        }
        return dependencies;
    }

    private void add(List<Identifiable> identifiables, ComponentDependencyImporter<Identifiable> importer) {
        for (Identifiable identifiable : identifiables) {
            if (importer.exists(identifiable)) {
                addOverwrittenDependency(importer.getComponentName(), importer.getOriginalElementFromRepository(identifiable));
            } else {
                addAddedDependency(importer.getComponentName(), identifiable);
            }
        }
    }

    public Map<String, List<Object>> getAdded() {
        return added;
    }

    public Map<String, List<Object>> getOverwritten() {
        return overwritten;
    }

    public void addOverwrittenDependency(String componentName, Object dependency) {
        overwritten = addDependency(overwritten, componentName, dependency);
    }

    public void addAddedDependency(String componentName, Object dependency) {
        added = addDependency(added, componentName, dependency);
    }

    private Map<String, List<Object>> addDependency(Map<String, List<Object>> map, String componentName, Object dependencies) {
        if (map == null) {
            map = new HashMap<>();
        }
        map.computeIfAbsent(componentName, key -> new ArrayList<>())
                .add(dependencies);
        return map;
    }
}
