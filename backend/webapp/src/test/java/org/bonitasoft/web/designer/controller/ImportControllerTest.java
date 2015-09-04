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
package org.bonitasoft.web.designer.controller;

import static org.bonitasoft.web.designer.builder.ImportReportBuilder.anImportReportFor;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.uiDesignerStandaloneSetup;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.controller.exception.ImportException.Type;
import org.bonitasoft.web.designer.controller.importer.ArtefactImporter;
import org.bonitasoft.web.designer.controller.importer.MultipartFileImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
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
        mockMvc = uiDesignerStandaloneSetup(importController).build();
    }

    @Test
    public void should_import_a_page_with_its_dependencies() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport =
                anImportReportFor(aPage().withId("aPage").withName("thePage"))
                        .withAdded(aWidget().id("addedWidget").name("newWidget"))
                        .withOverridden(aWidget().id("overriddenWidget").name("oldWidget")).build();
        when(multipartFileImporter.importFile(file, pageImporter)).thenReturn(expectedReport);

        mockMvc.perform(fileUpload("/import/page").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aPage"))
                .andExpect(jsonPath("element.name").value("thePage"))
                .andExpect(jsonPath("dependencies.added.widget[0].id").value("addedWidget"))
                .andExpect(jsonPath("dependencies.added.widget[0].name").value("newWidget"))
                .andExpect(jsonPath("dependencies.overridden.widget[0].id").value("overriddenWidget"))
                .andExpect(jsonPath("dependencies.overridden.widget[0].name").value("oldWidget"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_importing_a_page() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(multipartFileImporter.importFile(file, pageImporter)).thenThrow(new ImportException(Type.SERVER_ERROR, "an error messge"));

        mockMvc.perform(fileUpload("/import/page").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("an error messge"));
    }

    @Test
    public void should_import_a_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        ImportReport expectedReport = anImportReportFor(aWidget().id("aWidget").name("myWidgetName")).build() ;
        when(multipartFileImporter.importFile(file, widgetImporter)).thenReturn(expectedReport);

        mockMvc.perform(fileUpload("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("element.id").value("aWidget"))
                .andExpect(jsonPath("element.name").value("myWidgetName"));
    }

    @Test
    public void should_respond_an_error_with_ok_code_when_import_exception_occurs_while_importing_a_widget() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "myfile.zip", "application/zip", "foo".getBytes());
        when(multipartFileImporter.importFile(file, widgetImporter)).thenThrow(new ImportException(Type.SERVER_ERROR, "an error messge"));

        mockMvc.perform(fileUpload("/import/widget").file(file))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("type").value("SERVER_ERROR"))
                .andExpect(jsonPath("message").value("an error messge"));
    }
}
