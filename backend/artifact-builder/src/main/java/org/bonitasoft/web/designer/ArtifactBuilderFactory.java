/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer;

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.export.properties.FragmentPropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.properties.PagePropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.properties.WidgetPropertiesBuilder;
import org.bonitasoft.web.designer.controller.export.steps.AssetExportStep;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.export.steps.FragmentPropertiesExportStep;
import org.bonitasoft.web.designer.controller.export.steps.FragmentsExportStep;
import org.bonitasoft.web.designer.controller.export.steps.HtmlExportStep;
import org.bonitasoft.web.designer.controller.export.steps.PagePropertiesExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetByIdExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetsExportStep;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.FragmentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetDependencyImporter;
import org.bonitasoft.web.designer.i18n.I18nInitializer;
import org.bonitasoft.web.designer.i18n.LanguagePackBuilder;
import org.bonitasoft.web.designer.i18n.LanguagePackFactory;
import org.bonitasoft.web.designer.localization.LocalizationFactory;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.designer.visitor.ModelPropertiesVisitor;
import org.bonitasoft.web.designer.visitor.PageFactory;
import org.bonitasoft.web.designer.visitor.PropertyValuesVisitor;
import org.bonitasoft.web.designer.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.designer.visitor.VariableModelVisitor;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.FragmentDirectiveBuilder;
import org.bonitasoft.web.designer.workspace.HtmlSanitizer;
import org.bonitasoft.web.designer.workspace.ResourcesCopier;
import org.bonitasoft.web.designer.workspace.WidgetDirectiveBuilder;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.fedorahosted.tennera.jgettext.PoParser;

import java.util.List;

/**
 * @author Julien Mege
 */
@RequiredArgsConstructor
public class ArtifactBuilderFactory {

    private final UiDesignerProperties uiDesignerProperties;
    private final JsonHandler jsonHandler;
    private final UiDesignerCore core;

    public ArtifactBuilderFactory(UiDesignerProperties uiDesignerProperties) {
        this.uiDesignerProperties = uiDesignerProperties;
        this.jsonHandler = new JsonHandlerFactory().create();
        this.core = new UiDesignerCoreFactory(this.uiDesignerProperties, this.jsonHandler).create();
    }

    /**
     * Factory method for an instance of {@link ArtifactBuilder}
     * @return
     */
    public ArtifactBuilder create() {

        var fragmentIdVisitor = new FragmentIdVisitor(core.getFragmentRepository());
        var widgetIdVisitor = new WidgetIdVisitor(core.getFragmentRepository());

        // == Export
        List<PageFactory> pageFactories = List.of(
                new LocalizationFactory(core.getPageRepository()),
                new ModelPropertiesVisitor(core.getFragmentRepository()),
                new PropertyValuesVisitor(core.getFragmentRepository()),
                new VariableModelVisitor(core.getFragmentRepository())
        );

        var directiveFileGenerator = new DirectiveFileGenerator(uiDesignerProperties.getWorkspace(), core.getWidgetRepository(), widgetIdVisitor);

        var htmlBuilderVisitor = new HtmlBuilderVisitor(
                new AssetVisitor(core.getWidgetRepository(), core.getFragmentRepository()),
                pageFactories,
                new RequiredModulesVisitor(core.getWidgetRepository(), core.getFragmentRepository()),
                new DirectivesCollector(this.jsonHandler, uiDesignerProperties.getWorkspaceUid(),
                        directiveFileGenerator,
                        fragmentIdVisitor,
                        core.getFragmentRepository()
                ),
                core.getPageAssetRepository(),
                core.getWidgetAssetRepository(),
                core.getFragmentRepository()
        );

        //Page
        var htmlGenerator = new HtmlGenerator(htmlBuilderVisitor);

        var pageExportSteps = new ExportStep[]{
                new HtmlExportStep(htmlGenerator, uiDesignerProperties.getWorkspaceUid()),
                new PagePropertiesExportStep(new PagePropertiesBuilder(uiDesignerProperties, core.getPageService())),
                new WidgetsExportStep<Page>(uiDesignerProperties.getWorkspace().getWidgets().getDir(), widgetIdVisitor, directiveFileGenerator),
                new AssetExportStep(core.getPageAssetRepository()),
                new FragmentsExportStep<Page>(fragmentIdVisitor, uiDesignerProperties.getWorkspace().getFragments().getDir())
        };

        //Fragment
        var fragmentExportSteps = new ExportStep[]{
                new WidgetsExportStep<Fragment>(uiDesignerProperties.getWorkspace().getWidgets().getDir(), widgetIdVisitor, directiveFileGenerator),
                new FragmentsExportStep<Fragment>(fragmentIdVisitor, uiDesignerProperties.getWorkspace().getFragments().getDir()),
                new FragmentPropertiesExportStep(new FragmentPropertiesBuilder(uiDesignerProperties))
        };

        //Widget
        var widgetExportSteps = new ExportStep[]{
                new WidgetByIdExportStep(core.getWidgetRepository(), new WidgetPropertiesBuilder(uiDesignerProperties))
        };

        // == Builder
        var widgetExporter = new WidgetExporter(jsonHandler, core.getWidgetService(), widgetExportSteps);
        var fragmentExporter = new FragmentExporter(jsonHandler, core.getFragmentService(), fragmentExportSteps);
        var pageExporter = new PageExporter(jsonHandler, core.getPageService(), pageExportSteps);


        // Dependency importers
        var widgetAssetDependencyImporter = new AssetDependencyImporter<>(core.getWidgetAssetRepository());
        var fragmentDependencyImporter = new FragmentDependencyImporter(core.getFragmentRepository());
        var widgetDependencyImporter = new WidgetDependencyImporter(core.getWidgetRepository(), widgetAssetDependencyImporter);

        // Widget
        var widgetDependencyImporters = new DependencyImporter<?>[]{
                widgetAssetDependencyImporter
        };
        var widgetImporter = new WidgetImporter(
                jsonHandler,
                core.getWidgetService(),
                core.getWidgetRepository(),
                widgetDependencyImporters
        );

        // Fragment
        var fragmentDependencyImporters = new DependencyImporter<?>[]{
                fragmentDependencyImporter,
                widgetDependencyImporter
        };
        var fragmentImporter = new FragmentImporter(
                jsonHandler,
                core.getFragmentService(),
                core.getFragmentRepository(),
                fragmentDependencyImporters
        );

        // Page
        var pageDependencyImporters = new DependencyImporter<?>[]{
                fragmentDependencyImporter,
                widgetDependencyImporter,
                new AssetDependencyImporter<>(core.getPageAssetRepository())
        };
        var pageImporter = new PageImporter(
                jsonHandler,
                core.getPageService(),
                core.getPageRepository(),
                pageDependencyImporters
        );

        // Init workspace now
        var htmlSanitizer = new HtmlSanitizer();
        var widgetDirectiveBuilder = new WidgetDirectiveBuilder(uiDesignerProperties, core.getWatcher(), new WidgetFileBasedLoader(jsonHandler), htmlSanitizer);
        var fragmentDirectiveBuilder = new FragmentDirectiveBuilder(core.getWatcher(), jsonHandler, htmlBuilderVisitor, htmlSanitizer);

        var resourcesCopier = new ResourcesCopier();
        var workspace = new Workspace(
                uiDesignerProperties,
                core.getWidgetRepository(),
                core.getPageRepository(),
                widgetDirectiveBuilder, fragmentDirectiveBuilder,
                core.getWidgetAssetDependencyImporter(),
                resourcesCopier,
                List.of(
                        new LiveRepositoryUpdate<>(core.getPageRepository(), core.getPageMigrationStepsList()),
                        new LiveRepositoryUpdate<>(core.getFragmentRepository(), core.getFragmentMigrationStepsList()),
                        new LiveRepositoryUpdate<>(core.getWidgetRepository(), core.getWidgetMigrationStepsList())
                ),
                jsonHandler
        );
        workspace.initialize();

        var i18nInitializer = new I18nInitializer(
                new LanguagePackBuilder(
                        core.getWatcher(),
                        new LanguagePackFactory(new PoParser(), jsonHandler),
                        uiDesignerProperties.getWorkspaceUid()
                ),
                resourcesCopier,
                uiDesignerProperties.getWorkspaceUid()
        );
        i18nInitializer.initialize();

        return new AngularJsArtifactBuilder(
                // Workspace management
                workspace,
                core.getWidgetService(),
                core.getFragmentService(),
                core.getPageService(),
                // Export
                pageExporter,
                fragmentExporter,
                widgetExporter,
                htmlGenerator,
                // Import
                new ImportStore(),
                pageImporter,
                fragmentImporter,
                widgetImporter
        );
    }

}
