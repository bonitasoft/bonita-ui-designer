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

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.export.Exporter;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExportController {

    private Exporter<Page> pageExporter;
    private Exporter<Widget> widgetExporter;

    @Inject
    public ExportController(@Named("pageExporter") Exporter<Page> pageExporter, @Named("widgetExporter") Exporter<Widget> widgetExporter) {
        this.pageExporter = pageExporter;
        this.widgetExporter = widgetExporter;
    }

    @RequestMapping(value="/export/page/{id}")
    public void handleFileExportPage(@PathVariable("id") String id, HttpServletResponse resp) throws ServletException {
        pageExporter.handleFileExport(id, resp);
    }

    @RequestMapping(value="/export/widget/{id}")
    public void handleFileExportWidget(@PathVariable("id") String id, HttpServletResponse resp) throws ServletException {
        widgetExporter.handleFileExport(id, resp);
    }
}