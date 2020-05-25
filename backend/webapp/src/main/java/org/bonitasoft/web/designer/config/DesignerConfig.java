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
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.export.Exporter;
import org.bonitasoft.web.designer.controller.export.steps.AssetExportStep;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.export.steps.HtmlExportStep;
import org.bonitasoft.web.designer.controller.export.steps.PagePropertiesExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetByIdExportStep;
import org.bonitasoft.web.designer.controller.export.steps.WidgetsExportStep;
import org.bonitasoft.web.designer.controller.importer.ArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.migration.JacksonDeserializationProblemHandler;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.repository.*;
import org.bonitasoft.web.designer.service.BondsTypesFixer;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.AuthRulesCollector;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VariableModelVisitor;
import org.bonitasoft.web.designer.visitor.EmptyPageFactory;
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
@Conditional(DesignerConfigConditional.class)
@EnableScheduling
public class DesignerConfig {

    @Bean
    public Class[] jacksonSubTypes() {
        return new Class[]{Component.class, Container.class, FormContainer.class,  TabsContainer.class, TabContainer.class, ModalContainer.class};
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
    public JsonFileBasedLoader<Page> pageFileBasedLoader() {
        return new JsonFileBasedLoader<>(objectMapperWrapper(), Page.class);
    }

    @Bean
    public JsonFileBasedLoader<Widget> widgetFileBasedLoader() {
        return new WidgetFileBasedLoader(objectMapperWrapper());
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
    public ArtifactImporter<Page> pageImporter(PageRepository pageRepository, PageService pageService,
                                               WidgetImporter widgetImporter,
                                               AssetImporter<Page> pageAssetImporter) {
        return new ArtifactImporter<>(pageRepository, pageService, pageFileBasedLoader(), widgetImporter, pageAssetImporter);
    }

    @Bean
    public ArtifactImporter<Widget> widgetImporter(WidgetFileBasedLoader widgetLoader, WidgetRepository widgetRepository,
                                                   WidgetService widgetService,
                                                   AssetImporter<Widget> widgetAssetImporter) {
        return new ArtifactImporter<>(widgetRepository, widgetService, widgetLoader, widgetAssetImporter);
    }

    @Bean
    public Map<String, ArtifactImporter> artifactImporters(ArtifactImporter<Page> pageImporter,
                                                           ArtifactImporter<Widget> widgetImporter) {
        return ImmutableMap.<String, ArtifactImporter>builder()
                .put("page", pageImporter)
                .put("widget", widgetImporter)
                .build();
    }

    @Bean
    public WidgetsExportStep<Page> widgetsExportStep(WorkspacePathResolver pathResolver, WidgetIdVisitor widgetIdVisitor, DirectiveFileGenerator directiveFileGenerator) {
        return new WidgetsExportStep<Page>(pathResolver, widgetIdVisitor, directiveFileGenerator);
    }

    @Bean
    public ExportStep<Page>[] pageExportSteps(WidgetsExportStep<Page> widgetsExportStep,
                                              PagePropertiesExportStep pagePropertiesExportStep,
                                              HtmlExportStep htmlExportStep, AssetExportStep assetExportStep) {
        return new ExportStep[]{htmlExportStep, widgetsExportStep, pagePropertiesExportStep, assetExportStep};
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
    public AssetVisitor assetVisitor(WidgetRepository widgetRepository) {
        return new AssetVisitor(widgetRepository);
    }

    @Bean
    public WidgetIdVisitor widgetIdVisitor() {
        return new WidgetIdVisitor();
    }

    @Bean
    public ComponentVisitor componentVisitor() {
        return new ComponentVisitor();
    }

    @Bean
    public VariableModelVisitor variableModelVisitor() {
        return new VariableModelVisitor();
    }

    @Bean
    public PageFactory modelPropertiesFactory() {
        return new EmptyPageFactory("modelProperties");
    }

    @Bean
    public RequiredModulesVisitor requiredModulesVisitor(WidgetRepository widgetRepository) {
        return new RequiredModulesVisitor(widgetRepository);
    }

    @Bean
    public HtmlBuilderVisitor htmlBuilderVisitor(List<PageFactory> pageFactories,
                                                 RequiredModulesVisitor requiredModulesVisitor,
                                                 DirectivesCollector directivesCollector, AssetVisitor assetVisitor, PageRepository pageRepository,
                                                 WidgetRepository widgetRepository) {
        return new HtmlBuilderVisitor(pageFactories, requiredModulesVisitor, assetVisitor, directivesCollector,
                pageAssetRepository(pageRepository), widgetAssetRepository(widgetRepository));
    }

    @Bean
    public DirectiveFileGenerator directiveFileGenerator(WorkspacePathResolver pathResolver,
                                                         WidgetRepository widgetRepository,
                                                         WidgetIdVisitor widgetIdVisitor) {
        return new DirectiveFileGenerator(pathResolver, widgetRepository, widgetIdVisitor);
    }

    @Bean
    public PropertyValuesVisitor propertyValuesVisitor() {
        return new PropertyValuesVisitor();
    }

    @Bean
    public AuthRulesCollector authRulesCollector(WidgetRepository widgetRepository) {
        return new AuthRulesCollector(widgetRepository);
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
    public List<LiveRepositoryUpdate> liveRepositoriesUpdate(LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate,
                                                           LiveRepositoryUpdate<Widget> widgetRepositoryLiveUpdate) {
        return Lists.<LiveRepositoryUpdate>newArrayList(pageRepositoryLiveUpdate, widgetRepositoryLiveUpdate);
    }

    @Bean
    public VisitorFactory visitorFactory() {
        return new VisitorFactory();
    }

    @Bean
    public BondsTypesFixer<Page> pageBondsTypesFixer(PageRepository pageRepository) {
        return new BondsTypesFixer<>(pageRepository);
    }
}
