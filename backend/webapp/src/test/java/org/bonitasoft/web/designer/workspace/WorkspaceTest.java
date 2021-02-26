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
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WebMvcConfiguration;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.utils.CopyResources;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class WorkspaceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private Environment env;

    private WorkspaceProperties workspaceProperties;
    @Mock
    private WidgetDirectiveBuilder widgetDirectiveBuilder;

    @Mock
    private FragmentDirectiveBuilder fragmentDirectiveBuilder;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private AssetImporter<Widget> widgetAssetImporter;

    private UiDesignerProperties uiDesignerProperties;

    @Mock
    private Resource resource;

    @Mock
    private CopyResources copyResources;

    @Mock
    private BeanValidator validator;

    //We use the real instance because we want to verify the folder and the directive files
    private JsonFileBasedPersister<Widget> widgetPersister;
    //We use a real instance of object mapper
    private JacksonObjectMapper jacksonObjectMapper = new DesignerConfig().objectMapperWrapper();
    //Class on test

    private Workspace workspace;

    private WidgetRepository widgetRepository;

    private WorkspaceUidProperties workspaceUidProperties;

    @Before
    public void setUp() throws URISyntaxException, IOException {
        MockitoAnnotations.initMocks(this);

        uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.setModelVersion(CURRENT_MODEL_VERSION);
        workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getPages().setDir(Paths.get(temporaryFolder.toPath().toString(), "pages"));
        workspaceProperties.getWidgets().setDir(Paths.get(temporaryFolder.toPath().toString(), "widgets"));
        workspaceProperties.getWidgetsWc().setDir(Paths.get(temporaryFolder.toPath().toString(), "widgetsWc"));
        workspaceProperties.getFragments().setDir(Paths.get(temporaryFolder.toPath().toString(), "fragments"));

        widgetPersister = new DesignerConfig().widgetFileBasedPersister(uiDesignerProperties);

        when(resource.getURI()).thenReturn(temporaryFolder.toPath().resolve("widgets").toUri());
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        workspaceUidProperties = new WorkspaceUidProperties();
        workspaceUidProperties.setExtractPath(Paths.get("src/test/resources/workspace/"));

        widgetRepository = new WidgetRepository(
                workspaceProperties,
                new WorkspaceUidProperties(),
                widgetPersister,
                new WidgetFileBasedLoader(jacksonObjectMapper),
                validator,
                mock(Watcher.class), mock(UiDesignerProperties.class));
        WidgetFileBasedLoader widgetLoader = new WidgetFileBasedLoader(jacksonObjectMapper);
        workspace = new Workspace(workspaceProperties, widgetRepository, widgetLoader, widgetDirectiveBuilder, fragmentDirectiveBuilder, resourceLoader, widgetAssetImporter,uiDesignerProperties, workspaceUidProperties, mock(CopyResources.class));
    }

    private void mockWidgetsBasePath(Path path) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(path.toUri());
        when(resourceLoader.getResource(WebMvcConfiguration.WIDGETS_RESOURCES)).thenReturn(resource);
    }

    private void mockWidgetsWcBasePath(Path path) throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(path.toUri());
        when(resourceLoader.getResource(WebMvcConfiguration.WIDGETS_WC_RESOURCES)).thenReturn(resource);
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
        Path widgetsPath = workspaceProperties.getWidgets().getDir();
        assertThat(widgetsPath.resolve("pbLabel/pbLabel.json")).exists();
        assertThat(widgetsPath.resolve("pbText/pbText.json")).exists();
        assertThat(widgetsPath.resolve("pbText/help.html")).exists();
        assertThat(widgetsPath.resolve("pbMissingHelp/pbMissingHelp.json")).exists();
    }


    @Test
    public void should_copy_widgetWc_to_widgetWc_repository_folder() throws Exception {
        uiDesignerProperties.setExperimental(true);
        mockWidgetsWcBasePath(Paths.get("src/test/resources/workspace/widgetsWc"));

        workspace.initialize();
        Path widgetsWcPath = workspaceProperties.getWidgetsWc().getDir();
        assertThat(widgetsWcPath.resolve("pbLabel/pbLabel.json")).exists();
        assertThat(widgetsWcPath.resolve("pbLabel/pbLabel.tpl.html")).exists();
        assertThat(widgetsWcPath.resolve("pbText/pbText.json")).exists();
        assertThat(widgetsWcPath.resolve("pbText/pbText.tpl.html")).exists();
    }

    @Test
    public void should_not_copy_widgetWc_to_widgetWc_repository_folder_when_experimental_mode_is_not_set() throws Exception {
        uiDesignerProperties.setExperimental(false);
        mockWidgetsWcBasePath(Paths.get("src/test/resources/workspace/widgetsWc"));

        workspace.initialize();
        Path widgetsWcPath = workspaceProperties.getWidgetsWc().getDir();
        assertThat(widgetsWcPath.resolve("pbLabel/pbLabel.json")).doesNotExist();
        assertThat(widgetsWcPath.resolve("pbText/pbText.json")).doesNotExist();
    }

    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_with_same_version() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));
        String existingWidgetContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"" + CURRENT_MODEL_VERSION + "\"}";
        createWidget("pbLabel", existingWidgetContent);

        workspace.initialize();

        assertThat(contentOf(workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.json"))).isEqualTo(existingWidgetContent);
    }

    @Test
    public void should_copy_widget_file_if_it_is_already_in_widget_repository_folder_with_a_former_version() throws Exception {
        mockWidgetsBasePath(Paths.get("src/test/resources/workspace/widgets"));
        String existingWidgetContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"1.0.1\"}";
        createWidget("pbLabel", existingWidgetContent);

        workspace.initialize();

        assertThat(contentOf(workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.json"))).isNotEqualTo(existingWidgetContent);
    }

    @Test
    public void should_delete_page_reference_when_page_doesnt_exist_anymore_but_any_file_stay_on_filesystem() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages", "myPageToRemove", "js");
        temporaryFolder.newFilePath("pages/.gitignore");

        workspace.cleanPageWorkspace();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPageToRemove")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".gitignore")).exists();
    }

    @Test
    public void should_delete_only_js_folder_for_page_artifact_when_page_exist() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages", "myPage", "js");
        temporaryFolder.newFilePath("pages/myPage/myPage.json");

        workspace.cleanPageWorkspace();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage")).exists();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage").resolve("js")).doesNotExist();
    }

    @Test
    public void should_keep_file_with_a_reference_on_workspace_when_cleanup_is_called() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages", ".metadata");
        temporaryFolder.newFolderPath("pages", "myPage");
        temporaryFolder.newFilePath("pages/myPage/myPage.json");

        temporaryFolder.newFilePath("pages/.metadata/.index.json");
        temporaryFolder.newFilePath("pages/.metadata/myPage.json");
        temporaryFolder.newFilePath("pages/.metadata/oldestPage.json");

        workspace.cleanPageWorkspace();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage")).exists();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage").resolve("js")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve(".index.json")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve("oldestPage.json")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve("myPage.json")).exists();
    }

    @Test
    public void should_ensure_that_folders_page_widgets_fragments_are_created() throws Exception {

        workspace.initialize();

        // no exception expected and we have 3 folders
        assertThat(workspaceProperties.getPages().getDir()).exists();
        assertThat(workspaceProperties.getWidgets().getDir()).exists();
        assertThat(workspaceProperties.getFragments().getDir()).exists();
    }

    @Test
    public void should_not_throw_exception_when_a_folder_exist_before_init_with_fragment() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("fragments");

        workspace.initialize();

        // no exception expected and we have fragment folder
        assertThat(temporaryFolder.toPath().resolve("fragments")).exists();
    }


    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_folder() throws Exception {
        //We create the widget files
        Path labelDir = temporaryFolder.newFolderPath("widgets", "pbLabel");
        Path labelFile = labelDir.resolve("pbLabel.json");
        String existingWidget = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"" + CURRENT_MODEL_VERSION + "\"}";
        byte[] fileContent = existingWidget.getBytes(StandardCharsets.UTF_8);
        write(labelFile, fileContent, StandardOpenOption.CREATE);

        workspace.initialize();

        assertThat(readAllBytes(labelFile)).isEqualTo(fileContent);
        assertThat(workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.json")).exists();
    }

    @Test
    public void should_delete_fragment_reference_when_fragment_doesnt_exist_anymore_but_any_file_stay_on_filesystem() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("fragments", "myFragment");
        temporaryFolder.newFilePath("fragments/myFragment/widgets-abcd487.min.js");

        workspace.initialize();

        assertThat(temporaryFolder.toPath().resolve("fragments").resolve("myFragment")).doesNotExist();
    }

    @Test
    public void should_delete_only_js_file_for_fragment_artifact_when_fragment_descriptor_exist() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("fragments", "myFragment");
        temporaryFolder.newFolderPath("fragments", ".metadata");
        temporaryFolder.newFilePath("fragments/myFragment/widgets-abcd487.min.js");
        temporaryFolder.newFilePath("fragments/myFragment/myFragment.json");
        temporaryFolder.newFilePath("fragments/.metadata/myFragment.json");
        temporaryFolder.newFilePath("fragments/.metadata/oldestFragment.json");
        temporaryFolder.newFilePath("fragments/.DSSTORE");
        temporaryFolder.newFilePath("fragments/.gitignore");


        workspace.initialize();

        assertThat(temporaryFolder.toPath().resolve("fragments").resolve("myFragment").resolve("widgets-abcd487.min.js")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("fragments").resolve("myFragment")).exists();
        assertThat(temporaryFolder.toPath().resolve("fragments").resolve(".DSSTORE")).exists();
        assertThat(temporaryFolder.toPath().resolve("fragments").resolve(".gitignore")).exists();
        assertThat(temporaryFolder.toPath().resolve("fragments").resolve(".metadata").resolve("oldestFragment.json")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("fragments").resolve(".metadata").resolve("myFragment.json")).exists();

    }

}
