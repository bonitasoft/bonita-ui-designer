package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.WidgetService;

public class WidgetExporter extends Exporter<Widget> {

    @SafeVarargs
    public WidgetExporter(JsonHandler jsonHandler, WidgetService service, ExportStep<Widget>... exportSteps) {
        super(jsonHandler, service, exportSteps);
    }

    @Override
    protected String getComponentType() {
        return "widget";
    }
}
