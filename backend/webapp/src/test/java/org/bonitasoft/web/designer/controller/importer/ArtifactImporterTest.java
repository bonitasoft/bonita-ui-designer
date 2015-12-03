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
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.mocks.PageImportMock;
import org.bonitasoft.web.designer.controller.importer.mocks.WidgetImportMock;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactImporterTest {

    private static final String WIDGETS_FOLDER = "widgets";

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private PageRepository pageRepository;
    @Mock
    private JsonFileBasedLoader<Page> pageLoader;
    @Mock
    private WidgetLoader widgetLoader;
    @Mock
    private WidgetRepository widgetRepository;
    private ArtifactImporter<Page> importer;

    private Path pageImportPath;
    private Path unzippedPath;
    private WidgetImportMock wMocks;
    private PageImportMock pMocks;

    @Before
    public void setUp() throws IOException {
        pageImportPath = Files.createTempDirectory(tempDir.toPath(), "pageImport");
        DependencyImporter widgetImporter = new WidgetImporter(widgetLoader, widgetRepository, mock(AssetImporter.class));
        importer = new ArtifactImporter<>(pageRepository, pageLoader, widgetImporter);
        unzippedPath = pageImportPath.resolve("resources");
        Files.createDirectory(unzippedPath);
        when(pageRepository.getComponentName()).thenReturn("page");

        wMocks = new WidgetImportMock(unzippedPath, widgetLoader, widgetRepository);
        pMocks = new PageImportMock(unzippedPath, pageLoader);
    }

    @Test
    public void should_import_artifact_located_on_disk() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        importer.doImport(anImport(pageImportPath));

        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_reset_favorite_information_for_imported_element_and_its_dependencies() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies(
                aWidget().id("aWidget").favorite(),
                aWidget().id("anotherWidget").favorite());
        pMocks.mockPageToBeImported(
                aPage().withId("id").favorite());

        importer.doImport(anImport(pageImportPath));

        ArgumentCaptor<List> widgetCaptor = ArgumentCaptor.forClass(List.class);
        verify(widgetRepository).saveAll(widgetCaptor.capture());
        for (Widget widget : (List<Widget>) widgetCaptor.getValue()) {
            assertThat(widget.isFavorite()).isFalse();
        }
        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(pageRepository).updateLastUpdateAndSave(pageCaptor.capture());
        assertThat(pageCaptor.getValue().isFavorite()).isFalse();
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_imported_dependencies() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = importer.doImport(anImport(pageImportPath));

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden().get("widget")).isEqualTo(overridenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_been_overridden_when_element_already_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(true);

        ImportReport report = importer.doImport(anImport(pageImportPath));

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverridden()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_overridden_when_element_does_not_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);

        ImportReport report = importer.doImport(anImport(pageImportPath));

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverridden()).isFalse();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_been_imported_when_there_are_no_conflict() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = importer.doImport(anImport(pageImportPath));

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_imported_when_there_are_conflict() throws Exception {
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = importer.doImport(anImport(pageImportPath));

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.CONFLICT);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, never()).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_throw_import_exception_when_there_is_no_resource_folder_in_import_path() throws Exception {
        Path newFolder = tempDir.newFolderPath("emptyFolder");

        exception.expect(ImportException.class);
        exception.expect(hasType(UNEXPECTED_ZIP_STRUCTURE));

        importer.doImport(anImport(newFolder));
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_saving_files_in_repository() throws Exception {
        Page page = aPage().withId("aPage").build();
        when(pageLoader.load(any(Path.class), eq("page.json"))).thenReturn(page);
        doThrow(RepositoryException.class).when(pageRepository).updateLastUpdateAndSave(page);

        importer.doImport(anImport(pageImportPath));
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        Files.createDirectory(unzippedPath.resolve(WIDGETS_FOLDER));
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenThrow(new IOException());

        importer.doImport(anImport(pageImportPath));
    }

    private Import anImport(Path path) {
        return new Import(importer, "import-uuid", path);
    }

    @Test
    public void should_force_an_import() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);
        Import anImport = new Import(importer, "import-uuid", pageImportPath);

        ImportReport report = importer.forceImport(anImport);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverridden().get("widget")).isEqualTo(overriddenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isEqualTo(anImport.getUuid());
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }


}
