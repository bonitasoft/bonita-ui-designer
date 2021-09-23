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
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PageMigrationApplyer {

    private final List<Migration<Page>> migrationList;
    private final FragmentService fragmentService;
    private final WidgetService widgetService;

    public PageMigrationApplyer(List<Migration<Page>> pageMigrationStepsList, WidgetService widgetService, FragmentService fragmentService) {
        this.widgetService = widgetService;
        this.migrationList = pageMigrationStepsList;
        this.fragmentService = fragmentService;
    }

    public MigrationResult<Page> migrate(Page page) {
        var startTime = Instant.now();
        var formerArtifactVersion = page.getArtifactVersion();
        List<MigrationStepReport> reports = new ArrayList<>();
        for (Migration<Page> migration : migrationList) {
            reports.addAll(migration.migrate(page));
        }

        reports.addAll(migrateAllWidgetUsed(page));
        reports.addAll(migrateAllFragmentUsed(page));
        updatePreviousArtifactVersionIfMigrationDone(page, formerArtifactVersion, startTime);
        return new MigrationResult<>(page, reports);
    }

    public MigrationStatusReport getMigrationStatusDependencies(Page page) {
        var widgetStatus = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        var fragmentStatus = fragmentService.getMigrationStatusOfFragmentUsed(page);

        if (!widgetStatus.isCompatible() || !fragmentStatus.isCompatible()) {
            return new MigrationStatusReport(false, false);
        }
        if (widgetStatus.isMigration() || fragmentStatus.isMigration()) {
            return new MigrationStatusReport();
        }
        return new MigrationStatusReport(true, false);
    }

    protected void updatePreviousArtifactVersionIfMigrationDone(Page page, String formerArtifactVersion, Instant startTime) {
        if (!StringUtils.equals(formerArtifactVersion, page.getArtifactVersion())) {
            page.setPreviousArtifactVersion(formerArtifactVersion);
            var durationTime = Duration.between(startTime,Instant.now()).toMillis() / 1000.0f;
            log.info("[MIGRATION] Page {} has been terminated in {} seconds!", page.getName(), durationTime);
        }
    }

    protected List<MigrationStepReport> migrateAllWidgetUsed(Page page) {
        return widgetService.migrateAllCustomWidgetUsedInPreviewable(page);
    }

    private List<MigrationStepReport> migrateAllFragmentUsed(Page page) {
        return fragmentService.migrateAllFragmentUsed(page);
    }
}
