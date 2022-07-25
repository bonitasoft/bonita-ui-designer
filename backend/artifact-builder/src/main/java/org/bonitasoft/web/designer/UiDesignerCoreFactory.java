package org.bonitasoft.web.designer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.livebuild.ObserverFactory;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.AddModelVersionMigrationStep;
import org.bonitasoft.web.designer.migration.AssetExternalMigrationStep;
import org.bonitasoft.web.designer.migration.AssetIdMigrationStep;
import org.bonitasoft.web.designer.migration.DataExposedMigrationStep;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.SplitWidgetResourcesMigrationStep;
import org.bonitasoft.web.designer.migration.StyleAddModalContainerPropertiesMigrationStep;
import org.bonitasoft.web.designer.migration.StyleAssetMigrationStep;
import org.bonitasoft.web.designer.migration.StyleUpdateInputRequiredLabelMigrationStep;
import org.bonitasoft.web.designer.migration.page.AutocompleteWidgetReturnedKeyMigrationStep;
import org.bonitasoft.web.designer.migration.page.BondMigrationStep;
import org.bonitasoft.web.designer.migration.page.BusinessVariableMigrationStep;
import org.bonitasoft.web.designer.migration.page.DataToVariableMigrationStep;
import org.bonitasoft.web.designer.migration.page.DynamicTabsContainerMigrationStep;
import org.bonitasoft.web.designer.migration.page.PageUUIDMigrationStep;
import org.bonitasoft.web.designer.migration.page.SetInterpretHtmlTrueMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TableWidgetStylesMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetInterpretHTMLMigrationStep;
import org.bonitasoft.web.designer.migration.page.TextWidgetLabelMigrationStep;
import org.bonitasoft.web.designer.migration.page.UIBootstrapAssetMigrationStep;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetFileBasedPersister;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.service.BondsTypesFixer;
import org.bonitasoft.web.designer.service.DefaultFragmentService;
import org.bonitasoft.web.designer.service.DefaultPageService;
import org.bonitasoft.web.designer.service.DefaultWidgetService;
import org.bonitasoft.web.designer.service.FragmentMigrationApplyer;
import org.bonitasoft.web.designer.service.PageMigrationApplyer;
import org.bonitasoft.web.designer.service.WidgetMigrationApplyer;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.FragmentChangeVisitor;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.visitor.PageHasValidationErrorVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;

import javax.validation.Validation;
import java.util.List;

import static org.bonitasoft.web.designer.migration.Version.INITIAL_MODEL_VERSION;

@Getter
@Slf4j
public class UiDesignerCoreFactory {

    private final UiDesignerProperties uiDesignerProperties;
    private final JsonHandler jsonHandler;
    private final BeanValidator beanValidator;


    public UiDesignerCoreFactory(UiDesignerProperties uiDesignerProperties, JsonHandler jsonHandler) {
        this.uiDesignerProperties = uiDesignerProperties;
        this.jsonHandler = jsonHandler;
        this.beanValidator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
    }

    /**
     * Use this method to create a UID core object holding references to core services
     *
     * @return
     */
    public UiDesignerCore create() {

        var watcher = createWatcher(createFileMonitor(false));

        // == Widget
        var widgetRepository = createWidgetRepository(watcher);
        var widgetAssetRepository = createWidgetAssetRepository(widgetRepository);

        // == Fragment
        var fragmentRepository = createFragmentRepository(watcher);

        // == Page
        var pageRepository = createPageRepository(watcher);
        var pageAssetRepository = createPageAssetRepository(pageRepository);

        return create(watcher, widgetRepository, widgetAssetRepository, fragmentRepository, pageRepository, pageAssetRepository);

    }

    /**
     * Use this method to create a UID core object holding references to core services using externally managed beans (like in a spring context)
     *
     * @param watcher
     * @param widgetRepository
     * @param widgetAssetRepository
     * @param fragmentRepository
     * @param pageRepository
     * @param pageAssetRepository
     * @return
     */
    public UiDesignerCore create(
            Watcher watcher,
            WidgetRepository widgetRepository,
            AssetRepository<Widget> widgetAssetRepository,
            FragmentRepository fragmentRepository,
            PageRepository pageRepository,
            AssetRepository<Page> pageAssetRepository
    ) {

        var pageAssetDependencyImporter = new AssetDependencyImporter<>(pageAssetRepository);
        var pageAssetService = new AssetService<>(pageRepository, pageAssetRepository, pageAssetDependencyImporter);

        final var componentVisitor = new ComponentVisitor(fragmentRepository);
        List<Migration<Fragment>> fragmentMigrationStepsList = List.of(
                new Migration<>("1.0.3", new BondMigrationStep<>(componentVisitor, widgetRepository, new VisitorFactory())),
                new Migration<>("1.7.25", new TextWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.9.24", new TextWidgetLabelMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.12", new DataToVariableMigrationStep<>()),
                new Migration<>("1.10.16", new TableWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.18", new TableWidgetStylesMigrationStep<>(componentVisitor)),
                new Migration<>("1.11.46", new DataExposedMigrationStep<>()),
                new Migration<Fragment>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<>("INITIAL_MODEL_VERSION")),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1"), new AutocompleteWidgetReturnedKeyMigrationStep<>(componentVisitor)),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2"), new BusinessVariableMigrationStep<>()),
                new Migration<>("2.3", new SetInterpretHtmlTrueMigrationStep<>(componentVisitor))
        );

        List<Migration<Page>> pageMigrationStepsList = List.of(
                new Migration<>("1.0.2", new AssetIdMigrationStep<>()),
                new Migration<>("1.0.3", new BondMigrationStep<>(componentVisitor, widgetRepository, new VisitorFactory())),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<>()),
                new Migration<>("1.5.7", new StyleAssetMigrationStep(uiDesignerProperties, pageAssetService)),
                new Migration<>("1.5.10", new UIBootstrapAssetMigrationStep(pageAssetService, componentVisitor, widgetRepository)),
                new Migration<>("1.7.4", new TextWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.7.25", new PageUUIDMigrationStep()),
                new Migration<>("1.8.29", new StyleAddModalContainerPropertiesMigrationStep(pageAssetService)),
                new Migration<>("1.9.24", new TextWidgetLabelMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.5", new DynamicTabsContainerMigrationStep<>()),
                new Migration<>("1.10.12", new DataToVariableMigrationStep<>()),
                new Migration<>("1.10.16", new TableWidgetInterpretHTMLMigrationStep<>(componentVisitor)),
                new Migration<>("1.10.18", new TableWidgetStylesMigrationStep<>(componentVisitor)),
                new Migration<>("1.11.40", new BusinessVariableMigrationStep<>()),
                new Migration<>("1.11.46", new StyleUpdateInputRequiredLabelMigrationStep(pageAssetService)),
                new Migration<>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<>(INITIAL_MODEL_VERSION), new AutocompleteWidgetReturnedKeyMigrationStep<Page>(componentVisitor)),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1")),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2")),
                new Migration<>("2.3", new SetInterpretHtmlTrueMigrationStep<>(componentVisitor))
        );

        List<Migration<Widget>> widgetMigrationStepsList = List.of(
                new Migration<>("1.0.2", new AssetIdMigrationStep<>()),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<>()),
                new Migration<>("1.10.12", new SplitWidgetResourcesMigrationStep()),
                new Migration<>(INITIAL_MODEL_VERSION, new AddModelVersionMigrationStep<>(INITIAL_MODEL_VERSION)),
                new Migration<>("2.1", new AddModelVersionMigrationStep<>("2.1")),
                new Migration<>("2.2", new AddModelVersionMigrationStep<>("2.2")),
                new Migration<>("2.3", new AddModelVersionMigrationStep<>("2.3"))
        );

        var widgetMigrationApplyer = new WidgetMigrationApplyer(widgetMigrationStepsList);
        var widgetIdVisitor = new WidgetIdVisitor(fragmentRepository);
        var assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        var widgetAssetDependencyImporter = new AssetDependencyImporter<>(widgetAssetRepository);
        var widgetAssetService = new AssetService<>(widgetRepository, widgetAssetRepository, widgetAssetDependencyImporter);
        var widgetService = new DefaultWidgetService(
                widgetRepository,
                pageRepository,
                fragmentRepository,
                List.of(
                        new BondsTypesFixer<>(pageRepository),
                        new BondsTypesFixer<>(fragmentRepository)
                ),
                widgetMigrationApplyer,
                widgetIdVisitor,
                assetVisitor,
                uiDesignerProperties,
                widgetAssetService);

        var fragmentMigrationApplyer = new FragmentMigrationApplyer(fragmentMigrationStepsList, widgetService);
        var fragmentService = new DefaultFragmentService(
                fragmentRepository,
                pageRepository,
                fragmentMigrationApplyer,
                new FragmentIdVisitor(fragmentRepository),
                new FragmentChangeVisitor(),
                new PageHasValidationErrorVisitor(),
                assetVisitor,
                uiDesignerProperties);

        var pageMigrationApplyer = new PageMigrationApplyer(pageMigrationStepsList, widgetService, fragmentService);
        var pageService = new DefaultPageService(
                pageRepository,
                pageMigrationApplyer,
                new ComponentVisitor(fragmentRepository),
                assetVisitor,
                new FragmentIdVisitor(fragmentRepository),
                fragmentService,
                uiDesignerProperties,
                pageAssetService
        );

        // Return core services
        return new UiDesignerCore(
                watcher,

                pageRepository,
                pageAssetRepository,
                pageService,
                pageAssetService,

                fragmentRepository,
                fragmentService,

                widgetRepository,
                widgetAssetRepository,
                widgetService,
                widgetAssetService,

                // TODO: should not be visible outside of services, but forced to because of workspace init
                pageMigrationStepsList,
                fragmentMigrationStepsList,
                widgetMigrationStepsList,
                widgetAssetDependencyImporter
        );
    }

    /**
     * Factory method for a new instance of apache common file monitor
     *
     * @param managed, <p>Set to true if start/stop method are called externaly (ex: managed by a spring context).
     *                 If set to false, the {@link FileAlterationMonitor#start()} method will be called now and {@link FileAlterationMonitor#stop()} method call
     *                 will happen on jvm exit via a shutdown hook
     *                 </p>
     * @return a new instance of apache common file monitor
     */
    public FileAlterationMonitor createFileMonitor(boolean managed) {
        var monitor = new FileAlterationMonitor(1000);

        if (!managed) {
            try {
                monitor.start();
            } catch (Exception e) {
                throw new ArtifactBuilderException("Failed to start FileAlterationMonitor", e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    monitor.stop();
                } catch (Exception e) {
                    log.warn("Failed to cleanly stop FileAlterationMonitor on shutdown", e);
                }
            }));
        }
        return monitor;
    }

    /**
     * Factory method for a Watcher
     *
     * @param monitor
     * @return
     */
    public Watcher createWatcher(FileAlterationMonitor monitor) {
        return new Watcher(new ObserverFactory(), monitor);
    }

    /**
     * Factory method for a page Asset Repository
     *
     * @param pageRepository
     * @return
     */
    public AssetRepository<Page> createPageAssetRepository(PageRepository pageRepository) {
        return new AssetRepository<>(pageRepository, beanValidator);
    }

    /**
     * Factory method for a Page Repository
     *
     * @param watcher
     * @return
     */
    public PageRepository createPageRepository(Watcher watcher) {
        return new PageRepository(
                uiDesignerProperties.getWorkspace(),
                uiDesignerProperties.getWorkspaceUid(),
                new JsonFileBasedPersister<>(jsonHandler, beanValidator, this.uiDesignerProperties),
                new JsonFileBasedLoader<>(jsonHandler, Page.class),
                beanValidator, watcher
        );
    }

    /**
     * Factory method for a Fragment Repository
     *
     * @param watcher
     * @return
     */
    public FragmentRepository createFragmentRepository(Watcher watcher) {
        return new FragmentRepository(
                uiDesignerProperties.getWorkspace(),
                uiDesignerProperties.getWorkspaceUid(),
                new JsonFileBasedPersister<>(jsonHandler, beanValidator, this.uiDesignerProperties),
                new JsonFileBasedLoader<>(jsonHandler, Fragment.class),
                beanValidator, watcher
        );
    }

    /**
     * Factory method for a widget Asset Repository
     *
     * @param widgetRepository
     * @return
     */
    public AssetRepository<Widget> createWidgetAssetRepository(WidgetRepository widgetRepository) {
        return new AssetRepository<>(widgetRepository, beanValidator);
    }

    /**
     * Factory method for a Widget Repository
     *
     * @param watcher
     * @return
     */
    public WidgetRepository createWidgetRepository(Watcher watcher) {
        return new WidgetRepository(
                uiDesignerProperties.getWorkspace(),
                uiDesignerProperties.getWorkspaceUid(),
                new WidgetFileBasedPersister(jsonHandler, beanValidator, this.uiDesignerProperties),
                new WidgetFileBasedLoader(jsonHandler),
                beanValidator, watcher, uiDesignerProperties
        );
    }

}
