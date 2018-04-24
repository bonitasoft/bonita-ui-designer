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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageMigrationApplyerTest {

    @Mock
    private WidgetService widgetService;

    @Test
    public void should_migrate_a_page() throws IOException {
        Migration<Page> migration = new Migration("1.0.2", mock(MigrationStep.class));
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService);
        Page page = PageBuilder.aPage().withId("myPage").withVersion("1.0.1").withPreviousDesignerVersion("1.0.0").build();

        migrationApplyer.migrate(page);

        Assert.assertEquals(page.getPreviousDesignerVersion(),"1.0.1");
        Assert.assertEquals(page.getDesignerVersion(),"1.0.2");
    }

    @Test
    public void should_not_modify_previous_designer_version_when_no_migration_done() throws Exception {
        Migration<Page> migration = new Migration("1.0.0", mock(MigrationStep.class));
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService);
        Page page = PageBuilder.aPage().withId("myPage").withVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();

        migrationApplyer.migrate(page);

        Assert.assertEquals(page.getPreviousDesignerVersion(),"1.0.0");
        Assert.assertEquals(page.getDesignerVersion(),"1.0.0");
    }

    @Test
    public void should_migrate_all_custom_widget_uses_in_page_when_page_migration_is_done(){
        Migration<Page> migration = new Migration("1.0.1", mock(MigrationStep.class));
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService);
        Page page = PageBuilder.aPage().withId("myPage").withVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();

        Page migratedPage = migrationApplyer.migrate(page);

        verify(widgetService).migrateAllCustomWidgetUsedInPreviewable(migratedPage);
        Assert.assertEquals(migratedPage.getPreviousDesignerVersion(),"1.0.0");
        Assert.assertEquals(migratedPage.getDesignerVersion(),"1.0.1");
    }
}
