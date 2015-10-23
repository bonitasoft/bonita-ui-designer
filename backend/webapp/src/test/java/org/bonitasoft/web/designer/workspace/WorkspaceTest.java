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
package org.bonitasoft.web.designer.workspace;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.config.WebMvcConfiguration;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


public class WorkspaceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private Environment env;
    @Mock
    private WorkspacePathResolver pathResolver;
    @Mock
    private WidgetDirectiveBuilder widgetDirectiveBuilder;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private AssetImporter<Widget> widgetAssetImporter;
    @Mock
    private BeanValidator validator;
    //We use the real instance because we want to verify the folder and the directive files
    private JsonFileBasedPersister<Widget> widgetPersister = new DesignerConfig().widgetFileBasedPersister();
    //We use a real instance of object mapper
    private JacksonObjectMapper jacksonObjectMapper = new DesignerConfig().objectMapperWrapper();
    //Class on test

    private Workspace workspace;

    private WidgetRepository widgetRepository;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        //We mock the default directories
        when(pathResolver.getPagesRepositoryPath()).thenReturn(Paths.get(temporaryFolder.toPath().toString(), "pages"));
        when(pathResolver.getWidgetsRepositoryPath()).thenReturn(Paths.get(temporaryFolder.toPath().toString(), "widgets"));

        widgetRepository = new WidgetRepository(
                pathResolver.getWidgetsRepositoryPath(),
                widgetPersister,
                new WidgetLoader(jacksonObjectMapper),
                validator,
                new Watcher());

        workspace = new Workspace(pathResolver, widgetRepository, new WidgetLoader(jacksonObjectMapper), widgetDirectiveBuilder, resourceLoader, widgetAssetImporter);
    }

    private void mockWidgetsBasePath(Path path) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(path.toUri());
        when(resourceLoader.getResource(WebMvcConfiguration.WIDGETS_RESOURCES)).thenReturn(resource);
    }

    @Test
    public void should_ensure_that_folders_page_and_widgets_are_created() throws Exception {
        mockWidgetsBasePath(temporaryFolder.toPath().resolve("widgets"));

        workspace.initialize();

        // no exception expected and we have 3 folders
        assertThat(temporaryFolder.toPath().resolve("pages")).exists();
        assertThat(temporaryFolder.toPath().resolve("widgets")).exists();
    }

    @Test
    public void should_not_throw_exception_when_a_folder_exist_before_init() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages");
        Path widgetFolder = temporaryFolder.newFolderPath("widgets");
        mockWidgetsBasePath(widgetFolder);

        workspace.initialize();

        // no exception expected and we have 3 folders
        assertThat(temporaryFolder.toPath().resolve("pages")).exists();
        assertThat(temporaryFolder.toPath().resolve("widgets")).exists();
    }


    @Test
    public void should_copy_widget_to_widget_repository_folder() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));

        workspace.initialize();

        assertThat(pathResolver.getWidgetsRepositoryPath().resolve("pbLabel/pbLabel.json")).exists();
        assertThat(pathResolver.getWidgetsRepositoryPath().resolve("pbText/pbText.json")).exists();
    }

    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_folder() throws Exception {
        mockWidgetsBasePath(temporaryFolder.toPath().resolve("widgets"));

        //We create the widget files
        Path labelDir = temporaryFolder.newFolderPath("widgets", "pbLabel");
        Path labelFile = labelDir.resolve("pbLabel.json");
        byte[] fileContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\"}".getBytes(StandardCharsets.UTF_8);
        write(labelFile, fileContent, StandardOpenOption.CREATE);

        workspace.initialize();

        assertThat(readAllBytes(labelFile)).isEqualTo(fileContent);
        assertThat(pathResolver.getWidgetsRepositoryPath().resolve("pbLabel/pbLabel.json")).exists();
    }

    @Test
    public void should_copy_widget_file_if_it_is_already_in_widget_repository_folder_with_a_former_version() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));

        //We create the widget files
        Path labelDir = temporaryFolder.newFolderPath("widgets", "pbLabel");
        Path labelFile = labelDir.resolve("pbLabel.json");
        byte[] fileContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"1.0.1\"}".getBytes(StandardCharsets.UTF_8);
        write(labelFile, fileContent, StandardOpenOption.CREATE);

        workspace.initialize();

        String newLabelContent = new String(readAllBytes(pathResolver.getWidgetsRepositoryPath().resolve("pbLabel/pbLabel.json")));
        String oldLabelContent = new String(readAllBytes(Paths.get("src/test/resources/workspace/widgets/pbLabel/pbLabel.json")));
        assertThat(newLabelContent).isNotEqualTo(oldLabelContent);
    }

}
