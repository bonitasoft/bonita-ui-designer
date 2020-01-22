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

import static java.lang.String.format;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.config.DesignerConfigConditional;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class PageRepository extends AbstractRepository<Page> implements RefreshingRepository, WidgetContainerRepository<Page> {

    private static final Logger logger = LoggerFactory.getLogger(DesignerConfigConditional.class);
    public static final String METADATA = ".metadata";

    @Inject
    public PageRepository(
            @Named("pagesPath") Path path,
            @Named("pageFileBasedPersister") JsonFileBasedPersister<Page> persister,
            @Named("pageFileBasedLoader") JsonFileBasedLoader<Page> loader,
            BeanValidator validator,
            Watcher watcher) {
        super(path, persister, loader, validator, watcher);
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
            Page page = this.get(id);
            Path metadataPath = persister.updateMetadata(this.path.resolve(page.getId()), page);
            persister.saveInIndex(metadataPath, page);
        } catch (RepositoryException e) {
            logger.error(format("Cannot read page %s. Maybe a migration is required.", id), e);
        } catch (IOException e) {
            logger.error("Cannot update index file.", e);
        }
    }

    public void refreshIndexing(List<Page> pages) {
        try {
            persister.refreshIndexing(this.path.resolve(METADATA), pages);
        } catch (Exception e) {
            logger.error("Cannot refresh workspace indexing.");
        }
    }

}
