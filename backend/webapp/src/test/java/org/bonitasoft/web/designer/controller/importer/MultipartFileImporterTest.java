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
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipException;

import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFileImporterTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Mock
    private ArtifactImporter<Page> importer;
    @Mock
    private Unzipper unzipper;
    @Mock
    private ImportStore importStore;

    @InjectMocks
    private MultipartFileImporter multipartFileImporter;

    private Path unzzipedPath;

    @Before
    public void setUp() throws Exception {
        unzzipedPath = tempDir.newFolderPath("unzzipedPath");
        when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenReturn(unzzipedPath);
        multipartFileImporter = new MultipartFileImporter(unzipper, importStore);
    }

    private MockMultipartFile aZipFile() {
        return new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
    }

    private Import aMockedImport() {
        Import anImport = new Import(importer, "import-uuid", unzzipedPath);
        when(importStore.store(importer, unzzipedPath)).thenReturn(anImport);
        return anImport;
    }

    @Test
    public void should_import_zip_file() throws Exception {
        ImportReport expectedReport = anImportReportFor(aPage()).withStatus(IMPORTED).build();
        when(importer.doImport(aMockedImport())).thenReturn(expectedReport);

        ImportReport report = multipartFileImporter.importFile(aZipFile(), importer, false);

        assertThat(expectedReport).isEqualTo(report);
    }

    @Test
    public void should_remove_import_from_store_if_report_status_is_imported() throws Exception {
        Import anImport = aMockedImport();
        when(importer.doImport(anImport)).thenReturn(anImportReportFor(aPage()).withStatus(IMPORTED).build());

        multipartFileImporter.importFile(aZipFile(), importer, false);

        verify(importStore).remove(anImport.getUuid());
    }

    @Test
    public void should_remove_import_from_store_if_an_error_occurs_when_importing() throws Exception {
        Import anImport = aMockedImport();
        when(importer.doImport(anImport)).thenThrow(ImportException.class);

        try {
            multipartFileImporter.importFile(aZipFile(), importer, false);
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

        ImportReport report = multipartFileImporter.importFile(aZipFile(), importer, true);

        assertThat(expectedReport).isEqualTo(report);
        verify(importStore).remove(anImport.getUuid());
    }

    @Test
    public void should_throw_import_exception_when_an_import_error_occurs() throws Exception {
        when(importStore.store(importer, unzzipedPath)).thenReturn(new Import(importer, "a-uuid", unzzipedPath));
        doThrow(new ImportException(ImportException.Type.PAGE_NOT_FOUND, "an Error message")).when(importer).doImport(any(Import.class));
        MockMultipartFile file = aZipFile();

        exception.expect(ImportException.class);
        exception.expectMessage("an Error message");

        multipartFileImporter.importFile(file, importer, false);
    }

    @Test
    public void should_throw_import_exception_when_zip_file_could_not_be_opened() throws Exception {
        when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(ZipException.class);
        MockMultipartFile file = aZipFile();

        exception.expect(ImportException.class);
        exception.expect(hasType(CANNOT_OPEN_ZIP));

        multipartFileImporter.importFile(file, importer, false);
    }

    @Test(expected = ServerImportException.class)
    public void should_throw_server_import_exception_when_error_occurs_while_unzipping_zip_file() throws Exception {
        when(unzipper.unzipInTempDir(any(InputStream.class), anyString())).thenThrow(IOException.class);
        MockMultipartFile file = aZipFile();

        multipartFileImporter.importFile(file, importer, false);
    }

}
