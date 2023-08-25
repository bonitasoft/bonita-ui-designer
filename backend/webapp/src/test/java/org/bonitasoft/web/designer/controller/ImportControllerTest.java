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
package org.bonitasoft.web.designer.controller;


import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.DefaultArtifactBuilder;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportException.Type;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.UnZipper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.builder.ImportReportBuilder.anImportReportFor;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ImportControllerTest {

    @TempDir
    public Path tempDir;

    private MockMvc mockMvc;

    @Mock
    private ArtifactBuilder artifactBuilder;

    @Mock
    private UnZipper unzipper;

    private Path unzipedPath;

    @BeforeEach
    public void setUp() throws IOException {
        ImportController importController = new ImportController(artifactBuilder, unzipper);
        unzipedPath = Files.createDirectory(tempDir.resolve("unzipedPath"));
        lenient().when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenReturn(unzipedPath);
        mockMvc = mockServer(importController).build();
    }

    @Test
    public void should_respond_404_for_an_unknown_artifact_type() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());

        mockMvc.perform(multipart("/import/unknown").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    public void should_import_a_file_with_zip_content_type() throws Exception {
        mockMvc.perform(multipart("/import/page").file(aFile("application/zip")))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/import/page").file(aFile("application/x-zip-compressed")))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/import/page").file(aFile("application/x-zip")))
                .andExpect(status().isCreated());
    }

    private MockMultipartFile aFile(String contentType) {
        return new MockMultipartFile("file", "myfile.zip", contentType, "foo".getBytes());
    }

    @Test
    public void should_respond_400_when_file_content_type_is_not_supported() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "text/html", "foo".getBytes());

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("type").value("IllegalArgumentException"))
                .andExpect(jsonPath("message").value("Only zip files are allowed when importing a component"));
    }

    @Test
    public void should_import_a_file_with_octetstream_content_type() throws Exception {
        mockMvc.perform(multipart("/import/page").file(aFile("application/octet-stream")))
                .andExpect(status().isCreated());
    }

    @Test
    public void should_respond_400_when_file_content_type_is_octetstream_but_filename_is_not_a_zip() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.png", "application/octet-stream", "foo".getBytes());

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("type").value("IllegalArgumentException"))
                .andExpect(jsonPath("message").value("Only zip files are allowed when importing a component"));
    }

    @Test
    public void should_respond_400_when_file_content_is_empty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "".getBytes());

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("type").value("IllegalArgumentException"))
                .andExpect(jsonPath("message").value("Part named [file] is needed to successfully import a component"));
    }

    @Test
    public void should_import_a_page_with_its_dependencies() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport =
                anImportReportFor(aPage().withId("aPage").withName("thePage")).withUUID("UUIDZipFile").withStatus(ImportReport.Status.CONFLICT)
                        .withAdded(aWidget().withId("addedWidget").withName("newWidget"))
                        .withOverwritten(aWidget().withId("overwrittenWidget").withName("oldWidget")).build();
        when(artifactBuilder.importPage(unzipedPath, false)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("uuid").value("UUIDZipFile"))
                .andExpect(jsonPath("extractedDirName").doesNotExist())
                .andExpect(jsonPath("element.id").value("aPage"))
                .andExpect(jsonPath("status").value("conflict"))
                .andExpect(jsonPath("element.name").value("thePage"))
                .andExpect(jsonPath("dependencies.added.widget[0].id").value("addedWidget"))
                .andExpect(jsonPath("dependencies.added.widget[0].name").value("newWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].id").value("overwrittenWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].name").value("oldWidget"));
    }

    @Test
    public void should_force_a_page_import() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport =
                anImportReportFor(aPage().withId("aPage").withName("thePage")).withUUID("UUIDZipFile").withStatus(ImportReport.Status.IMPORTED)
                        .withAdded(aWidget().withId("addedWidget").withName("newWidget"))
                        .withOverwritten(aWidget().withId("overwrittenWidget").withName("oldWidget")).build();
        when(artifactBuilder.importPage(unzipedPath, true)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/page?force=true").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("uuid").value("UUIDZipFile"))
                .andExpect(jsonPath("extractedDirName").doesNotExist())
                .andExpect(jsonPath("element.id").value("aPage"))
                .andExpect(jsonPath("status").value("imported"))
                .andExpect(jsonPath("element.name").value("thePage"))
                .andExpect(jsonPath("dependencies.added.widget[0].id").value("addedWidget"))
                .andExpect(jsonPath("dependencies.added.widget[0].name").value("newWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].id").value("overwrittenWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].name").value("oldWidget"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_importing_a_page() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(artifactBuilder.importPage(unzipedPath, false)).thenThrow(new ImportException(Type.SERVER_ERROR, "an error messge"));

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("an error messge"));
    }

    @Test
    public void should_import_a_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport = anImportReportFor(aWidget().withId("aWidget").withName("myWidgetName")).build();
        when(artifactBuilder.importWidget(unzipedPath, false)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aWidget"))
                .andExpect(jsonPath("element.name").value("myWidgetName"));
    }

    @Test
    public void should_force_a_widget_import() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport = anImportReportFor(aWidget().withId("aWidget").withName("myWidgetName")).build();
        when(artifactBuilder.importWidget(unzipedPath, true)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/widget?force=true").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aWidget"))
                .andExpect(jsonPath("element.name").value("myWidgetName"));
    }

    @Test
    public void should_force_an_uncompleted_import() throws Exception {
        ImportReport expectedReport = anImportReportFor(aWidget().withId("aWidget").withName("myWidgetName")).build();
        final String uuid = "import-uuid";
        when(artifactBuilder.replayImportIgnoringConflicts(uuid)).thenReturn(expectedReport);

        mockMvc.perform(post("/import/" + uuid + "/force"))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aWidget"))
                .andExpect(jsonPath("element.name").value("myWidgetName"));
    }

    @Test
    public void should_cancel_an_import() throws Exception {
        mockMvc.perform(post("/import/import-uuid/cancel"))
                .andExpect(status().isOk());

        verify(artifactBuilder).cancelImport("import-uuid");
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_importing_a_widget() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(artifactBuilder.importWidget(unzipedPath, false)).thenThrow(new ImportException(Type.SERVER_ERROR, "an error messge"));

        mockMvc.perform(multipart("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("an error messge"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_zip_file_could_not_be_opened() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(ZipException.class);

        mockMvc.perform(multipart("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("CANNOT_OPEN_ZIP"))
                .andExpect(jsonPath("message").value("Cannot open zip file"));

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("CANNOT_OPEN_ZIP"))
                .andExpect(jsonPath("message").value("Cannot open zip file"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_unzipping() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(IOException.class);

        mockMvc.perform(multipart("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("Error while unzipping zip file"));

        mockMvc.perform(multipart("/import/page").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("Error while unzipping zip file"));
    }

    @Test
    public void should_import_an_artifact() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport = anImportReportFor(aWidget().withId("aWidget").withName("myWidgetName")).build();
        when(artifactBuilder.importArtifact(unzipedPath, false)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/artifact").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aWidget"))
                .andExpect(jsonPath("element.name").value("myWidgetName"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_model_file_is_not_found_while_importing_an_artifact() throws Exception {
        final ImportException exception = new ImportException(MODEL_NOT_FOUND, "Could not load component, artifact model file not found");
        exception.addInfo("modelfiles", DefaultArtifactBuilder.supportedArtifactTypes.stream().map(type -> type + ".json").collect(toList()));
        when(artifactBuilder.importArtifact(any(), eq(false))).thenThrow(exception);

        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        Files.createDirectory(unzipedPath.resolve("resources"));


        mockMvc.perform(multipart("/import/artifact").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("MODEL_NOT_FOUND"))
                .andExpect(jsonPath("message").value("Could not load component, artifact model file not found"))
                .andExpect(jsonPath("infos.modelfiles", containsInAnyOrder("page.json", "widget.json", "fragment.json")));
    }

    @Test
    public void should_import_a_fragment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport =
                anImportReportFor(aPage().withId("aPage").withName("thePage")).withUUID("UUIDZipFile")
                        .withAdded(aWidget().withId("addedWidget").withName("newWidget"))
                        .withOverwritten(aWidget().withId("overwrittenWidget").withName("oldWidget")).build();
        when(artifactBuilder.importFragment(unzipedPath, false)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/fragment").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("uuid").value("UUIDZipFile"))
                .andExpect(jsonPath("extractedDirName").doesNotExist())
                .andExpect(jsonPath("element.id").value("aPage"))
                .andExpect(jsonPath("element.name").value("thePage"))
                .andExpect(jsonPath("dependencies.added.widget[0].id").value("addedWidget"))
                .andExpect(jsonPath("dependencies.added.widget[0].name").value("newWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].id").value("overwrittenWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].name").value("oldWidget"));
    }

    @Test
    public void should_force_a_fragment_import() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport =
                anImportReportFor(aPage().withId("aPage").withName("thePage")).withUUID("UUIDZipFile")
                        .withAdded(aWidget().withId("addedWidget").withName("newWidget"))
                        .withOverwritten(aWidget().withId("overwrittenWidget").withName("oldWidget")).build();
        when(artifactBuilder.importFragment(unzipedPath, true)).thenReturn(expectedReport);

        mockMvc.perform(multipart("/import/fragment?force=true").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("uuid").value("UUIDZipFile"))
                .andExpect(jsonPath("extractedDirName").doesNotExist())
                .andExpect(jsonPath("element.id").value("aPage"))
                .andExpect(jsonPath("element.name").value("thePage"))
                .andExpect(jsonPath("dependencies.added.widget[0].id").value("addedWidget"))
                .andExpect(jsonPath("dependencies.added.widget[0].name").value("newWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].id").value("overwrittenWidget"))
                .andExpect(jsonPath("dependencies.overwritten.widget[0].name").value("oldWidget"));

    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_importing_a_fragment() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(artifactBuilder.importFragment(unzipedPath, false)).thenThrow(
                new ImportException(ImportException.Type.SERVER_ERROR, "an error message"));

        mockMvc.perform(multipart("/import/fragment").file(file))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("an error message"));
    }
}
