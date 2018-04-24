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

import static java.lang.String.format;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageMigrationApplyer {

    protected WidgetService widgetService;
    protected final List<Migration<Page>> migrationList;
    private static final Logger logger = LoggerFactory.getLogger(PageMigrationApplyer.class);

    public PageMigrationApplyer(List<Migration<Page>> pageMigrationStepsList, WidgetService widgetService) {
        this.widgetService = widgetService;
        this.migrationList = pageMigrationStepsList;
    }

    public Page migrate(Page page) {
        long startTime = System.currentTimeMillis();
        String formerArtifactVersion = page.getDesignerVersion();
        for (Migration<Page> migration : migrationList) {
            migration.migrate(page);
        }

        migrateAllWidgetUsed(page);

        updatePreviousDesignerVersionIfMigrationDone(page,formerArtifactVersion,startTime);

        return page;
    }

    protected Page updatePreviousDesignerVersionIfMigrationDone(Page page, String formerArtifactVersion, long startTime){
        if (!StringUtils.equals(formerArtifactVersion, page.getDesignerVersion())) {
            page.setPreviousDesignerVersion(formerArtifactVersion);
            logger.info(format("[MIGRATION] Page %s has been terminated in %s seconds!", page.getName(), (System.currentTimeMillis() - startTime) / 1000.0f));
        }
        return page;
    }

    protected void migrateAllWidgetUsed(Page page) {
       widgetService.migrateAllCustomWidgetUsedInPreviewable(page);
    }
}
