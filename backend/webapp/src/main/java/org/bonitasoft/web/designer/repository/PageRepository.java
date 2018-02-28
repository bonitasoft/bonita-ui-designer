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

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.util.StringUtils;


@Named
public class PageRepository extends AbstractRepository<Page> implements WidgetContainerRepository<Page> {

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
}
