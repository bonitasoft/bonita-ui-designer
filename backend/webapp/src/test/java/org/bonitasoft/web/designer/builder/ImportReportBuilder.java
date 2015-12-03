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
package org.bonitasoft.web.designer.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.controller.importer.report.Dependencies;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.Identifiable;

public class ImportReportBuilder {

    private Identifiable element;
    List<Identifiable> added = new ArrayList<>();
    List<Identifiable> overridden = new ArrayList<>();
    private String uuid;
    private ImportReport.Status status;

    private boolean override;

    public ImportReportBuilder(Identifiable element) {
        this.element = element;
    }

    public static ImportReportBuilder anImportReportFor(PageBuilder builder) {
        return new ImportReportBuilder(builder.build());
    }

    public static ImportReportBuilder anImportReportFor(WidgetBuilder builder) {
        return new ImportReportBuilder(builder.build());
    }

    public ImportReportBuilder withAdded(WidgetBuilder builder) {
        added.add(builder.build());
        return this;
    }

    public ImportReportBuilder withOverridden(WidgetBuilder builder) {
        overridden.add(builder.build());
        return this;
    }

    public ImportReportBuilder withUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ImportReportBuilder withOverride(boolean override) {
        this.override = override;
        return this;
    }

    public ImportReportBuilder withStatus(ImportReport.Status status) {
        this.status = status;
        return this;
    }

    public ImportReport build() {
        Dependencies dependencies = new Dependencies();
        for (Identifiable identifiable : added) {
            dependencies.addAddedDependency(identifiable.getClass().getSimpleName().toLowerCase(), identifiable);
        }
        for (Identifiable identifiable : overridden) {
            dependencies.addOverriddenDependency(identifiable.getClass().getSimpleName().toLowerCase(), identifiable);
        }
        ImportReport importReport = new ImportReport(element, dependencies);
        importReport.setUUID(uuid);
        importReport.setOverridden(override);
        importReport.setStatus(status);
        return importReport;
    }
}
