package org.bonitasoft.web.designer.controller.importer;

import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.service.PageService;

public class PageImporter extends AbstractArtifactImporter<Page> {
    public PageImporter(JsonHandler jsonHandler, PageService pageService, PageRepository pageRepository, DependencyImporter... dependencyImporters) {
        super(jsonHandler, pageService, pageRepository, dependencyImporters);
    }

    @Override
    protected Class<Page> getArtifactType() {
        return Page.class;
    }
}
