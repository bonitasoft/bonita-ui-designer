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

import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.export.Exporter;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class ExportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Exporter<Page> pageExporter;
    @Mock
    private Exporter<Widget> widgetExporter;

    private ExportController exportController;

    @Before
    public void setUp() {
        exportController = new ExportController(pageExporter, widgetExporter);
        mockMvc = mockServer(exportController).build();
    }

    @Test
    public void should_export_a_page() throws Exception {

        mockMvc.perform(get("/export/page/a-page"));

        verify(pageExporter).handleFileExport(eq("a-page"), any(HttpServletResponse.class));
    }

    @Test
    public void should_export_a_widget() throws Exception {

        mockMvc.perform(get("/export/widget/a-widget"));

        verify(widgetExporter).handleFileExport(eq("a-widget"), any(HttpServletResponse.class));
    }
}
