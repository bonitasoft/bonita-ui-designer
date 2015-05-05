/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.bonitasoft.web.designer.controller.importer.ArtefactImporter;
import org.bonitasoft.web.designer.controller.importer.MultipartFileImporter;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class ImportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtefactImporter<Page> pageImporter;

    @Mock
    private ArtefactImporter<Widget> widgetImporter;
    @Mock
    private MultipartFileImporter multipartFileImporter;

    @Before
    public void setUp() {
        ImportController importController = new ImportController(multipartFileImporter, pageImporter, widgetImporter);
        mockMvc = standaloneSetup(importController).build();
    }

    @Test
    public void should_import_a_page() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());

        mockMvc.perform(fileUpload("/import/page").file(file));

        verify(multipartFileImporter).importFile(file, pageImporter);
    }

    @Test
    public void should_import_a_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());

        mockMvc.perform(fileUpload("/import/widget").file(file));

        verify(multipartFileImporter).importFile(file, widgetImporter);
    }
}
