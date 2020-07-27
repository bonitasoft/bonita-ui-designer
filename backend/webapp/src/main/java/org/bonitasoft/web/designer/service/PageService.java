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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;

@Named
public class PageService implements ArtifactService {

    private final PageMigrationApplyer pageMigrationApplyer;
    private PageRepository pageRepository;

    @Inject
    public PageService(PageRepository pageRepository, PageMigrationApplyer pageMigrationApplyer) {
        this.pageRepository = pageRepository;
        this.pageMigrationApplyer = pageMigrationApplyer;
    }


    @Override
    public Page get(String id) {
        Page page = this.pageRepository.get(id);
        return migrate(page);
    }

    @Override
    public Page migrate(Identifiable page) {
        MigrationResult<Page> migrationResult = migrateWithReport(page);
        return migrationResult.getArtifact();
    }

    @Override
    public MigrationResult<Page> migrateWithReport(Identifiable page) {
        String formerArtifactVersion = page.getArtifactVersion();
        MigrationResult<Page> migratedResult = pageMigrationApplyer.migrate((Page) page);
        Page migratedPage = migratedResult.getArtifact();
        if (!StringUtils.equals(formerArtifactVersion, migratedPage.getArtifactVersion()) && !migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            pageRepository.updateLastUpdateAndSave(migratedPage);
        }
        return migratedResult;
    }

    @Override
    public MigrationStatusReport getStatus(Identifiable identifiable) {
        return pageMigrationApplyer.getMigrationStatusDependencies((Page) identifiable);
    }

}
