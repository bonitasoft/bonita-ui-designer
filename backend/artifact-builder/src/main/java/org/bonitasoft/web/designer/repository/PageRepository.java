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
package org.bonitasoft.web.designer.repository;

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PageRepository extends AbstractRepository<Page> implements RefreshingRepository, WidgetContainerRepository<Page> {

    public static final String METADATA = ".metadata";
    private static final Logger logger = LoggerFactory.getLogger(PageRepository.class);

    public PageRepository(
            WorkspaceProperties workspaceProperties,
            WorkspaceUidProperties workspaceUidProperties,
            JsonFileBasedPersister<Page> persister,
            JsonFileBasedLoader<Page> loader,
            BeanValidator validator,
            Watcher watcher) {
        super(workspaceProperties.getPages().getDir(), persister, loader, validator, watcher, workspaceUidProperties.getTemplateResourcesPath());
    }

    @Override
    public String getComponentName() {
        return "page";
    }

    public List<Page> getArtifactsUsingWidget(String widgetId) {
        return this.findByObjectId(widgetId);
    }

    public Map<String, List<Page>> getArtifactsUsingWidgets(List<String> widgetIds) {
        return this.findByObjectIds(widgetIds);
    }

    @Override
    public void refresh(String id) {
        try {
            var page = this.get(id);
            var metadataPath = persister.updateMetadata(this.path.resolve(page.getId()), page);
            persister.saveInIndex(metadataPath, page);
        } catch (RepositoryException e) {
            logger.error("Cannot read page {}. Maybe a migration is required.", id, e);
        } catch (IOException e) {
            logger.error("Cannot update index file.", e);
        }
    }

    public void refreshIndexing(List<Page> pages) {
        try {
            persister.refreshIndexing(this.path.resolve(METADATA), pages);
        } catch (Exception e) {
            logger.error("Cannot refresh workspace indexing.", e);
        }
    }

}
