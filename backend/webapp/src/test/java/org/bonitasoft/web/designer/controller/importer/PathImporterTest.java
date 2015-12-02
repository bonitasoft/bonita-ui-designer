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
import static org.bonitasoft.web.designer.builder.ImportReportBuilder.anImportReportFor;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;

import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PathImporterTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private ArtifactImporter importer;
    @Mock
    private ImportStore importStore;

    @InjectMocks
    private PathImporter pathImporter;

    private Path unzipedPath;

    @Before
    public void setUp() throws Exception {
        unzipedPath = tempDir.newFolderPath("unzipedPath");
        pathImporter = new PathImporter(importStore);
    }

    private Import aMockedImport() {
        Import anImport = new Import(importer, "import-uuid", unzipedPath);
        when(importStore.store(importer, unzipedPath)).thenReturn(anImport);
        return anImport;
    }

    @Test
    public void should_import_zip_file() throws Exception {
        ImportReport expectedReport = anImportReportFor(aPage()).withStatus(IMPORTED).build();
        when(importer.doImport(aMockedImport())).thenReturn(expectedReport);

        ImportReport report = pathImporter.importFromPath(unzipedPath, importer);

        assertThat(expectedReport).isEqualTo(report);
    }

    @Test
    public void should_remove_import_from_store_if_report_status_is_imported() throws Exception {
        Import anImport = aMockedImport();
        when(importer.doImport(anImport)).thenReturn(anImportReportFor(aPage()).withStatus(IMPORTED).build());

        pathImporter.importFromPath(unzipedPath, importer);

        verify(importStore).remove(anImport.getUuid());
    }

    @Test
    public void should_remove_import_from_store_if_an_error_occurs_when_importing() throws Exception {
        Import anImport = aMockedImport();
        when(importer.doImport(anImport)).thenThrow(ImportException.class);

        try {
            pathImporter.importFromPath(unzipedPath, importer);
            failBecauseExceptionWasNotThrown(ImportException.class);
        } catch (Exception e) {
            verify(importStore).remove(anImport.getUuid());
        }
    }

    @Test
    public void should_force_import_of_zip_file() throws Exception {
        Import anImport = aMockedImport();
        ImportReport expectedReport = anImportReportFor(aPage()).withStatus(IMPORTED).build();
        when(importer.forceImport(anImport)).thenReturn(expectedReport);

        ImportReport report = pathImporter.forceImportFromPath(unzipedPath, importer);

        assertThat(expectedReport).isEqualTo(report);
        verify(importStore).remove(anImport.getUuid());
    }

    @Test(expected =  ImportException.class)
    public void should_throw_import_exception_when_an_import_error_occurs() throws Exception {
        when(importStore.store(importer, unzipedPath)).thenReturn(new Import(importer, "a-uuid", unzipedPath));
        doThrow(new ImportException(ImportException.Type.PAGE_NOT_FOUND, "an Error message")).when(importer).doImport(any(Import.class));

        pathImporter.importFromPath(unzipedPath, importer);
    }
}
