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

package org.bonitasoft.web.designer.migration;

import static java.util.Collections.singletonList;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MigrationTest {

    @Mock
    MigrationStep<Page> migrationStep;

    Migration<Page> migration;

    Page page = aPage().withId("123").build();

    @Before
    public void setUp() throws Exception {
        migration = new Migration<>("1.0.1", migrationStep);
    }

    @Test
    public void should_migrate_a_page_with_a_version_lower_than_migration() throws Exception {
        page.setDesignerVersion("1.0.0");

        migration.migrate(page);

        verify(migrationStep).migrate(page);
    }

    @Test
    public void should_not_migrate_a_page_with_a_version_greater_than_migration() throws Exception {
        page.setDesignerVersion("1.0.2");

        migration.migrate(page);

        verify(migrationStep, never()).migrate(page);
    }

    @Test
    public void should_not_migrate_a_page_with_a_version_equal_to_migration() throws Exception {
        page.setDesignerVersion("1.0.1");

        migration.migrate(page);

        verify(migrationStep, never()).migrate(page);
    }
}
