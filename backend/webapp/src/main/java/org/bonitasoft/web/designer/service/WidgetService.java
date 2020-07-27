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
package org.bonitasoft.web.designer.service;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.MigrationResource;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.migration.MigrationException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;

@Named
public class WidgetService implements ArtifactService {

    private final WidgetMigrationApplyer widgetMigrationApplyer;
    private WidgetIdVisitor widgetIdVisitor;
    private WidgetRepository widgetRepository;
    private List<BondsTypesFixer> bondsTypesFixers;

    @Inject
    public WidgetService(WidgetRepository widgetRepository,
                         List<BondsTypesFixer> bondsTypesFixers,
                         WidgetMigrationApplyer widgetMigrationApplyer,
                         WidgetIdVisitor widgetIdVisitor) {
        this.widgetRepository = widgetRepository;
        this.bondsTypesFixers = bondsTypesFixers;
        this.widgetMigrationApplyer = widgetMigrationApplyer;
        this.widgetIdVisitor = widgetIdVisitor;
    }

    public List<Property> updateProperty(String widgetId, String propertyName, Property property) {
        for (BondsTypesFixer bondsTypesFixer : bondsTypesFixers) {
            bondsTypesFixer.fixBondsTypes(widgetId, singletonList(property));
        }
        return widgetRepository.updateProperty(widgetId, propertyName, property);
    }

    @Override
    public Widget get(String id) {
        Widget widget = this.widgetRepository.get(id);
        return migrate(widget);
    }

    @Override
    public Widget migrate(Identifiable artifact) {
        MigrationResult<Widget> migrationResult = migrateWithReport(artifact);
        return migrationResult.getArtifact();
    }

    @Override
    public MigrationResult<Widget> migrateWithReport(Identifiable artifact) {
        String formerArtifactVersion = artifact.getArtifactVersion();
        MigrationResult<Widget> migratedResult = widgetMigrationApplyer.migrate((Widget) artifact);
        Widget migratedWidget = migratedResult.getArtifact();
        if (!StringUtils.equals(formerArtifactVersion, migratedWidget.getArtifactVersion())  && !migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            widgetRepository.updateLastUpdateAndSave(migratedWidget);
        }
        return migratedResult;
    }

    @Override
    public MigrationStatusReport getStatus(Identifiable artifact) {
        // not used since a widget has no dependencies
        return null;
    }

    public List<MigrationStepReport> migrateAllCustomWidgetUsedInPreviewable(Previewable previewable) {
        List<MigrationStepReport> migrationStepReports = new ArrayList<>();

        widgetRepository.getByIds(widgetIdVisitor.visit(previewable))
                .forEach(w -> {
                    MigrationResult<Widget> result = this.migrateWithReport(w);
                    migrationStepReports.addAll(result.getMigrationStepReportListFilterByFinalStatus());
                });

        return migrationStepReports;
    }

    public MigrationStatusReport getMigrationStatusOfCustomWidgetUsed(Previewable previewable) {
        List<MigrationStatusReport> reports = new ArrayList<>();
        widgetRepository.getByIds(widgetIdVisitor.visit(previewable))
                .forEach(widget -> {
                    MigrationStatusReport report = MigrationResource.getStatus(widget);
                    reports.add(report);
                });

        boolean migration = false;
        for (MigrationStatusReport report : reports) {
            if (!report.isCompatible()) {
                return report;
            }
            if (!migration && report.isMigration()) {
                migration = true;
            }
        }
        return new MigrationStatusReport(true, migration);
    }
}
