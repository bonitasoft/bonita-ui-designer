package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.service.PageService;

import java.util.List;

public class PageExporter extends Exporter<Page> {

    @SafeVarargs
    public PageExporter(JsonHandler jsonHandler, PageService pageService, ExportStep<Page>[] exportSteps, ExportStep<Page>... angularExportSteps) {
        super(jsonHandler, pageService, exportSteps, angularExportSteps);
    }

    @Override
    protected String getComponentType() {
        return "page";
    }
}
