package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.service.PageService;

public class PageExporter extends Exporter<Page> {

    @SafeVarargs
    public PageExporter(JsonHandler jsonHandler, PageService pageService, ExportStep<Page>... exportSteps) {
        super(jsonHandler, pageService, exportSteps);
    }

    @Override
    protected String getComponentType() {
        return "page";
    }
}
