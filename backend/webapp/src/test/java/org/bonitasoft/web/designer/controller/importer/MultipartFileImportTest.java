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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class MultipartFileImportTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ArtifactImporter<Page> importer;

    private MultipartFileImporter multipartFileImporter = new MultipartFileImporter();

    @Test
    public void should_import_zip_file_with_content_type_zip() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());

        multipartFileImporter.importFile(file, importer);

        verify(importer).execute(any(InputStream.class));
    }

    @Test
    public void should_import_zip_file_with_content_type_xzip_compressed() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/x-zip-compressed", "foo".getBytes());

        multipartFileImporter.importFile(file, importer);

        verify(importer).execute(any(InputStream.class));
    }

    @Test
    public void should_import_zip_file_with_content_type_xzip() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/x-zip", "foo".getBytes());

        multipartFileImporter.importFile(file, importer);

        verify(importer).execute(any(InputStream.class));
    }

    @Test
    public void should_import_zip_file_with_content_type_octetstream() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/octet-stream", "foo".getBytes());

        multipartFileImporter.importFile(file, importer);

        verify(importer).execute(any(InputStream.class));
    }

    @Test
    public void should_throw_import_exception_when_file_part_is_not_present_in_request() throws Exception {
        //The file sent is empty
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "".getBytes());

        exception.expect(ImportException.class);
        exception.expectMessage("Part named [file] is needed to successfully import a component");

        multipartFileImporter.importFile(file, importer);
    }

    @Test
    public void should_throw_import_exception_when_file_content_type_is_not_supported() throws Exception {
        //The file sent is not a zio
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "text/html", "foo".getBytes());

        exception.expect(ImportException.class);
        exception.expectMessage("Only zip files are allowed when importing a component");

        multipartFileImporter.importFile(file, importer);
    }

    @Test
    public void should_throw_import_exception_when_file_is_not_a_zip_file_but_has_content_type_octetstream() throws Exception {
        //The file sent is not a zio
        MockMultipartFile file = new MockMultipartFile("file", "myfile.png", "application/octet-stream", "foo".getBytes());

        exception.expect(ImportException.class);
        exception.expectMessage("Only zip files are allowed when importing a component");

        multipartFileImporter.importFile(file, importer);
    }

    @Test
    public void should_throw_import_exception_when_an_import_error_occurs() throws Exception {
        doThrow(new ImportException(ImportException.Type.PAGE_NOT_FOUND, "an Error message")).when(importer).execute(any(InputStream.class));
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());

        exception.expect(ImportException.class);
        exception.expectMessage("an Error message");

        multipartFileImporter.importFile(file, importer);
    }
}
