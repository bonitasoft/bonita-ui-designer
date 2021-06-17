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

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class DefaultWidgetService extends AbstractAssetableArtifactService<WidgetRepository, Widget> implements WidgetService {

    private final WidgetMigrationApplyer widgetMigrationApplyer;

    private final WidgetIdVisitor widgetIdVisitor;

    private final AssetVisitor assetVisitor;

    private final List<BondsTypesFixer> bondsTypesFixers;

    // Frag and page repo
    private final List<WidgetContainerRepository> widgetContainerRepositories;

    public DefaultWidgetService(
            WidgetRepository widgetRepository,
            PageRepository pageRepository,
            FragmentRepository fragmentRepository,
            List<BondsTypesFixer> bondsTypesFixers,
            WidgetMigrationApplyer widgetMigrationApplyer,
            WidgetIdVisitor widgetIdVisitor,
            AssetVisitor assetVisitor,
            UiDesignerProperties uiDesignerProperties,
            AssetService<Widget> widgetAssetService) {
        super(uiDesignerProperties, widgetAssetService, widgetRepository);
        this.bondsTypesFixers = bondsTypesFixers;
        this.widgetMigrationApplyer = widgetMigrationApplyer;
        this.widgetIdVisitor = widgetIdVisitor;
        this.assetVisitor = assetVisitor;
        this.widgetContainerRepositories = List.of(fragmentRepository, pageRepository);
    }

    @Override
    protected void checkUpdatable(String id) {
        super.checkUpdatable(id);
        if (id.startsWith(WidgetRepository.ANGULARJS_STANDARD_PREFIX)) {
            throw new NotAllowedException("Not allowed to modify a non custom widgets");
        }
    }

    @Override
    public Widget create(Widget widget) {
        return repository.create(widget);
    }

    @Override
    public Widget createFrom(String sourceWidgetId, Widget widget) {
        var savedWidget = create(widget);
        var widgetsPath = getWidgetsPath();
        var sourceWidgetPath = repository.resolvePath(sourceWidgetId);
        assetService.duplicateAsset(widgetsPath, sourceWidgetPath, sourceWidgetId, savedWidget.getId());
        return savedWidget;
    }

    private Path getWidgetsPath() {
        return uiDesignerProperties.getWorkspace().getWidgets().getDir();
    }

    @Override
    public Widget save(String widgetId, Widget widget) {

        this.checkUpdatable(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only save a custom widget");
        }

        widget.setId(widgetId);
        return repository.updateLastUpdateAndSave(widget);
    }

    @Override
    public List<Property> addProperty(String widgetId, Property property) {
        this.checkUpdatable(widgetId);
        return repository.addProperty(widgetId, property);
    }

    @Override
    public List<Property> updateProperty(String widgetId, String propertyName, Property property) {
        this.checkUpdatable(widgetId);
        for (var bondsTypesFixer : bondsTypesFixers) {
            bondsTypesFixer.fixBondsTypes(widgetId, singletonList(property));
        }
        return repository.updateProperty(widgetId, propertyName, property);
    }

    @Override
    public List<Property> deleteProperty(String widgetId, String propertyName) {
        this.checkUpdatable(widgetId);
        return repository.deleteProperty(widgetId, propertyName);
    }

    @Override
    public Widget get(String id) {
        var widget = repository.get(id);
        return migrate(widget);
    }

    @Override
    public Widget getWithAsset(String id) {
        var widget = repository.get(id);
        widget = migrate(widget);
        widget.setAssets(assetVisitor.visit(widget));
        return widget;
    }

    @Override
    public List<Widget> getAll() {
        return repository.getAll().stream()
                .map(widget -> {
                    widget.setStatus(this.getStatus(widget));
                    return widget;
                }).collect(toList());
    }

    @Override
    public List<Widget> getAllWithUsedBy() {
        List<Widget> widgets = getAll();
        List<String> widgetIds = new ArrayList<>();
        for (Widget widget : widgets) {
            widgetIds.add(widget.getId());
        }

        for (var repository : widgetContainerRepositories) {
            var map = repository.getArtifactsUsingWidgets(widgetIds);
            for (Widget widget : widgets) {
                widget.addUsedBy(repository.getComponentName(), (List<Identifiable>) map.get(widget.getId()));
            }
        }
        return widgets;
    }

    @Override
    public void delete(String id) {
        var widget = repository.get(id);

        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only delete a custom widget");
        }

        checkNotUsedByOther(widget);

        repository.delete(id);
    }

    /**
     * throw {@link InUseException} if widget is used by other components
     *
     * @param widget
     */
    private void checkNotUsedByOther(Widget widget) {
        //if any, it's useful for user to know which components use this widget
        var message = new StringBuilder("The widget cannot be deleted because it is used in");
        for (var repository : widgetContainerRepositories) {
            List<Identifiable> componentsUsingWidget = repository.getArtifactsUsingWidget(widget.getId());
            // If any components found, prepare error message
            if (!componentsUsingWidget.isEmpty()) {
                var size = componentsUsingWidget.size();
                message.append(" ").append(size).append(" ").append(repository.getComponentName()).append(size > 1 ? "s" : "");
                componentsUsingWidget.forEach(component -> message.append(", <").append(component.getName()).append(">"));
                // Set the found components on instance
                widget.addUsedBy(repository.getComponentName(), componentsUsingWidget);
            }
        }
        //if this widget is used elsewhere we prevent the deletion.
        if (widget.isUsed()) {
            throw new InUseException(message.toString());
        }

    }

    @Override
    public Widget migrate(Widget widget) {
        var migrationResult = migrateWithReport(widget);
        return migrationResult.getArtifact();
    }

    @Override
    public MigrationResult<Widget> migrateWithReport(Widget widgetToMigrate) {
        widgetToMigrate.setStatus(getStatus(widgetToMigrate));

        if (!widgetToMigrate.getStatus().isMigration()) {
            return new MigrationResult<>(widgetToMigrate, Collections.emptyList());
        }

        var migratedResult = widgetMigrationApplyer.migrate(widgetToMigrate);
        var migratedWidget = migratedResult.getArtifact();
        if (!migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            repository.updateLastUpdateAndSave(migratedWidget);
        }
        return migratedResult;
    }

    @Override
    public List<MigrationStepReport> migrateAllCustomWidgetUsedInPreviewable(Previewable previewable) {
        List<MigrationStepReport> migrationStepReports = new ArrayList<>();

        repository.getByIds(widgetIdVisitor.visit(previewable))
                .forEach(w -> {
                    var result = this.migrateWithReport(w);
                    migrationStepReports.addAll(result.getMigrationStepReportListFilterByFinalStatus());
                });

        return migrationStepReports;
    }

    @Override
    public MigrationStatusReport getMigrationStatusOfCustomWidgetUsed(Previewable previewable) {
        List<MigrationStatusReport> reports = new ArrayList<>();
        repository.getByIds(widgetIdVisitor.visit(previewable))
                .forEach(widget -> reports.add(this.getStatus(widget)));

        var migration = false;
        for (var report : reports) {
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
