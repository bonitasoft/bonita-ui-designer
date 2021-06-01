package org.bonitasoft.web.designer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UiDesignerCore {

    private final Watcher watcher;
    private final PageRepository pageRepository;
    private final AssetRepository<Page> pageAssetRepository;
    private final PageService pageService;
    private final AssetService<Page> pageAssetService;

    private final FragmentRepository fragmentRepository;
    private final FragmentService fragmentService;

    private final WidgetRepository widgetRepository;
    private final AssetRepository<Widget> widgetAssetRepository;
    private final WidgetService widgetService;
    private final AssetService<Widget> widgetAssetService;

    private final List<Migration<Page>> pageMigrationStepsList;
    private final List<Migration<Fragment>> fragmentMigrationStepsList;
    private final List<Migration<Widget>> widgetMigrationStepsList;
    private final AssetDependencyImporter<Widget> widgetAssetDependencyImporter;
}
