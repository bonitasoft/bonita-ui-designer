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

import org.bonitasoft.web.designer.AngularJsArtifactBuilder;
import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.bonitasoft.web.designer.builder.ImportReportBuilder.anImportReportFor;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PathImporterTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Path unzipedPath;

    @Mock
    private ImportStore importStore;

    private ArtifactBuilder artifactBuilder;

    private PageImporter pageImporter;

    private FragmentImporter fragmentImporter;

    private WidgetImporter widgetImporter;

    @Before
    public void setUp() throws Exception {
        unzipedPath = tempDir.newFolderPath("unzipedPath");
        pageImporter = mock(PageImporter.class);
        fragmentImporter = mock(FragmentImporter.class);
        widgetImporter = mock(WidgetImporter.class);
        artifactBuilder = new AngularJsArtifactBuilder(
                mock(Workspace.class),
                mock(WidgetService.class),
                mock(FragmentService.class),
                mock(PageService.class),
                mock(PageExporter.class),
                mock(FragmentExporter.class),
                mock(WidgetExporter.class),
                mock(HtmlGenerator.class),
                importStore,
                pageImporter,
                fragmentImporter,
                widgetImporter
        );
    }

    private Import aMockedImport(AbstractArtifactImporter<?> importer) {
        Import anImport = new Import(importer, "import-uuid", unzipedPath);
        when(importStore.store(importer, unzipedPath)).thenReturn(anImport);
        return anImport;
    }

    @Test
    public void should_import_zip_file() throws Exception {
        ImportReport expectedReport = anImportReportFor(aPage()).withStatus(IMPORTED).build();
        when(pageImporter.tryToImportAndGenerateReport(aMockedImport(pageImporter),false)).thenReturn(expectedReport);

        ImportReport report = artifactBuilder.importPage(unzipedPath,false);

        assertThat(expectedReport).isEqualTo(report);
    }

    @Test
    public void should_remove_import_from_store_if_report_status_is_imported() throws Exception {
        Import anImport = aMockedImport(pageImporter);
        when(pageImporter.tryToImportAndGenerateReport(anImport, false)).thenReturn(anImportReportFor(aPage()).withStatus(IMPORTED).build());

        artifactBuilder.importPage(unzipedPath, false);

        verify(importStore).remove(anImport.getUUID());
    }

    @Test
    public void should_remove_import_from_store_if_an_error_occurs_when_importing() throws Exception {
        Import anImport = aMockedImport(pageImporter);
        when(pageImporter.tryToImportAndGenerateReport(anImport,false)).thenThrow(ImportException.class);

        try {
            artifactBuilder.importPage(unzipedPath, false);
            failBecauseExceptionWasNotThrown(ImportException.class);
        }
        catch (Exception e) {
            verify(importStore).remove(anImport.getUUID());
        }
    }

    @Test
    public void should_force_import_of_zip_file() throws Exception {
        Import anImport = aMockedImport(widgetImporter);
        ImportReport expectedReport = anImportReportFor(WidgetBuilder.aWidget()).withStatus(IMPORTED).build();
        when(widgetImporter.tryToImportAndGenerateReport(anImport,true)).thenReturn(expectedReport);

        ImportReport report = artifactBuilder.importWidget(unzipedPath, true);

        assertThat(expectedReport).isEqualTo(report);
        verify(importStore).remove(anImport.getUUID());
    }

    @Test(expected = ImportException.class)
    public void should_throw_import_exception_when_an_import_error_occurs() throws Exception {
        when(importStore.store(fragmentImporter, unzipedPath)).thenReturn(new Import(fragmentImporter, "a-uuid", unzipedPath));
        doThrow(new ImportException(ImportException.Type.PAGE_NOT_FOUND, "an Error message")).when(fragmentImporter).tryToImportAndGenerateReport(any(),any(Boolean.class));

        artifactBuilder.importFragment(unzipedPath, false);
    }
}
