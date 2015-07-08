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
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;

import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveMigrationTest {

    @Mock
    Watcher watcher;

    @Mock
    JacksonObjectMapper objectMapper;

    @Mock
    Migration<Page> migration;

    @InjectMocks
    LiveMigration liveMigration;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    File pageJson;

    Page page = aPage()
            .withId("123")
            .withVersion("1.0.1")
            .build();

    @Test
    public void should_migrate_a_page() throws Exception {
        pageJson = folder.newFile("pageJson.json");
        Files.write(pageJson.toPath(), "{ 'name': 'foobar' }".getBytes());
        when(objectMapper.fromJson("{ 'name': 'foobar' }".getBytes(), Page.class))
                .thenReturn(page);

        liveMigration.start(
                folder.toPath(),
                Page.class,
                singletonList(migration));

        verify(migration).migrate(page);
    }

    @Test
    public void should_not_migrate_file_which_are_not_json() throws Exception {
        folder.newFile("whatever");

        liveMigration.start(
                folder.toPath(),
                Page.class,
                singletonList(migration));

        verify(migration, never()).migrate(page);
    }
}
