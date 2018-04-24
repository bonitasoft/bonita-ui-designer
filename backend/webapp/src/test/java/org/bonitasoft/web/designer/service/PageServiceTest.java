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

import static org.mockito.Mockito.*;

import org.bonitasoft.web.designer.builder.PageBuilder;
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

    @Mock
    private PageMigrationApplyer pageMigrationApplyer;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageService pageService;

    @Before
    public void setUp() throws Exception {
        pageService = new PageService(pageRepository, pageMigrationApplyer);
    }

    @Test
    public void should_migrate_found_page_when_get_is_called() {
        Page page = PageBuilder.aPage().withId("myPage").withVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withVersion("1.0.1").withPreviousDesignerVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.migrate(page)).thenReturn(migratedPage);

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave(migratedPage);
    }

    @Test
    public void should_not_update_and_save_page_if_no_migration_done() {
        Page page = PageBuilder.aPage().withId("myPage").withVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        when(pageMigrationApplyer.migrate(page)).thenReturn(migratedPage);

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(migratedPage);
    }
}
