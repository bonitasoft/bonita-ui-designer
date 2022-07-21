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
package org.bonitasoft.web.designer.service;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.PageAssetPredicate;
import org.bonitasoft.web.designer.controller.export.properties.BonitaResourceTransformer;
import org.bonitasoft.web.designer.controller.export.properties.BonitaVariableResourcePredicate;
import org.bonitasoft.web.designer.controller.export.properties.ResourceURLFunction;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.exception.IncompatibleException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class DefaultPageService extends AbstractAssetableArtifactService<PageRepository, Page> implements PageService {

    public static final String BONITA_RESOURCE_REGEX = ".+/API/(?!extension)([^ /]*)/([^ /(?|{)]*)[\\S+]*";// matches ..... /API/{}/{}?...

    public static final String EXTENSION_RESOURCE_REGEX = ".+/API/(?=extension)([^ /]*)/([^ (?|{)]*).*";

    private final PageMigrationApplyer pageMigrationApplyer;

    private final ComponentVisitor componentVisitor;

    private final AssetVisitor assetVisitor;

    private final FragmentIdVisitor fragmentIdVisitor;

    private final FragmentService fragmentService;

    public DefaultPageService(PageRepository pageRepository, PageMigrationApplyer pageMigrationApplyer, ComponentVisitor componentVisitor,
                              AssetVisitor assetVisitor, FragmentIdVisitor fragmentIdVisitor, FragmentService fragmentService,
                              UiDesignerProperties uiDesignerProperties, AssetService<Page> pageAssetService) {
        super(uiDesignerProperties, pageAssetService, pageRepository);
        this.pageMigrationApplyer = pageMigrationApplyer;
        this.componentVisitor = componentVisitor;
        this.assetVisitor = assetVisitor;
        this.fragmentIdVisitor = fragmentIdVisitor;
        this.fragmentService = fragmentService;
    }

    @Override
    public Page get(String id) {
        var page = repository.get(id);
        return migrate(page);
    }

    @Override
    public Page getWithAsset(String id) {
        var page = repository.get(id);
        page = migrate(page);
        page.setAssets(assetVisitor.visit(page));
        return page;
    }

    @Override
    public List<Page> getAll() {
        return repository.getAll().stream().map(page -> {
            page.setStatus(getStatus(page));
            return page;
        }).collect(toList());
    }

    @Override
    public Page create(Page page) {
        final var savedPage = doCreate(page);
        // default assets
        assetService.loadDefaultAssets(page);
        return savedPage;
    }

    @Override
    public Page createFrom(String sourcePageId, Page page) {
        final var savedPage = doCreate(page);
        // copy assets
        assetService.duplicateAsset(
                repository.resolvePath(sourcePageId),
                repository.resolvePath(sourcePageId),
                sourcePageId,
                savedPage.getId()
        );
        return savedPage;
    }

    private Page doCreate(Page page) {
        // the page should not have an ID. If it has one, we ignore it and generate one using the name
        String pageId = repository.getNextAvailableId(page.getName());
        page.setId(pageId);
        // the page should not have an UUID. If it has one, we ignore it and generate one
        page.setUUID(UUID.randomUUID().toString());
        page.setAssets(page.getAssets().stream().filter(new PageAssetPredicate()).collect(toSet()));
        return repository.updateLastUpdateAndSave(page);
    }

    @Override
    public Page save(String pageId, Page page) {
        try {
            var existingPage = get(pageId);
            if (!existingPage.isCompatible()) {
                throw new IncompatibleException("Page " + existingPage.getId() + " is in an incompatible version. Newer UI Designer version is required.");
            }

            if (existingPage.getName().equals(page.getName())) {
                // the page should have the same ID as pageId.
                page.setId(existingPage.getId());
            } else {
                page.setId(repository.getNextAvailableId(page.getName()));
            }
        } catch (NotFoundException e) {
            page.setId(repository.getNextAvailableId(page.getName()));
        }

        if (!hasText(page.getUUID())) {
            //it is a new page so we set its UUID
            page.setUUID(UUID.randomUUID().toString());
        }

        page.setAssets(page.getAssets().stream().filter(new PageAssetPredicate()).collect(toSet()));
        var savedPage = repository.updateLastUpdateAndSave(page);
        if (!savedPage.getId().equals(pageId)) {
            assetService.duplicateAsset(repository.resolvePath(pageId), repository.resolvePath(pageId), pageId, savedPage.getId());
            repository.delete(pageId);
        }
        return savedPage;
    }

    @Override
    public Page rename(String pageId, String name) {
        Page page = get(pageId);
        if (!page.getName().equals(name)) {
            String newPageId = repository.getNextAvailableId(name);
            page.setId(newPageId);
            page.setName(name);
            Page savedPage = repository.updateLastUpdateAndSave(page);
            assetService.duplicateAsset(repository.resolvePath(pageId), repository.resolvePath(pageId), pageId, savedPage.getId());
            repository.delete(pageId);
            return savedPage;
        }
        return page;
    }

    @Override
    public void delete(String pageId) {
        repository.delete(pageId);
    }

    @Override
    public List<String> getResources(Page page) {
        List<String> resources = getResourcesFromVariables(page.getVariables());

        Set<String> fragments = fragmentIdVisitor.visit(page);
        for (String fragmentId : fragments) {
            Fragment fragment = fragmentService.get(fragmentId);
            List<String> fragmentResources = getResourcesFromVariables(fragment.getVariables());
            resources.addAll(fragmentResources);
        }

        var componentList = new ArrayList<Component>();
        componentVisitor.visit(page).forEach(componentList::add);

        if (componentList.stream()
                .anyMatch(withAction("Start process"))) {
            resources.add("POST|bpm/process");
        }
        if (componentList.stream()
                .anyMatch(withAction("Submit task"))) {
            resources.add("POST|bpm/userTask");
        }
        resources.addAll(findResourcesIn(componentList.stream().filter(withAction("GET")), "url", "GET"));
        resources.addAll(findResourcesIn(componentList.stream().filter(withAction("POST")), "url", "POST"));
        resources.addAll(findResourcesIn(componentList.stream().filter(withAction("PUT")), "url", "PUT"));
        resources.addAll(findResourcesIn(componentList.stream().filter(withAction("DELETE")), "url", "DELETE"));
        resources.addAll(findResourcesIn(componentList.stream(), "apiUrl", "GET"));
        resources.addAll(findResourcesIn(componentList.stream().filter(withId("pbUpload")), "url", "POST"));

        return resources.stream().distinct().collect(toList());
    }

    private List<String> getResourcesFromVariables(Map<String, Variable> variables) {
        List<String> resources = variables.values().stream()
                .filter(new BonitaVariableResourcePredicate(BONITA_RESOURCE_REGEX))
                .map(new BonitaResourceTransformer(BONITA_RESOURCE_REGEX))
                .collect(toList());

        List<String> extension = variables.values().stream()
                .filter(new BonitaVariableResourcePredicate(EXTENSION_RESOURCE_REGEX))
                .map(new BonitaResourceTransformer(EXTENSION_RESOURCE_REGEX))
                .collect(Collectors.toList());
        resources.addAll(extension);
        return resources;
    }

    private Set<String> findResourcesIn(Stream<Component> components, String propertyName, String httpVerb) {
        return components
                .map(propertyValue(propertyName))
                .filter(Objects::nonNull)
                .filter(propertyType(ParameterType.CONSTANT).or(propertyType(ParameterType.INTERPOLATION)))
                .filter(notNullOrEmptyValue())
                .map(toPageResource(httpVerb))
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private Predicate<? super PropertyValue> notNullOrEmptyValue() {
        return propertyValue -> propertyValue.getValue() != null && !propertyValue.getValue().toString().isEmpty();
    }

    private Function<PropertyValue, String> toPageResource(String httpVerb) {
        return propertyValue -> {
            String value = propertyValue.getValue().toString();
            return value.matches(BONITA_RESOURCE_REGEX)
                    ? new ResourceURLFunction(BONITA_RESOURCE_REGEX, httpVerb).apply(value)
                    : value.matches(EXTENSION_RESOURCE_REGEX)
                    ? new ResourceURLFunction(EXTENSION_RESOURCE_REGEX, httpVerb).apply(value) : null;
        };
    }

    private Function<Component, PropertyValue> propertyValue(String propertyName) {
        return component -> component.getPropertyValues().get(propertyName);
    }

    private Predicate<PropertyValue> propertyType(ParameterType type) {
        return propertyValue -> Objects.equals(type.getValue(), propertyValue.getType());
    }

    private Predicate<? super Component> withAction(String action) {
        return component -> component.getPropertyValues().containsKey("action") && Objects.equals(action,
                String.valueOf(component.getPropertyValues().get("action").getValue()));
    }

    private Predicate<? super Component> withId(String id) {
        return component -> Objects.equals(id, component.getId());
    }

    @Override
    public Page migrate(Page page) {
        MigrationResult<Page> migrationResult = migrateWithReport(page);
        return migrationResult.getArtifact();
    }

    @Override
    public MigrationResult<Page> migrateWithReport(Page pageToMigrate) {
        pageToMigrate.setStatus(getStatus(pageToMigrate));

        if (!pageToMigrate.getStatus().isMigration()) {
            return new MigrationResult<>(pageToMigrate, Collections.emptyList());
        }

        MigrationResult<Page> migratedResult = pageMigrationApplyer.migrate(pageToMigrate);
        Page migratedPage = migratedResult.getArtifact();
        // Error during adding modalContainer classes in asset, Missing templates/page/assets/css/style.css from classpath
        if (!migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            repository.updateLastUpdateAndSave(migratedPage);
        } else {
            migratedResult.getMigrationStepReportList().stream().filter(report -> !MigrationStatus.SUCCESS.equals(report.getMigrationStatus()))
                    .forEach(report -> log.error("{}: {} - {}", report.getMigrationStatus(), report.getStepInfo(), report.getComments()));
        }
        return migratedResult;
    }

    @Override
    public MigrationStatusReport getStatus(Page page) {

        MigrationStatusReport pageStatusReport = super.getStatus(page);
        MigrationStatusReport depReport = pageMigrationApplyer.getMigrationStatusDependencies(page);

        return mergeStatusReport(pageStatusReport, depReport);
    }

    @Override
    public Set<Asset> listAsset(Page page) {
        return assetVisitor.visit(page);
    }

}
