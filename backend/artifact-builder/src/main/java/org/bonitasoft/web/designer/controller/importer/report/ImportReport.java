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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.Identifiable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImportReport {

    private Status status;
    private Identifiable element;
    private Identifiable overwrittenElement;
    private Boolean overwritten = false;
    private Dependencies dependencies;
    private String uuid;

    public ImportReport(Identifiable element, Dependencies dependencies) {
        this.element = element;
        this.dependencies = dependencies;
    }

    public static ImportReport from(Identifiable element, Map<DependencyImporter, List<?>> dependencies) {
        return new ImportReport(element, Dependencies.from(dependencies));
    }

    public Identifiable getElement() {
        return element;
    }

    public void setElement(Identifiable element) {
        this.element = element;
    }

    public Identifiable getOverwrittenElement() {
        return overwrittenElement;
    }

    public void setOverwrittenElement(Identifiable overwrittenElement) {
        this.overwrittenElement = overwrittenElement;
    }

    public boolean isOverwritten() {
        return overwritten;
    }

    public void setOverwritten(Boolean overwritten) {
        this.overwritten = overwritten;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    public boolean doesNotOverwriteElements() {
        return !this.isOverwritten() && (this.getDependencies().getOverwritten() == null || this.getDependencies().getOverwritten().isEmpty());
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        IMPORTED, CONFLICT, INCOMPATIBLE;

        @JsonValue
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
