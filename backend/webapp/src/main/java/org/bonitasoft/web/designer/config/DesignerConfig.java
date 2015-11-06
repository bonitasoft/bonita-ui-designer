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

import static java.util.Arrays.asList;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.validation.Validation;
import javax.validation.Validator;

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
import org.bonitasoft.web.designer.migration.AssetExternalMigrationStep;
import org.bonitasoft.web.designer.migration.AssetIdMigrationStep;
import org.bonitasoft.web.designer.migration.JacksonDeserializationProblemHandler;
import org.bonitasoft.web.designer.migration.LiveMigration;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.page.BondMigrationStep;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.DataModelVisitor;
import org.bonitasoft.web.designer.visitor.DirectivesCollector;
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
public class DesignerConfig {

    @Bean
    public Class[] jacksonSubTypes() {
        return new Class[] { Component.class, Container.class, FormContainer.class, TabsContainer.class };
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
        return new JsonFileBasedPersister<>(objectMapperWrapper(), beanValidator());
    }

    @Bean
    public JsonFileBasedLoader<Page> pageFileBasedLoader() {
        return new JsonFileBasedLoader<>(objectMapperWrapper(), Page.class);
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
    public WidgetImporter widgetElementImporter(WidgetLoader widgetLoader, WidgetRepository widgetRepository, AssetImporter<Widget> widgetAssetImporter) {
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
    public ArtifactImporter<Page> pageImporter(PageRepository pageRepository, WidgetImporter widgetImporter, AssetImporter<Page> pageAssetImporter) {
        return new ArtifactImporter<>(pageRepository, pageFileBasedLoader(), widgetImporter, pageAssetImporter);
    }

    @Bean
    public ArtifactImporter<Widget> widgetImporter(WidgetLoader widgetLoader, WidgetRepository widgetRepository, AssetImporter<Widget> widgetAssetImporter) {
        return new ArtifactImporter<>(widgetRepository, widgetLoader, widgetAssetImporter);
    }

    @Bean
    public Map<String, ArtifactImporter> artifactImporters(ArtifactImporter<Page> pageImporter, ArtifactImporter<Widget> widgetImporter) {
        return ImmutableMap.<String, ArtifactImporter>builder()
                .put("page", pageImporter)
                .put("widget", widgetImporter)
                .build();
    }

    @Bean
    public ExportStep<Page>[] pageExportSteps(WidgetsExportStep widgetsExportStep, PagePropertiesExportStep pagePropertiesExportStep,
            HtmlExportStep htmlExportStep, AssetExportStep assetExportStep) {
        return new ExportStep[] { htmlExportStep, widgetsExportStep, pagePropertiesExportStep, assetExportStep };
    }

    @Bean
    public Exporter<Page> pageExporter(PageRepository pageRepository, JacksonObjectMapper objectMapper, ExportStep<Page>[] pageExportSteps) {
        return new Exporter(pageRepository, objectMapper, pageExportSteps);
    }

    @Bean
    public Exporter<Widget> widgetExporter(WidgetRepository widgetRepository, JacksonObjectMapper objectMapper, WidgetByIdExportStep widgetByIdExportStep) {
        return new Exporter(widgetRepository, objectMapper, widgetByIdExportStep);
    }

    @Bean
    public List<Repository> widgetsUsedByRepositories(WidgetRepository widgetRepository, PageRepository pageRepository) {
        return Lists.<Repository>newArrayList(pageRepository, widgetRepository);
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
    public DataModelVisitor dataModelVisitor() {
        return new DataModelVisitor();
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
    public HtmlBuilderVisitor htmlBuilderVisitor(List<PageFactory> pageFactories, RequiredModulesVisitor requiredModulesVisitor,
            DirectivesCollector directivesCollector, AssetVisitor assetVisitor) {
        return new HtmlBuilderVisitor(pageFactories, requiredModulesVisitor, directivesCollector, assetVisitor);
    }

    @Bean
    public PropertyValuesVisitor propertyValuesVisitor() {
        return new PropertyValuesVisitor();
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
        return new AssetService<>(pageRepository, pageAssetRepository(pageRepository), pageAssetImporter(pageAssetRepository(pageRepository)),
                objectMapperWrapper());
    }

    @Bean
    public AssetService<Widget> widgetAssetService(WidgetRepository widgetRepository) {
        return new AssetService<>(widgetRepository, widgetAssetRepository(widgetRepository), widgetAssetImporter(widgetAssetRepository(widgetRepository)),
                objectMapperWrapper());
    }

    @Bean
    public LiveMigration<Page> pageLiveMigration(JsonFileBasedLoader<Page> pageFileBasedLoader, PageRepository pageRepository,
            BondMigrationStep bondMigrationStep) {
        return new LiveMigration<>(pageRepository, pageFileBasedLoader, asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Page>()),
                new Migration<>("1.0.3", bondMigrationStep),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Page>())));
    }

    @Bean
    public LiveMigration<Widget> widgetLiveMigration(WidgetLoader widgetLoader, WidgetRepository widgetRepository) {
        return new LiveMigration<>(widgetRepository, widgetLoader, asList(
                new Migration<>("1.0.2", new AssetIdMigrationStep<Widget>()),
                new Migration<>("1.2.9", new AssetExternalMigrationStep<Widget>())));
    }

    @Bean
    public List<LiveMigration> liveMigrations(LiveMigration<Page> pageLiveMigration, LiveMigration<Widget> widgetLiveMigration) {
        return Lists.<LiveMigration>newArrayList(pageLiveMigration, widgetLiveMigration);
    }

    @Bean
    public VisitorFactory visitorFactory() {
        return new VisitorFactory();
    }
}
