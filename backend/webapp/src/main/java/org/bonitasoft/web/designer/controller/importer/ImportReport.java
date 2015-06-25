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
package org.bonitasoft.web.designer.controller.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.controller.importer.dependencies.ComponentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.Identifiable;

public class ImportReport {

    private Identifiable element;
    private Map<String, List<?>> dependencies;

    public static ImportReport from(Identifiable element, Map<DependencyImporter, List<?>> dependencies) {
        ImportReport report = new ImportReport();
        report.setElement(element);
        for (Map.Entry<DependencyImporter, List<?>> entry : dependencies.entrySet()) {
            DependencyImporter importer = entry.getKey();
            List<?> l = entry.getValue();
            if (importer instanceof ComponentDependencyImporter && !l.isEmpty()) {
                report.addDependency(((ComponentDependencyImporter) entry.getKey()).getComponentName(), l);
            }
        }
        return report;
    }

    public void setElement(Identifiable element) {
        this.element = element;
    }

    public void addDependency(String componentName, List<?> dependencies) {
        if (this.dependencies == null) {
            this.dependencies = new HashMap<>();
        }
        this.dependencies.put(componentName, dependencies);
    }

    public Identifiable getElement() {
        return element;
    }

    public Map<String, List<?>> getDependencies() {
        return dependencies;
    }
}
