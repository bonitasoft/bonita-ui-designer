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

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.config.WebMvcConfiguration;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.*;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorkspaceTest {

    private static final String CURRENT_DESIGNER_VERSION = "2.0.0";

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
                new WidgetFileBasedLoader(jacksonObjectMapper),
                validator,
                mock(Watcher.class));

        workspace = new Workspace(pathResolver, widgetRepository, new WidgetFileBasedLoader(jacksonObjectMapper), widgetDirectiveBuilder, resourceLoader, widgetAssetImporter);
        ReflectionTestUtils.setField(workspace, "currentDesignerVersion", CURRENT_DESIGNER_VERSION);
    }

    private void mockWidgetsBasePath(Path path) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(path.toUri());
        when(resourceLoader.getResource(WebMvcConfiguration.WIDGETS_RESOURCES)).thenReturn(resource);
    }


    private void createWidget(String id, String content) throws IOException {
        Path labelFile = temporaryFolder.newFolderPath("widgets", id).resolve(id + ".json");
        byte[] fileContent = content.getBytes(StandardCharsets.UTF_8);
        write(labelFile, fileContent, StandardOpenOption.CREATE);
    }

    private String contentOf(Path path) throws IOException {
        return new String(readAllBytes(path));
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
        assertThat(pathResolver.getWidgetsRepositoryPath().resolve("pbText/help.html")).exists();
        assertThat(pathResolver.getWidgetsRepositoryPath().resolve("pbMissingHelp/pbMissingHelp.json")).exists();
    }

    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_with_same_version() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));
        String existingWidgetContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \""+ CURRENT_DESIGNER_VERSION + "\"}";
        createWidget("pbLabel", existingWidgetContent);

        workspace.initialize();

        assertThat(contentOf(pathResolver.getWidgetsRepositoryPath().resolve("pbLabel/pbLabel.json"))).isEqualTo(existingWidgetContent);
    }

    @Test
    public void should_copy_widget_file_if_it_is_already_in_widget_repository_folder_with_a_former_version() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));
        String existingWidgetContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"1.0.1\"}";
        createWidget("pbLabel", existingWidgetContent);

        workspace.initialize();

        assertThat(contentOf(pathResolver.getWidgetsRepositoryPath().resolve("pbLabel/pbLabel.json"))).isNotEqualTo(existingWidgetContent);
    }

    @Test
    public void should_delete_page_reference_when_page_doesnt_exist_anymore_but_any_file_stay_on_filesystem() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages","myPageToRemove","js");
        temporaryFolder.newFilePath("pages/.gitignore");

        workspace.cleanPageWorkspace();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPageToRemove")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".gitignore")).exists();
    }

    @Test
    public void should_delete_only_js_folder_for_page_artifact_when_page_exist() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages","myPage","js");
        temporaryFolder.newFilePath("pages/myPage/myPage.json");

        workspace.cleanPageWorkspace();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage")).exists();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage").resolve("js")).doesNotExist();
    }


}
