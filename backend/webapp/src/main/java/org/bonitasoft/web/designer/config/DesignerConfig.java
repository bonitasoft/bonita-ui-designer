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
package org.bonitasoft.web.designer.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.collect.ObjectArrays;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.export.Exporter;
import org.bonitasoft.web.designer.controller.export.steps.AssetExportStep;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.export.steps.FragmentPropertiesExportStep;
import org.bonitasoft.web.designer.controller.export.steps.FragmentsExportStep;
import org.bonitasoft.web.designer.controller.export.steps.HtmlExportStep;
import org.bonitasoft.web.designer.controller.export.steps.PagePropertiesExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetByIdExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetsExportStep;
import org.bonitasoft.web.designer.controller.importer.ArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.migration.JacksonDeserializationProblemHandler;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.repository.*;
import org.bonitasoft.web.designer.service.BondsTypesFixer;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.AuthRulesCollector;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.FragmentChangeVisitor;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.visitor.ModelPropertiesVisitor;
import org.bonitasoft.web.designer.visitor.PageHasValidationErrorVisitor;
import org.bonitasoft.web.designer.visitor.VariableModelVisitor;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;
import org.bonitasoft.web.designer.visitor.PageFactory;
import org.bonitasoft.web.designer.visitor.PropertyValuesVisitor;
import org.bonitasoft.web.designer.visitor.RequiredModulesVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Guillaume EHRET
 */
@Configuration
@EnableScheduling
public class DesignerConfig {

    @Bean
    public Class[] jacksonSubTypes() {
        return new Class[]{FragmentElement.class, Component.class, Container.class, FormContainer.class,  TabsContainer.class, TabContainer.class, ModalContainer.class};
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //By default all properties without explicit view definition are included in serialization.
        //To use JsonView we have to change this parameter
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

        //We don't have to serialize null values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new JodaModule());
        objectMapper.registerSubtypes(jacksonSubTypes());

        //add Handler to migrate old json
        objectMapper.addHandler(new JacksonDeserializationProblemHandler());

        //disable filter name check so that filtering is optional
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.setFailOnUnknownId(false);
        objectMapper.setFilters(simpleFilterProvider);

        return objectMapper;
    }

    /**
     * We use our own jackson object Mapper
     */
    @Bean
    public JacksonObjectMapper objectMapperWrapper() {
        return new JacksonObjectMapper(objectMapper());
    }

    /**
     * Used by internationalisation to work on PO files
     */
    @Bean
    public PoParser poParser() {
        return new PoParser();
    }

    @Bean
    public BeanValidator beanValidator() {
        //For the bean Widget the
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return new BeanValidator(validator);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public JsonFileBasedPersister<Page> pageFileBasedPersister() {
        return new JsonFileBasedPersister<>(objectMapperWrapper(), beanValidator());
    }

    @Bean
    public JsonFileBasedPersister<Widget> widgetFileBasedPersister() {
        return new WidgetFileBasedPersister(objectMapperWrapper(), beanValidator());
    }

    @Bean
    public JsonFileBasedPersister<Fragment> fragmentFileBasedPersister() {
        return new JsonFileBasedPersister<>(objectMapperWrapper(), beanValidator());
    }

    @Bean
    public JsonFileBasedLoader<Page> pageFileBasedLoader() {
        return new JsonFileBasedLoader<>(objectMapperWrapper(), Page.class);
    }

    @Bean
    public JsonFileBasedLoader<Widget> widgetFileBasedLoader() {
        return new WidgetFileBasedLoader(objectMapperWrapper());
    }

    @Bean
    public JsonFileBasedLoader<Fragment> fragmentFileBasedLoader() {
        return new JsonFileBasedLoader<>(objectMapperWrapper(), Fragment.class);
    }

    @Bean
    public Path widgetPath(WorkspacePathResolver workspacePathResolver) {
        return workspacePathResolver.getWidgetsRepositoryPath();
    }

    @Bean
    public Path pagesPath(WorkspacePathResolver workspacePathResolver) {
        return workspacePathResolver.getPagesRepositoryPath();
    }

    @Bean
    public WidgetImporter widgetElementImporter(WidgetFileBasedLoader widgetLoader, WidgetRepository widgetRepository,
                                                AssetImporter<Widget> widgetAssetImporter) {
        return new WidgetImporter(widgetLoader, widgetRepository, widgetAssetImporter);
    }

    @Bean
    public AssetImporter<Page> pageAssetImporter(AssetRepository<Page> pageAssetRepository) {
        return new AssetImporter<>(pageAssetRepository);
    }

    @Bean
    public AssetImporter<Widget> widgetAssetImporter(AssetRepository<Widget> widgetAssetRepository) {
        return new AssetImporter<>(widgetAssetRepository);
    }

    @Bean
    public ArtifactImporter<Page> pageImporter(PageRepository pageRepository, PageService pageService, FragmentImporter fragmentImporter,
                                               WidgetImporter widgetImporter, AssetImporter<Page> pageAssetImporter) {
        return new ArtifactImporter<>(pageRepository, pageService, pageFileBasedLoader(), fragmentImporter, widgetImporter, pageAssetImporter);
    }

    @Bean
    public ArtifactImporter<Widget> widgetImporter(WidgetFileBasedLoader widgetLoader, WidgetRepository widgetRepository,
                                                   WidgetService widgetService,
                                                   AssetImporter<Widget> widgetAssetImporter) {
        return new ArtifactImporter<>(widgetRepository, widgetService, widgetLoader, widgetAssetImporter);
    }

    @Bean
    public FragmentImporter fragmentElementImporter(FragmentRepository fragmentRepository) {
        return new FragmentImporter(fragmentFileBasedLoader(), fragmentRepository);
    }

    @Bean
    public Map<String, ArtifactImporter> artifactImporters(ArtifactImporter<Page> pageImporter,
                                                           ArtifactImporter<Widget> widgetImporter,
                                                           ArtifactImporter<Fragment> fragmentImporter) {
        return ImmutableMap.<String, ArtifactImporter>builder()
                .put("page", pageImporter)
                .put("widget", widgetImporter)
                .put("fragment", fragmentImporter)
                .build();
    }

    @Bean
    public WidgetsExportStep<Page> widgetsExportStep(WorkspacePathResolver pathResolver, WidgetIdVisitor widgetIdVisitor, DirectiveFileGenerator directiveFileGenerator) {
        return new WidgetsExportStep<Page>(pathResolver, widgetIdVisitor, directiveFileGenerator);
    }

    @Bean
    public ExportStep<Page>[] pageExportSteps(FragmentsExportStep<Page> fragmentsExportStep, WidgetsExportStep<Page> widgetsExportStep,
                                              PagePropertiesExportStep pagePropertiesExportStep, HtmlExportStep htmlExportStep,
                                              AssetExportStep assetExportStep) {
        return new ExportStep[]{htmlExportStep, widgetsExportStep, pagePropertiesExportStep, assetExportStep,fragmentsExportStep};
    }

    @Bean
    public Exporter<Page> pageExporter(PageRepository pageRepository, PageService pageService, ExportStep<Page>[] pageExportSteps ) {
        return new Exporter(pageRepository, pageService, pageExportSteps);
    }

    @Bean
    public Exporter<Widget> widgetExporter(WidgetRepository widgetRepository, WidgetService widgetService, WidgetByIdExportStep widgetByIdExportStep) {
        return new Exporter(widgetRepository,widgetService, widgetByIdExportStep);
    }

    @Bean
    @Primary
    public AssetVisitor assetVisitor(WidgetRepository widgetRepository, FragmentRepository fragmentRepository) {
        return new AssetVisitor(widgetRepository, fragmentRepository);
    }

    @Bean
    @Primary
    public WidgetIdVisitor widgetIdVisitor(FragmentRepository fragmentRepository) {
        return new WidgetIdVisitor(fragmentRepository);
    }

    @Bean
    @Primary
    public ComponentVisitor componentVisitor(FragmentRepository fragmentRepository) {
        return new ComponentVisitor(fragmentRepository);
    }

    @Bean
    @Primary
    public VariableModelVisitor variableModelVisitor(FragmentRepository fragmentRepository) {
        return new VariableModelVisitor(fragmentRepository);
    }

    @Bean
    public PageFactory modelPropertiesFactory(FragmentRepository fragmentRepository) {
        return new ModelPropertiesVisitor(fragmentRepository);
    }

    @Bean
    @Primary
    public RequiredModulesVisitor requiredModulesVisitor(WidgetRepository widgetRepository, FragmentRepository fragmentRepository) {
        return new RequiredModulesVisitor(widgetRepository, fragmentRepository);
    }

    @Bean
    public HtmlBuilderVisitor htmlBuilderVisitor(FragmentRepository fragmentRepository, List<PageFactory> pageFactories,
                                                 RequiredModulesVisitor requiredModulesVisitor,
                                                 DirectivesCollector directivesCollector, AssetVisitor assetVisitor, PageRepository pageRepository,
                                                 WidgetRepository widgetRepository) {
        return new HtmlBuilderVisitor(fragmentRepository, pageFactories, requiredModulesVisitor, assetVisitor, directivesCollector,
                pageAssetRepository(pageRepository), widgetAssetRepository(widgetRepository));
    }

    @Bean
    public DirectiveFileGenerator directiveFileGenerator(WorkspacePathResolver pathResolver,
                                                         WidgetRepository widgetRepository,
                                                         WidgetIdVisitor widgetIdVisitor) {
        return new DirectiveFileGenerator(pathResolver, widgetRepository, widgetIdVisitor);
    }

    @Bean
    @Primary
    public PropertyValuesVisitor propertyValuesVisitor(FragmentRepository fragmentRepository) {
        return new PropertyValuesVisitor(fragmentRepository);
    }

    @Bean
    public AuthRulesCollector authRulesCollector(WidgetRepository widgetRepository, FragmentRepository fragmentRepository) {
        return new AuthRulesCollector(widgetRepository, fragmentRepository);
    }

    @Bean
    public AssetRepository<Page> pageAssetRepository(PageRepository pageRepository) {
        return new AssetRepository<>(pageRepository, beanValidator());
    }

    @Bean
    public AssetRepository<Widget> widgetAssetRepository(WidgetRepository widgetRepository) {
        return new AssetRepository<>(widgetRepository, beanValidator());
    }

    @Bean
    public AssetService<Page> pageAssetService(PageRepository pageRepository) {
        return new AssetService<>(pageRepository, pageAssetRepository(pageRepository),
                pageAssetImporter(pageAssetRepository(pageRepository)),
                objectMapperWrapper());
    }

    @Bean
    public AssetService<Widget> widgetAssetService(WidgetRepository widgetRepository) {
        return new AssetService<>(widgetRepository, widgetAssetRepository(widgetRepository),
                widgetAssetImporter(widgetAssetRepository(widgetRepository)),
                objectMapperWrapper());
    }

    @Bean
    public List<LiveRepositoryUpdate> liveRepositoriesUpdate(LiveRepositoryUpdate<Page> pageLiveRepositoryUpdate,
                                                             LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate,
                                                             LiveRepositoryUpdate<Fragment> fragmentLiveRepositoryUpdate) {
        return Lists.<LiveRepositoryUpdate>newArrayList(pageLiveRepositoryUpdate, widgetLiveRepositoryUpdate, fragmentLiveRepositoryUpdate);
    }

    @Bean
    public VisitorFactory visitorFactory() {
        return new VisitorFactory();
    }

    @Bean
    public BondsTypesFixer<Page> pageBondsTypesFixer(PageRepository pageRepository) {
        return new BondsTypesFixer<>(pageRepository);
    }

    @Bean
    public Path fragmentsPath(WorkspacePathResolver workspacePathResolver) {
        return workspacePathResolver.getFragmentsRepositoryPath();
    }

    @Bean
    public FragmentChangeVisitor fragmentRenamingVisitor() {
        return new FragmentChangeVisitor();
    }

    @Bean
    public PageHasValidationErrorVisitor pageHasValidationErrorVisitor() {
        return new PageHasValidationErrorVisitor();
    }

    @Bean
    public ArtifactImporter<Fragment> fragmentImporter(FragmentRepository fragmentRepository, FragmentService fragmentService, FragmentImporter fragmentImporter, WidgetImporter widgetImporter) {
        return new ArtifactImporter<>(fragmentRepository, fragmentService, fragmentFileBasedLoader(), fragmentImporter, widgetImporter);
    }

    @Bean
    public FragmentsExportStep<Page> fragmentsExportStep(FragmentIdVisitor fragmentIdVisitor, WorkspacePathResolver workspacePathResolver, FragmentPropertiesExportStep fragmentPropertiesExportStep) {
        return new FragmentsExportStep<>(fragmentIdVisitor, workspacePathResolver, fragmentPropertiesExportStep);
    }

    @Bean
    public WidgetsExportStep<Fragment> widgetsExportStepFragment(WorkspacePathResolver workspacePathResolver, WidgetIdVisitor widgetIdVisitor, DirectiveFileGenerator directiveFileGenerator) {
        return new WidgetsExportStep<>(workspacePathResolver, widgetIdVisitor, directiveFileGenerator);
    }

    @Bean
    public FragmentsExportStep<Fragment> fragmentsExportStepFragment(FragmentIdVisitor fragmentIdVisitor, WorkspacePathResolver workspacePathResolver, FragmentPropertiesExportStep fragmentPropertiesExportStep) {
        return new FragmentsExportStep<>(fragmentIdVisitor, workspacePathResolver, fragmentPropertiesExportStep);
    }

    @Bean
    public Exporter<Fragment> fragmentExporter(FragmentRepository fragmentRepository, FragmentService fragmentService, JacksonObjectMapper objectMapper, WidgetsExportStep<Fragment> widgetsExportStepFragment, FragmentsExportStep<Fragment> fragmentsExportStepFragment) {
        return new Exporter(fragmentRepository, fragmentService, widgetsExportStepFragment, fragmentsExportStepFragment);
    }

    @Bean
    public FragmentIdVisitor fragmentIdVisitor(FragmentRepository fragmentRepository) {
        return new FragmentIdVisitor(fragmentRepository);
    }

    @Bean
    public List<Repository> fragmentsUsedByRepositories(PageRepository pageRepository, FragmentRepository fragmentRepository) {
        return Lists.<Repository>newArrayList(pageRepository, fragmentRepository);
    }

    //TODO : check if we need this after localisation decision
//    @Bean
//    public Manager manager() {
//        return Manager.getInstance();
//    }
//
//    @Bean
//    public FeatureManager featureManager() {
//        return new FeatureManager(manager());
//    }

    @Bean
    public DimensionFactory dimensionFactory() {
        return new DimensionFactory();
    }

    @Bean
    public BondsTypesFixer<Fragment> fragmentBondsTypesFixer(FragmentRepository fragmentRepository) {
        return new BondsTypesFixer<>(fragmentRepository);
    }

}
