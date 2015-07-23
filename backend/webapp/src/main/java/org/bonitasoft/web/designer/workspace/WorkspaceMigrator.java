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
package org.bonitasoft.web.designer.workspace;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Versioned;
import org.bonitasoft.web.designer.repository.AbstractRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.retrocompatibility.ComponentMigrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class WorkspaceMigrator {

    protected static final Logger logger = LoggerFactory.getLogger(WorkspaceMigrator.class);

    private PageRepository pageRepository;

    private WidgetRepository widgetRepository;

    private ComponentMigrator componentMigrator;

    @Inject
    public WorkspaceMigrator(PageRepository pageRepository, WidgetRepository widgetRepository, ComponentMigrator componentMigrator) {
        this.pageRepository = pageRepository;
        this.widgetRepository = widgetRepository;
        this.componentMigrator = componentMigrator;
    }


    public void migrate() {
        //Pages and widgets can contain assets
        loadAndMigrateComponents(pageRepository);
        loadAndMigrateComponents(widgetRepository);
    }

    private <T extends Versioned & Assetable> void loadAndMigrateComponents(AbstractRepository<T> repository) {
        List<T> assetables = repository.getAll();

        for (T assetable : assetables) {
            componentMigrator.migrate(repository, assetable);
        }
    }
}
