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

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FragmentMigrationApplyer {

    private static final Logger logger = LoggerFactory.getLogger(FragmentMigrationApplyer.class);

    private final List<Migration<Fragment>> migrationList;
    private final WidgetService widgetService;

    public FragmentMigrationApplyer(List<Migration<Fragment>> fragmentMigrationStepsList, WidgetService widgetService) {
        this.migrationList = fragmentMigrationStepsList;
        this.widgetService = widgetService;
    }

    public MigrationResult<Fragment> migrate(Fragment fragment, boolean migrateCustomWidgetUsed) {
        var startTime = Instant.now();
        var formerArtifactVersion = fragment.getArtifactVersion();
        List<MigrationStepReport> reports = new ArrayList<>();
        for (var migration : migrationList) {
            reports.addAll(migration.migrate(fragment));
        }

        if (migrateCustomWidgetUsed) {
            reports.addAll(migrateAllCustomWidgetUsed(fragment));
        }

        if (!StringUtils.equals(formerArtifactVersion, fragment.getArtifactVersion())) {
            fragment.setPreviousArtifactVersion(formerArtifactVersion);
            var durationTime = Duration.between(startTime, Instant.now()).toMillis() / 1000.0f ;
            logger.info("[MIGRATION] Fragment {} has been terminated in {} seconds!", fragment.getName(), durationTime);
        }

        return new MigrationResult(fragment, reports);
    }

    public MigrationStatusReport getMigrationStatusOfCustomWidgetsUsed(Fragment fragment) {
        return widgetService.getMigrationStatusOfCustomWidgetUsed(fragment);
    }

    private List<MigrationStepReport> migrateAllCustomWidgetUsed(Fragment fragment) {
        return widgetService.migrateAllCustomWidgetUsedInPreviewable(fragment);
    }
}

