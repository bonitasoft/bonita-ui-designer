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

import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveMigrationTest {

    JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());

    @Mock
    Migration<Page> migration;

    @Mock
    JsonFileBasedPersister<Page> persister;

    JsonFileBasedLoader<Page> loader = new JsonFileBasedLoader<>(objectMapper, Page.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    BeanValidator beanValidator;

    PageRepository repository;

    LiveMigration<Page> liveMigration;

    @Before
    public void setUp() throws Exception {
        repository = new PageRepository(folder.toPath(), persister, loader, beanValidator, new Watcher());
        liveMigration = new LiveMigration<>(repository, loader, singletonList(migration));
    }

    @Test
    public void should_migrate_a_page() throws Exception {
        folder.newFolder("pageJson");
        File pageJson = folder.newFile("pageJson/pageJson.json");
        write(pageJson.toPath(), "{ \"id\": \"pageJson\", \"designerVersion\": \"1.0.1\" }".getBytes());
        Page page = loader.load(pageJson.getParentFile().toPath(), pageJson.getName());

        liveMigration.start();

        verify(migration).migrate(page);
        verify(persister).save(pageJson.getParentFile().toPath(), "pageJson", page);
    }

    @Test
    public void should_not_migrate_file_which_are_not_json() throws Exception {
        folder.newFile("whatever");

        liveMigration.start();

        verify(migration, never()).migrate(any(Page.class));
    }
}
