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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.lang.String.format;
import static java.nio.file.Files.write;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LiveRepositoryUpdateTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());

    @Mock
    JsonFileBasedPersister<Page> persister;

    JsonFileBasedLoader<Page> loader = new JsonFileBasedLoader<>(objectMapper, Page.class);

    @Mock
    BeanValidator beanValidator;

    PageRepository repository;

    private WorkspaceProperties workspaceProperties;

    @Before
    public void setUp() throws Exception {
        workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getPages().setDir(folder.toPath());
        workspaceProperties.getWidgets().setDir(folder.toPath());
        repository = new PageRepository(workspaceProperties, new WorkspaceUidProperties(), persister, loader, beanValidator, mock(Watcher.class));
    }

    @Test
    public void should_migrate_a_page() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration<>("2.1", mockMigrationStep);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, singletonList(migration));
        Page page = createPage("2.0");
        Optional<MigrationStepReport> stepReport = Optional.of(new MigrationStepReport(MigrationStatus.SUCCESS, "pageJson"));
        when(mockMigrationStep.migrate(page)).thenReturn(stepReport);

        liveRepositoryUpdate.migrate();

        page.setModelVersion("2.1");
        verify(persister).save(folder.getRoot().toPath().resolve("pageJson"), page);
    }

    @Test
    public void should_not_migrate_file_which_are_not_json() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, singletonList(migration));
        folder.newFile("whatever");

        liveRepositoryUpdate.migrate();

        verify(migration, never()).migrate(any(Page.class));
    }

    @Test
    public void should_not_save_an_artifact_already_migrated() throws Exception {
        Migration<Page> migration = new Migration<>("1.0.2", mock(MigrationStep.class));
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, singletonList(migration));
        createPage("1.0.2");

        liveRepositoryUpdate.migrate();

        verify(persister, never()).save(any(Path.class), any(Page.class));
    }

    @Test
    public void should_exclude_assets() throws Exception {
        Migration<Page> migration = mock(Migration.class);
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, singletonList(migration));
        createPage("1.0.0");
        folder.newFolder("pageJson", "assets");
        folder.newFile("pageJson/assets/whatever.json");

        liveRepositoryUpdate.migrate();


        verify(migration, only()).migrate(any(Page.class));
    }

    @Test
    public void should_be_refresh_repository_index_json_on_start() throws Exception {
        LiveRepositoryUpdate<Page> liveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, EMPTY_LIST);
        createPage("1.7.25");

        liveRepositoryUpdate.start();

        verify(persister).saveInIndex(nullable(Path.class), any(Page.class));
        verify(persister).updateMetadata(any(Path.class), any(Page.class));
    }

    @Test
    public void should_order_LiveRepositoryUpdate() throws Exception {
        LiveRepositoryUpdate<Page> pageLiveRepositoryUpdate = new LiveRepositoryUpdate<>(repository, loader, EMPTY_LIST);

        Repository<Widget> wRepo = new WidgetRepository(workspaceProperties, new WorkspaceUidProperties(),mock(JsonFileBasedPersister.class), mock(WidgetFileBasedLoader.class), beanValidator, mock(Watcher.class), mock(UiDesignerProperties.class));

        LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate = new LiveRepositoryUpdate<>(wRepo, mock(WidgetFileBasedLoader.class), EMPTY_LIST);

        List<LiveRepositoryUpdate> liveRepoList = new ArrayList<>();
        liveRepoList.add(pageLiveRepositoryUpdate);
        liveRepoList.add(widgetLiveRepositoryUpdate);

        Assertions.assertThat(liveRepoList).containsExactly(pageLiveRepositoryUpdate, widgetLiveRepositoryUpdate);
        Assertions.assertThat(liveRepoList.stream().sorted().collect(Collectors.toList())).containsExactly(widgetLiveRepositoryUpdate, pageLiveRepositoryUpdate);
    }

    private Page createPage(String version) throws IOException {
        folder.newFolder("pageJson");
        File pageJson = folder.newFile("pageJson/pageJson.json");
        write(pageJson.toPath(), format("{ \"id\": \"pageJson\", \"modelVersion\": \"%s\" }", version).getBytes());
        return loader.load(pageJson.getParentFile().toPath().resolve(pageJson.getName()));
    }
}
