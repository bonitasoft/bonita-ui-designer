package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.service.FragmentService;

public class FragmentExporter extends Exporter<Fragment> {

    @SafeVarargs
    public FragmentExporter(JsonHandler jsonHandler, FragmentService service, ExportStep<Fragment>... exportSteps) {
        super(jsonHandler, service, exportSteps);
    }

    @Override
    protected String getComponentType() {
        return "fragment";
    }
}
