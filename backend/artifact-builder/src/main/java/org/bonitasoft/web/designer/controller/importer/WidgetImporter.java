package org.bonitasoft.web.designer.controller.importer;

import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.WidgetService;

public class WidgetImporter extends AbstractArtifactImporter<Widget> {
    public WidgetImporter(JsonHandler jsonHandler, WidgetService widgetService, WidgetRepository widgetRepository, DependencyImporter... dependencyImporters) {
        super(jsonHandler, widgetService, widgetRepository, dependencyImporters);
    }

    @Override
    protected Class<Widget> getArtifactType() {
        return Widget.class;
    }
}
