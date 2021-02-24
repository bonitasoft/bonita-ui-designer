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
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.export.Exporter;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExportController {

    private Exporter<Page> pageExporter;
    private Exporter<Widget> widgetExporter;
    private Exporter<Fragment> fragmentExporter;

    @Inject
    public ExportController(@Named("pageExporter") Exporter<Page> pageExporter, @Named("widgetExporter") Exporter<Widget> widgetExporter,
                            @Named("fragmentExporter") Exporter<Fragment> fragmentExporter) {
        this.pageExporter = pageExporter;
        this.widgetExporter = widgetExporter;
        this.fragmentExporter = fragmentExporter;
    }

    @RequestMapping(value = "/export/page/{id}")
    public ResponseEntity<String> handleFileExportPage(@PathVariable("id") String id, HttpServletResponse resp) throws Exception {
        return handleExport(pageExporter, id, resp);
    }


    @RequestMapping(value = "/export/widget/{id}")
    public ResponseEntity<String> handleFileExportWidget(@PathVariable("id") String id, HttpServletResponse resp) throws Exception {
        return handleExport(widgetExporter, id, resp);
    }

    @RequestMapping(value = "/export/fragment/{id}")
    public ResponseEntity<String> handleFileExportFragment(@PathVariable("id") String id, HttpServletResponse resp) throws Exception {
        return handleExport(fragmentExporter, id, resp);
    }

    protected ResponseEntity<String> handleExport(Exporter exporter, String id, HttpServletResponse resp) {
        try {
            exporter.handleFileExport(id, resp);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ModelException e) {
            return new ResponseEntity(e.getMessage(), ResourceControllerAdvice.getHttpHeaders(), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity(String.format("Export failed, %s doesn't exist.", id), ResourceControllerAdvice.getHttpHeaders(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity("Export failed. Check logs for more details", ResourceControllerAdvice.getHttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
