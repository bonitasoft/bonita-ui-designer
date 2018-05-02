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
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.Identifiable;
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
        String formerArtifactVersion = page.getDesignerVersion();
        Page migratedPage = pageMigrationApplyer.migrate((Page) page);
        if (!StringUtils.equals(formerArtifactVersion, migratedPage.getDesignerVersion())) {
            pageRepository.updateLastUpdateAndSave(migratedPage);
        }
        return migratedPage;
    }

}
