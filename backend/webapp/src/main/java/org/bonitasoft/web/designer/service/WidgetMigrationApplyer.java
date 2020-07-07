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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetMigrationApplyer {

    private final List<Migration<Widget>> migrationList;
    private static final Logger logger = LoggerFactory.getLogger(WidgetMigrationApplyer.class);

    public WidgetMigrationApplyer(List<Migration<Widget>> widgetMigrationStepsList){
        this.migrationList = widgetMigrationStepsList;
    }

    public MigrationResult<Widget> migrate(Widget widget) {
        long startTime = System.currentTimeMillis();
        String formerArtifactVersion = widget.getArtifactVersion();
        List<MigrationStepReport> l = new ArrayList<>();
        for (Migration<Widget> migration : migrationList) {
            l.addAll(migration.migrate(widget));
        }

        if (!StringUtils.equals(formerArtifactVersion, widget.getArtifactVersion())) {
            widget.setPreviousArtifactVersion(formerArtifactVersion);
            logger.info(format("[MIGRATION] Widget %s has been terminated in %s seconds !", widget.getName(),(System.currentTimeMillis() - startTime)/ 1000.0f));
        }
        return new MigrationResult(widget,l);
    }
}
