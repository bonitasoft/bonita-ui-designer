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
import org.bonitasoft.web.designer.repository.*;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
    private PageService pageService;

    @Mock
    private JsonFileBasedLoader<Page> pageLoader;
    @Mock
    private WidgetFileBasedLoader widgetLoader;
    @Mock
    private WidgetRepository widgetRepository;
    private ArtifactImporter<Page> pageImporter;
    private ArtifactImporter<Widget> widgetImporter;

    private Path pageImportPath;
    private Path widgetImportPath;
    private Path pageUnzippedPath;
    private Path widgetUnzippedPath;
    private WidgetImportMock wMocks;
    private PageImportMock pMocks;

    @Before
    public void setUp() throws IOException {
        pageImportPath = Files.createTempDirectory(tempDir.toPath(), "pageImport");
        widgetImportPath = Files.createTempDirectory(tempDir.toPath(), "widgetImport");
        DependencyImporter widgetDependencyImporter = new WidgetImporter(widgetLoader, widgetRepository, mock(AssetImporter.class));
        pageImporter = new ArtifactImporter<Page>(pageRepository, pageService, pageLoader, widgetDependencyImporter);
        widgetImporter = new ArtifactImporter<Widget>(widgetRepository, null, widgetLoader);
        pageUnzippedPath = pageImportPath.resolve("resources");
        Files.createDirectory(pageUnzippedPath);
        when(pageRepository.getComponentName()).thenReturn("page");
        widgetUnzippedPath = widgetImportPath.resolve("resources");
        Files.createDirectory(widgetUnzippedPath);
        when(widgetRepository.getComponentName()).thenReturn("widget");

        wMocks = new WidgetImportMock(pageUnzippedPath, widgetLoader, widgetRepository);
        pMocks = new PageImportMock(pageUnzippedPath, pageLoader);
    }

    @Test
    public void should_import_artifact_located_on_disk() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        pageImporter.doImport(anImport(pageImportPath));

        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_imported_dependencies() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = pageImporter.doImport(anImport(pageImportPath));

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverwritten().get("widget")).isEqualTo(overridenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_page_is_going_to_be_overwritten_when_element_already_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);

        ImportReport report = pageImporter.doImport(anImport(pageImportPath));

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_widget_is_going_to_be_overwritten_when_element_already_exists_in_repository() throws Exception {
        Widget widget = aWidget().id("aWidget").custom().build();
        Widget existingWidgetInRepo = aWidget().id("aWidget").favorite().custom().build();
        when(widgetLoader.load(widgetUnzippedPath.resolve("widget.json"))).thenReturn(widget);
        when(widgetRepository.exists(widget.getId())).thenReturn(true);
        when(widgetRepository.get(widget.getId())).thenReturn(existingWidgetInRepo);

        ImportReport report = widgetImporter.doImport(anImport(widgetImportPath));

        assertThat(report.getElement()).isEqualTo(existingWidgetInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_overwritten_when_element_does_not_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(null);

        ImportReport report = pageImporter.doImport(anImport(pageImportPath));

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverwritten()).isFalse();
        assertThat(report.getOverwrittenElement()).isNull();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_been_imported_when_there_are_no_conflict() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = pageImporter.doImport(anImport(pageImportPath));

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_imported_when_there_are_conflict() throws Exception {
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        ImportReport report = pageImporter.doImport(anImport(pageImportPath));

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.CONFLICT);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, never()).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_throw_import_exception_when_there_is_no_resource_folder_in_import_path() throws Exception {
        Path newFolder = tempDir.newFolderPath("emptyFolder");

        exception.expect(ImportException.class);
        exception.expect(hasType(UNEXPECTED_ZIP_STRUCTURE));

        pageImporter.doImport(anImport(newFolder));
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_saving_files_in_repository() throws Exception {
        Page page = aPage().withId("aPage").build();
        when(pageLoader.load(pageUnzippedPath.resolve("page.json"))).thenReturn(page);
        doThrow(RepositoryException.class).when(pageRepository).updateLastUpdateAndSave(page);

        pageImporter.doImport(anImport(pageImportPath));
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        Files.createDirectory(pageUnzippedPath.resolve(WIDGETS_FOLDER));
        when(widgetLoader.loadAllCustom(pageUnzippedPath.resolve(WIDGETS_FOLDER))).thenThrow(new IOException());

        pageImporter.doImport(anImport(pageImportPath));
    }

    private Import anImport(Path path) {
        return new Import(pageImporter, "import-uuid", path);
    }

    @Test
    public void should_force_an_import() throws Exception {
        List<Widget> addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);
        Import anImport = new Import(pageImporter, "import-uuid", pageImportPath);

        ImportReport report = pageImporter.forceImport(anImport);

        assertThat(report.getDependencies().getAdded().get("widget")).isEqualTo(addedWidgets);
        assertThat(report.getDependencies().getOverwritten().get("widget")).isEqualTo(overriddenWidgets);
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isEqualTo(anImport.getUUID());
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_force_an_import_overwriting_page() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withId("alreadyHere").withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);
        when(pageRepository.exists(page.getId())).thenReturn(false);
        Import anImport = new Import(pageImporter, "import-uuid", pageImportPath);

        ImportReport report = pageImporter.forceImport(anImport);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("id");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_force_an_import_when_another_page_with_same_id_exist() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        page.setName("myPage");
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withId("alreadyHere").withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);
        when(pageRepository.exists(page.getId())).thenReturn(true);
        when(pageRepository.getNextAvailableId(page.getName())).thenReturn("myPage1");
        Import anImport = new Import(pageImporter, "import-uuid", pageImportPath);

        ImportReport report = pageImporter.forceImport(anImport);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("myPage1");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }
}
