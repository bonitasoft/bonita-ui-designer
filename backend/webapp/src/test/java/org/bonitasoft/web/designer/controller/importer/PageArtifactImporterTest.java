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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.bonitasoft.web.designer.controller.importer.mocks.StreamMock.aStream;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipException;

import org.assertj.core.util.Lists;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.mocks.PageImportMock;
import org.bonitasoft.web.designer.controller.importer.mocks.WidgetImportMock;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
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
public class PageArtifactImporterTest {

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
    private ArtifactImporter<Page> importer;

    private Path pageImportPath;
    private Path unzippedPath;
    private WidgetImportMock wMocks;
    private PageImportMock pMocks;

    @Before
    public void setUp() throws IOException {
        pageImportPath = Files.createTempDirectory(tempDir.toPath(), "pageImport");
        DependencyImporter widgetImporter = new WidgetImporter(widgetLoader, widgetRepository, widgetAssetImporter);
        importer = new ArtifactImporter<>(unzip, pageRepository, pageLoader, widgetImporter);
        when(unzip.unzipInTempDir(any(InputStream.class), anyString())).thenReturn(pageImportPath);
        unzippedPath = pageImportPath.resolve("resources");
        Files.createDirectory(unzippedPath);
        when(pageRepository.getComponentName()).thenReturn("page");

        wMocks = new WidgetImportMock(unzippedPath, widgetLoader, widgetRepository);
        pMocks = new PageImportMock(unzippedPath, pageLoader);
    }

    @Test
    public void should_unzip_stream_then_import_artefacts_for_a_page() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        importer.importFromPath(pageImportPath);

        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository).save(page);
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_imported_dependencies() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden().get("widget")).isEqualTo(overridenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_been_overridden_when_element_already_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(true);

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverridden()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_overridden_when_element_does_not_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverridden()).isFalse();
    }

    @Test
    public void should_delete_created_folder_after_import() throws Exception {
        when(pageLoader.load(any(Path.class), eq("page.json"))).thenReturn(aPage().withId("page-id").build());

        importer.importFromPath(pageImportPath);

        assertThat(Files.exists(pageImportPath)).isFalse();
    }

    @Test
    public void should_delete_created_folder_even_if_an_exception_occurs_when_importing() throws Exception {
        when(pageLoader.load(any(Path.class), eq("page.json"))).thenThrow(ImportException.class);

        try {
            importer.importFromPath(pageImportPath);
            failBecauseExceptionWasNotThrown(ImportException.class);
        } catch (ImportException e) {
            assertThat(Files.exists(pageImportPath)).isFalse();
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

        importer.importFromPath(pageImportPath);
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        Files.createDirectory(unzippedPath.resolve(WIDGETS_FOLDER));
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenThrow(new IOException());

        importer.importFromPath(pageImportPath);
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_imported_dependencies_and_a_extractedDirName_and_force_import_afterwards()
            throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);
        when(unzip.getTemporaryZipPath()).thenReturn(tempDir.toPath());

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden().get("widget")).isEqualTo(overridenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isNotEmpty();
        verify(pageRepository, never()).save(any(Page.class));

        importer.forceExecution(report.getUUID());

        verify(pageRepository).save(any(Page.class));
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_some_overridden_dependencies_and_UUID_and_then_force_save() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = Lists.emptyList();
        Page page = pMocks.mockPageToBeImported();

        when(pageRepository.exists(page.getId())).thenReturn(true);
        when(unzip.getTemporaryZipPath()).thenReturn(tempDir.toPath());

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden()).isNullOrEmpty();
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isNotEmpty();
        verify(pageRepository, never()).save(any(Page.class));

        importer.forceExecution(report.getUUID());
        verify(pageRepository).save(any(Page.class));
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_some_overridden_dependencies_and_UUID_and_cancel() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = Lists.emptyList();
        Page page = pMocks.mockPageToBeImported();

        when(pageRepository.exists(page.getId())).thenReturn(true);
        when(unzip.getTemporaryZipPath()).thenReturn(tempDir.toPath());

        ImportReport report = importer.importFromPath(pageImportPath);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden()).isNullOrEmpty();
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isNotEmpty();
        verify(pageRepository, never()).save(any(Page.class));

        importer.cancelImport(report.getUUID());
        verify(pageRepository, never()).save(any(Page.class));
        assertThat(Files.exists(pageImportPath)).isFalse();
    }

}
