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

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.importer.ArtefactImporter;
import org.bonitasoft.web.designer.controller.importer.MultipartFileImporter;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rest.ErrorMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportController {

    private MultipartFileImporter multipartFileImporter;
    private ArtefactImporter<Page> pageImporter;
    private ArtefactImporter<Widget> widgetImporter;

    @Inject
    public ImportController(
            MultipartFileImporter multipartFileImporter,
            @Named("pageImporter") ArtefactImporter<Page> pageImporter,
            @Named("widgetImporter") ArtefactImporter<Widget> widgetImporter) {
        this.multipartFileImporter = multipartFileImporter;
        this.pageImporter = pageImporter;
        this.widgetImporter = widgetImporter;
    }

    @RequestMapping(value="/import/page", method= RequestMethod.POST)
    public ResponseEntity<ErrorMessage> importPage(@RequestParam("file") MultipartFile file){
        return multipartFileImporter.importFile(file, pageImporter);
    }

    @RequestMapping(value="/import/widget", method= RequestMethod.POST)
    public ResponseEntity<ErrorMessage> importWidget(@RequestParam("file") MultipartFile file){
        return multipartFileImporter.importFile(file, widgetImporter);
    }
}
