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

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.ArtifactBuilderFactory;
import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.UiDesignerCore;
import org.bonitasoft.web.designer.UiDesignerCoreFactory;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.mocks.PageImportMock;
import org.bonitasoft.web.designer.controller.importer.mocks.WidgetImportMock;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.DefaultPageService;
import org.bonitasoft.web.designer.service.DefaultWidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import java.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.UNEXPECTED_ZIP_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactImporterTest {

    private static final String WIDGETS_FOLDER = "widgets";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock(lenient = true)
    private PageRepository pageRepository;

    @Mock(lenient = true)
    private DefaultPageService pageService;

    @Mock(lenient = true)
    private DefaultWidgetService widgetService;

    @Mock(lenient = true)
    private WidgetRepository widgetRepository;

    @Spy
    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private UiDesignerProperties uiDesignerProperties;

    private Path pageImportPath;

    private Path widgetImportPath;

    private Path pageUnzippedPath;

    private Path widgetUnzippedPath;

    private WidgetImportMock wMocks;

    private PageImportMock pMocks;

    private ArtifactBuilder artifactBuilder;

    @Before
    public void setUp() throws Exception {
        pageImportPath = Files.createTempDirectory(tempDir.toPath(), "pageImport");
        widgetImportPath = Files.createTempDirectory(tempDir.toPath(), "widgetImport");

        uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.getWorkspace().getPages().
                setDir(Files.createTempDirectory(tempDir.toPath(), "pages"));
        uiDesignerProperties.getWorkspace().getWidgets().
                setDir(Files.createTempDirectory(tempDir.toPath(), "widgets"));
        uiDesignerProperties.getWorkspace().getFragments().
                setDir(Files.createTempDirectory(tempDir.toPath(), "fragments"));

        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetRepository.resolvePath(any())).thenAnswer(invocation -> {
            String widgetId = invocation.getArgument(0);
            return tempDir.toPath().resolve(WIDGETS_FOLDER).resolve(widgetId);
        });

        UiDesignerCore core = new UiDesignerCoreFactory(uiDesignerProperties, jsonHandler).create(
                mock(Watcher.class),
                widgetRepository,
                mock(AssetRepository.class),
                mock(FragmentRepository.class),
                pageRepository,
                mock(AssetRepository.class)
        );

        artifactBuilder = new ArtifactBuilderFactory(uiDesignerProperties, jsonHandler, core).create();

        pageUnzippedPath = pageImportPath.resolve("resources");
        Files.createDirectory(pageUnzippedPath);
        when(pageRepository.getComponentName()).thenReturn("page");

        widgetUnzippedPath = widgetImportPath.resolve("resources");
        Files.createDirectory(widgetUnzippedPath);

        wMocks = new WidgetImportMock(pageUnzippedPath, widgetRepository, jsonHandler);
        pMocks = new PageImportMock(pageUnzippedPath, pageRepository, jsonHandler);
    }

    @Test
    public void should_import_artifact_located_on_disk() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.updateLastUpdateAndSave(page)).thenReturn(page);

        artifactBuilder.importPage(pageImportPath, true);

        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_prepare_widget_to_deserialize_on_import_widget() throws Exception {
        Widget widget = spy(aWidget().withId("aWidget").custom().build());
        doReturn(widget).when(jsonHandler).fromJson(any(Path.class), eq(Widget.class), eq(JsonViewPersistence.class));
        when(widgetRepository.updateLastUpdateAndSave(widget)).thenReturn(widget);

        artifactBuilder.importWidget(widgetImportPath, true);

        verify(widget).prepareWidgetToDeserialize(any(Path.class));
    }

    @Test
    public void should_return_an_import_report_containing_imported_element_and_imported_dependencies() throws Exception {
        var addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        var overridenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.updateLastUpdateAndSave(page)).thenReturn(page);

//        ImportReport report = pageImporter.doImport(anImport(pageImportPath));
        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getDependencies().getAdded()).containsEntry("widget", new ArrayList<>(addedWidgets));
        assertThat(report.getDependencies().getOverwritten()).containsEntry("widget", new ArrayList<>(overridenWidgets));
        assertThat(report.getElement()).isEqualTo(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_page_is_going_to_be_overwritten_when_element_already_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_widget_is_going_to_be_overwritten_when_element_already_exists_in_repository() throws Exception {
        Widget widget = aWidget().withId("aWidget").custom().build();
        Widget existingWidgetInRepo = aWidget().withId("aWidget").favorite().custom().build();

        when(widgetRepository.exists(widget.getId())).thenReturn(true);
        when(widgetRepository.get(widget.getId())).thenReturn(existingWidgetInRepo);
        doReturn(existingWidgetInRepo).when(jsonHandler).fromJson(any(Path.class), eq(Widget.class), eq(JsonViewPersistence.class));
        when(widgetRepository.updateLastUpdateAndSave(existingWidgetInRepo)).thenAnswer((Answer<Widget>) invocationOnMock -> {
            Widget widgetArg = invocationOnMock.getArgument(0);
            widgetArg.setLastUpdate(Instant.now());
            return widgetArg;
        });

        final ImportReport report = artifactBuilder.importWidget(widgetImportPath, true);

        assertThat(report.getElement()).isEqualTo(existingWidgetInRepo);
        assertThat(report.isOverwritten()).isTrue();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_overwritten_when_element_does_not_exists_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(null);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.isOverwritten()).isFalse();
        assertThat(report.getOverwrittenElement()).isNull();
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_been_imported_when_there_are_no_conflict() throws Exception {
        List<Widget> widgets = wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository).saveAll(widgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_return_an_import_report_saying_that_element_has_not_been_imported_when_there_are_conflict() throws Exception {
        List<Widget> overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();

        final ImportReport report = artifactBuilder.importPage(pageImportPath, false);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.CONFLICT);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, never()).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_throw_import_exception_when_there_is_no_resource_folder_in_import_path() throws Exception {
        Path newFolder = tempDir.newFolderPath("emptyFolder");

        exception.expect(ImportException.class);
        exception.expect(hasType(UNEXPECTED_ZIP_STRUCTURE));

        final ImportReport report = artifactBuilder.importPage(newFolder, true);
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_saving_files_in_repository() throws Exception {
        Page page = pMocks.mockPageToBeImported(aPage().withId("aPage"));
        when(pageRepository.updateLastUpdateAndSave(page)).thenThrow(RepositoryException.class);

        artifactBuilder.importPage(pageImportPath, true);
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_import_exception_when_an_error_occurs_while_getting_widgets() throws Exception {
        Files.createDirectory(pageUnzippedPath.resolve(WIDGETS_FOLDER));
        wMocks.mockWidgetsAsAddedDependencies();
        pMocks.mockPageToBeImported(aPage().withId("aPage"));
        when(widgetRepository.loadAll(pageUnzippedPath.resolve(WIDGETS_FOLDER),
                WidgetDependencyImporter.CUSTOM_WIDGET_FILTER)).thenThrow(IOException.class);

        artifactBuilder.importPage(pageImportPath, true);
    }

    @Test
    public void should_force_an_import() throws Exception {
        var addedWidgets = wMocks.mockWidgetsAsAddedDependencies();
        var overriddenWidgets = wMocks.mockWidgetsAsOverridenDependencies();
        Page page = pMocks.mockPageToBeImported();
        when(pageRepository.exists(page.getId())).thenReturn(false);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getDependencies().getAdded()).containsEntry("widget", new ArrayList<>(addedWidgets));
        assertThat(report.getDependencies().getOverwritten()).containsEntry("widget", new ArrayList<>(overriddenWidgets));
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getUUID()).isNotBlank();
        assertThat(UUID.fromString(report.getUUID())).isNotNull();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        verify(widgetRepository, never()).saveAll(overriddenWidgets);
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_force_an_import_overwriting_page() throws Exception {
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported();
        Page existingPageInRepo = aPage().withUUID(page.getUUID()).withId("alreadyHere").withName("alreadyHere").build();
        when(pageRepository.getByUUID(page.getUUID())).thenReturn(existingPageInRepo);
        when(pageRepository.get(existingPageInRepo.getId())).thenReturn(existingPageInRepo);
        when(pageRepository.exists(page.getId())).thenReturn(false);

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("id");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
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

        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getOverwrittenElement()).isEqualTo(existingPageInRepo);
        assertThat(report.isOverwritten()).isTrue();
        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.IMPORTED);
        assertThat(page.getId()).isEqualTo("myPage1");
        verify(pageRepository).delete(existingPageInRepo.getId());
        verify(pageRepository, times(3)).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_return_incompatible_status_if_version_is_not_compatible_with_uid() throws Exception {
        uiDesignerProperties.setModelVersion("11.0.0");
        wMocks.mockWidgetsAsAddedDependencies();
        Page page = pMocks.mockPageToBeImported(aPage().withName("myPage").withId("myPage").withModelVersion("12.0.0"));
        lenient().when(pageRepository.getNextAvailableId(page.getName())).thenReturn("myPage1");
        when(pageService.getStatusWithoutDependencies(page)).thenReturn(new MigrationStatusReport(false, false));


        final ImportReport report = artifactBuilder.importPage(pageImportPath, true);

        assertThat(report.getStatus()).isEqualTo(ImportReport.Status.INCOMPATIBLE);
        assertThat(page.getId()).isEqualTo("myPage");
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }
}
