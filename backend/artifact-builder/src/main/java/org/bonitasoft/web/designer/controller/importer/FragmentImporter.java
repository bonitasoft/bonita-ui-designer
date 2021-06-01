package org.bonitasoft.web.designer.controller.importer;

import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.service.FragmentService;

public class FragmentImporter extends AbstractArtifactImporter<Fragment> {
    public FragmentImporter(JsonHandler jsonHandler, FragmentService fragmentService, FragmentRepository fragmentRepository, DependencyImporter... dependencyImporters) {
        super(jsonHandler, fragmentService, fragmentRepository, dependencyImporters);
    }

    @Override
    protected Class<Fragment> getArtifactType() {
        return Fragment.class;
    }
}
