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

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.ArtifactBuilderFactory;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Workspace workspace;

    private UiDesignerProperties uiDesignerProperties;

    private WorkspaceProperties workspaceProperties;

    @Before
    public void setUp() throws Exception {

        uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.setModelVersion(CURRENT_MODEL_VERSION);

        workspaceProperties = uiDesignerProperties.getWorkspace();
        final String fakeProjectFolder = temporaryFolder.toPath().toString();
        workspaceProperties.getPages().setDir(Paths.get(fakeProjectFolder, "pages"));
        workspaceProperties.getWidgets().setDir(Paths.get(fakeProjectFolder, "widgets"));
        workspaceProperties.getFragments().setDir(Paths.get(fakeProjectFolder, "fragments"));

        WorkspaceUidProperties workspaceUidProperties = uiDesignerProperties.getWorkspaceUid();
        workspaceUidProperties.setExtractPath(Paths.get(fakeProjectFolder).resolve("tmpExtract"));

        ArtifactBuilder artifactBuilder = new ArtifactBuilderFactory(uiDesignerProperties).create();

        workspace = spy(artifactBuilder.getWorkspace());
        workspace.initialized.set(false);

    }

    private void createWidget(String id, String content) throws IOException {
        final Path widgetPath = temporaryFolder.toPath().resolve("widgets/" + id);
        if (Files.exists(widgetPath)) {
            Files.walk(widgetPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        Path labelFile = temporaryFolder.newFolderPath("widgets", id).resolve(id + ".json");
        byte[] fileContent = content.getBytes(StandardCharsets.UTF_8);
        write(labelFile, fileContent, StandardOpenOption.CREATE);
    }

    private String contentOf(Path path) throws IOException {
        return new String(readAllBytes(path));
    }

    @Test
    public void should_ensure_that_folders_page_and_widgets_are_created() throws Exception {

        workspace.initialize();

        getClass().getResourceAsStream("");

        // no exception expected and we have 3 folders
        assertThat(temporaryFolder.toPath().resolve("pages")).exists();
        assertThat(temporaryFolder.toPath().resolve("widgets")).exists();
    }

    @Test
    public void should_not_throw_exception_when_a_folder_exist_before_init() throws Exception {
        //Folder creation
        if (!Files.exists(temporaryFolder.toPath().resolve("pages"))) {
            temporaryFolder.newFolderPath("pages");
        }
        Path widgetFolder = temporaryFolder.toPath().resolve("widgets");
        if (!Files.exists(widgetFolder)) {
            temporaryFolder.newFolderPath("pages");
        }

        workspace.initialize();

        // no exception expected and we have 3 folders
        assertThat(temporaryFolder.toPath().resolve("pages")).exists();
        assertThat(temporaryFolder.toPath().resolve("widgets")).exists();
    }


    @Test
    public void should_copy_widget_to_widget_repository_folder() throws Exception {

        workspace.initialize();

        Path widgetsPath = workspaceProperties.getWidgets().getDir();
        assertThat(widgetsPath.resolve("pbButton/pbButton.json")).exists();
        assertThat(widgetsPath.resolve("pbButton/help.html")).exists();
        assertThat(widgetsPath.resolve("pbMissingHelp/pbMissingHelp.json")).exists();
    }


    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_with_same_version() throws Exception {
        String existingWidgetContent = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"" + CURRENT_MODEL_VERSION + "\"}";
        createWidget("pbLabel", existingWidgetContent);

        workspace.initialize();

        assertThat(contentOf(workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.json"))).isEqualTo(existingWidgetContent);
    }

    @Test
    public void should_copy_widget_file_if_it_is_already_in_widget_repository_folder_with_a_former_version() throws Exception {
        String existingWidgetContent = this.getClass().getResourceAsStream("/workspace/widgets/pbWidgetToOverride/pbWidgetToOverride.json").readAllBytes().toString();

        workspace.initialize();

        assertThat(contentOf(workspaceProperties.getWidgets().getDir().resolve("pbWidgetToOverride/pbWidgetToOverride.json"))).isNotEqualTo(existingWidgetContent);
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
        if (!Files.exists(temporaryFolder.toPath().resolve("fragments"))) {
            temporaryFolder.newFolderPath("fragments");
        }

        workspace.initialize();

        // no exception expected and we have fragment folder
        assertThat(temporaryFolder.toPath().resolve("fragments")).exists();
    }


    @Test
    public void should_not_copy_widget_file_if_it_is_already_in_widget_repository_folder() throws Exception {
        //We create the widget files
        String existingWidget = "{\"id\":\"pbLabel\", \"template\": \"<div>Hello</div>\", \"designerVersion\": \"" + CURRENT_MODEL_VERSION + "\"}";
        byte[] fileContent = existingWidget.getBytes(StandardCharsets.UTF_8);
        createWidget("pbLabel",existingWidget);

        workspace.initialize();

        final Path labelFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.json");
        assertThat(labelFile).exists().hasBinaryContent(fileContent);
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
        //test will fail if fake files are empty (no json inside)
        Files.write(temporaryFolder.newFilePath("fragments/myFragment/myFragment.json"),"{}".getBytes(), StandardOpenOption.WRITE);
        Files.write(temporaryFolder.newFilePath("fragments/.metadata/myFragment.json"),"{}".getBytes(), StandardOpenOption.WRITE);
        Files.write(temporaryFolder.newFilePath("fragments/.metadata/oldestFragment.json"),"{}".getBytes(), StandardOpenOption.WRITE);
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

    @Test
    public void should_initialize_workspace() throws Exception {

        //When
        workspace.initialize();

        verify(workspace).doInitialize();
        verify(workspace).cleanPageWorkspace();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtimeException_if_error_occurs_while_initializing_workspace() throws Exception {
        doThrow(new IOException()).when(workspace).initialize();
        workspace.initialize();
    }


    @Test
    public void should_refresh_index_file_in_metadata_folder_when_initialize_is_called() throws Exception {
        //Folder creation
        temporaryFolder.newFolderPath("pages", ".metadata");
        temporaryFolder.newFolderPath("pages", "myPage");
        var pageContent = "{\"designerVersion\": \"1.0.0\"," +
                "\"id\": \"myPage\"," +
                "\"uuid\": \"123ca6c5-9a72-4a03-a890-2e6bc2aeed93\"," +
                "\"name\": \"myPage\"," +
                "\"type\": \"page\"," +
                "\"lastUpdate\": 1436966572684," +
                "\"rows\": [[]],\"assets\": [],\"data\": {}}";

        write(temporaryFolder.newFilePath("pages/myPage/myPage.json"),
                pageContent.getBytes(), StandardOpenOption.WRITE);

        write(temporaryFolder.newFilePath("pages/.metadata/.index.json"),
                "{\"4a732c6f-254b-4b37-841f-9582696d40e9\":\"anyPage\",\"225ca6c5-9a72-4a03-a890-2e6bc2aeed93\":\"myPage\"}".getBytes(), StandardOpenOption.WRITE);

        temporaryFolder.newFilePath("pages/.metadata/oldestPage.json");

        workspace.initialize();

        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage")).exists();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve("myPage").resolve("js")).doesNotExist();
        await().atMost(2, SECONDS).untilAsserted(() ->
            assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve(".index.json")).exists()
        );
        assertThat(contentOf(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve(".index.json")))
                .isEqualTo("{\"123ca6c5-9a72-4a03-a890-2e6bc2aeed93\":\"myPage\"}");

        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve("oldestPage.json")).doesNotExist();
        assertThat(temporaryFolder.toPath().resolve("pages").resolve(".metadata").resolve("myPage.json")).exists();
    }

}
