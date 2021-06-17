package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;

import java.util.List;

public interface WidgetService extends AssetableArtifactService<Widget> {
    Widget create(Widget widget);

    Widget createFrom(String sourceWidgetId, Widget widget);

    Widget save(String widgetId, Widget widget);

    List<Property> addProperty(String widgetId, Property property);

    List<Property> updateProperty(String widgetId, String propertyName, Property property);

    List<Property> deleteProperty(String widgetId, String propertyName);

    Widget get(String id);

    Widget getWithAsset(String id);

    List<Widget> getAll();

    List<Widget> getAllWithUsedBy();

    void delete(String id);

    List<MigrationStepReport> migrateAllCustomWidgetUsedInPreviewable(Previewable previewable);

    MigrationStatusReport getMigrationStatusOfCustomWidgetUsed(Previewable previewable);
}
