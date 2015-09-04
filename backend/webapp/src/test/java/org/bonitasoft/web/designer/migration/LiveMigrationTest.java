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

import static java.lang.String.format;
import static java.nio.file.Files.write;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

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
    JsonFileBasedPersister<Page> persister;

    JsonFileBasedLoader<Page> loader = new JsonFileBasedLoader<>(objectMapper, Page.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    BeanValidator beanValidator;

    PageRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new PageRepository(folder.toPath(), persister, loader, beanValidator, new Watcher());
    }

    @Test
    public void should_migrate_a_page() throws Exception {
        Migration<Page> migration = new Migration<>("1.0.2", mock(MigrationStep.class));
        LiveMigration<Page> liveMigration = new LiveMigration<>(repository, loader, singletonList(migration));
        Page page = createPage("1.0.1");

        liveMigration.start();

        page.setDesignerVersion("1.0.2");
        verify(persister).save(folder.getRoot().toPath().resolve("pageJson"), "pageJson", page);
    }

    @Test
    public void should_not_migrate_file_which_are_not_json() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveMigration<Page> liveMigration = new LiveMigration<>(repository, loader, singletonList(migration));
        folder.newFile("whatever");

        liveMigration.start();

        verify(migration, never()).migrate(any(Page.class));
    }

    @Test
    public void should_not_save_an_artifact_already_migrated() throws Exception {
        Migration<Page> migration = new Migration<>("1.0.2", mock(MigrationStep.class));
        LiveMigration<Page> liveMigration = new LiveMigration<>(repository, loader, singletonList(migration));
        createPage("1.0.2");

        liveMigration.start();

        verify(persister, never()).save(any(Path.class), anyString(), any(Page.class));
    }

    @Test
    public void should_exclude_assets() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveMigration<Page> liveMigration = new LiveMigration<>(repository, loader, singletonList(migration));
        createPage("1.0.0");
        folder.newFolder("pageJson/assets");
        folder.newFile("pageJson/assets/whatever.json");

        liveMigration.start();

        verify(migration, only()).migrate(any(Page.class));
    }

    private Page createPage(String version) throws IOException {
        folder.newFolder("pageJson");
        File pageJson = folder.newFile("pageJson/pageJson.json");
        write(pageJson.toPath(), format("{ \"id\": \"pageJson\", \"designerVersion\": \"%s\" }", version).getBytes());
        return loader.load(pageJson.getParentFile().toPath(), pageJson.getName());
    }
}
