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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Mock
    private PageMigrationApplyer pageMigrationApplyer;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageService pageService;

    @Before
    public void setUp() throws Exception {
        pageService = new PageService(pageRepository, pageMigrationApplyer, new UiDesignerProperties("1.13.0",CURRENT_MODEL_VERSION));
    }

    @Test
    public void should_migrate_found_page_when_get_is_called() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(true, false));
        MigrationResult mr = new MigrationResult(migratedPage, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave((Page) mr.getArtifact());
        verify(pageMigrationApplyer).getMigrationStatusDependencies(page);
    }

    @Test
    public void should_not_update_and_save_page_if_no_migration_done() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(true, false));

        Page returnedPage = pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
        assertEquals(returnedPage.getStatus().isCompatible(), true);
        assertEquals(returnedPage.getStatus().isMigration(), false);
    }

    @Test
    public void should_not_update_and_save_page_if_migration_is_on_error() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        MigrationResult mr = new MigrationResult(migratedPage, Arrays.asList(new MigrationStepReport(MigrationStatus.ERROR)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(true, false));

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave((Page) mr.getArtifact());
    }

    @Test
    public void should_migrate_page_when_dependencies_need_to_be_migrated() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(true, true));
        MigrationResult mr = new MigrationResult(migratedPage, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave((Page) mr.getArtifact());
    }

    @Test
    public void should_not_migrate_page_when_page_not_compatible() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("3.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(false, false));

        pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_not_migrate_page_when_dependencies_not_compatible() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(false, false));

        pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_migrate_page_when_no_artifact_version_is_declared() {
        Page page = PageBuilder.aPage().withId("myPage").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.getMigrationStatusDependencies(page)).thenReturn(new MigrationStatusReport(true, false));
        MigrationResult mr = new MigrationResult(migratedPage, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }


}
