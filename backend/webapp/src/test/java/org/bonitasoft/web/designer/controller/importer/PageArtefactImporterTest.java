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
package org.bonitasoft.web.designer.controller.importer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.exception.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.bonitasoft.web.designer.controller.exception.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipException;

import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageArtefactImporterTest {

    private static final String WIDGETS_FOLDER = "widgets";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    @Mock
    private Unzipper unzip;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private JsonFileBasedLoader<Page> pageLoader;
    @Mock
    private WidgetLoader widgetLoader;
    @Mock
    private WidgetRepository widgetRepository;
    @Mock
    private AssetImporter<Widget> widgetAssetImporter;

    private ArtefactImporter<Page> importer;

    private Path unzippedPath;

    private InputStream aStream() {
        return new ByteArrayInputStream("foo".getBytes());
    }

    private List<Widget> aListOfCustomWidgets() {
        return asList(aWidget().id("aWidget").custom().build(), aWidget().id("anotherWidget").custom().build());
    }

    @Before
    public void setUp() throws IOException {
        DependencyImporter widgetImporter = new WidgetImporter(widgetLoader, widgetRepository, widgetAssetImporter);
        importer = new ArtefactImporter<>(unzip, pageRepository, pageLoader, widgetImporter);
        when(unzip.unzipInTempDir(any(InputStream.class), anyString())).thenReturn(tempDir.toPath());
        unzippedPath = tempDir.newFolderPath("resources");
        when(pageRepository.getComponentName()).thenReturn("page");
    }

    @Test
    public void should_unzip_stream_then_import_artefacts_for_a_page() throws Exception {
        tempDir.newFolderPath("resources", WIDGETS_FOLDER);
        List<Widget> widgets = aListOfCustomWidgets();

        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenReturn(widgets);

        importer.execute(aStream());

        verify(widgetRepository).saveAll(widgets);
    }

    @Test
    public void should_delete_created_folder_after_import() throws Exception {
        when(pageLoader.load(any(Path.class), eq("page.json"))).thenReturn(aPage().withId("page-id").build());

        importer.execute(aStream());

        assertThat(Files.exists(tempDir.toPath())).isFalse();
    }

    @Test
    public void should_delete_created_folder_even_if_an_exception_occurs_when_importing() throws Exception {
        when(pageLoader.load(any(Path.class),eq("page.json"))).thenThrow(ImportException.class);

        try {
            importer.execute(aStream());
            failBecauseExceptionWasNotThrown(ImportException.class);
        } catch (ImportException e) {
            assertThat(Files.exists(tempDir.toPath())).isFalse();
        }
    }

    @Test
    public void should_throw_import_exception_when_zip_file_could_not_be_opened() throws Exception {
        when(unzip.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(ZipException.class);

        exception.expect(ImportException.class);
        exception.expect(hasType(CANNOT_OPEN_ZIP));

        importer.execute(aStream());
    }

    @Test
    public void should_throw_import_exception_when_there_is_no_resource_folder_in_zip() throws Exception {
        Path newFolder = tempDir.newFolderPath("emptyFolder");
        when(unzip.unzipInTempDir(any(InputStream.class), anyString())).thenReturn(newFolder);

        exception.expect(ImportException.class);
        exception.expect(hasType(UNEXPECTED_ZIP_STRUCTURE));

        importer.execute(aStream());
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_unzipping_zip_file() throws Exception {
        when(unzip.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(IOException.class);

        importer.execute(aStream());
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_saving_files_in_repository() throws Exception {
        Page page = aPage().withId("aPage").build();
        when(pageLoader.load(any(Path.class), eq("page.json"))).thenReturn(page);
        doThrow(RepositoryException.class).when(pageRepository).save(page);

        importer.execute(aStream());
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        tempDir.newFolderPath("resources",  WIDGETS_FOLDER);
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenThrow(new IOException());
        importer.execute(aStream());
    }
}
