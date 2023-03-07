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
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.exception.IncompatibleException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.WebResourcesVisitor;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
public class DefaultPageService extends AbstractAssetableArtifactService<PageRepository, Page> implements PageService {

    private final PageMigrationApplyer pageMigrationApplyer;

    private final ComponentVisitor componentVisitor;

    private final AssetVisitor assetVisitor;

    private final WebResourcesVisitor webResourcesVisitor;

    public DefaultPageService(PageRepository pageRepository, PageMigrationApplyer pageMigrationApplyer, ComponentVisitor componentVisitor,
                              AssetVisitor assetVisitor,
                              UiDesignerProperties uiDesignerProperties, AssetService<Page> pageAssetService, WebResourcesVisitor webResourcesVisitor) {
        super(uiDesignerProperties, pageAssetService, pageRepository);
        this.pageMigrationApplyer = pageMigrationApplyer;
        this.componentVisitor = componentVisitor;
        this.assetVisitor = assetVisitor;
        this.webResourcesVisitor = webResourcesVisitor;
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
    public List<WebResource> detectAutoWebResources(Page page) {
        var resources = this.webResourcesVisitor.visit(page);
        return resources.values().stream().collect(toList());
    }

    @Override
    public List<String> getResources(Page page) {
        List<WebResource> autoResource = detectAutoWebResources(page);

        List<String> resources = autoResource.stream().map(WebResource::toDefinition).collect(toList());
        List<String> manual = page.getWebResources().stream().map(WebResource::toDefinition).collect(Collectors.toList());
        resources.addAll(manual);

        var componentList = new ArrayList<Component>();
        componentVisitor.visit(page).forEach(componentList::add);

        return resources.stream().distinct().collect(toList());
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

