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

import org.bonitasoft.web.designer.controller.importer.ArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.MultipartFileImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportController {

    private MultipartFileImporter multipartFileImporter;
    private ArtifactImporter<Page> pageImporter;
    private ArtifactImporter<Widget> widgetImporter;

    @Inject
    public ImportController(
            MultipartFileImporter multipartFileImporter,
            @Named("pageImporter") ArtifactImporter<Page> pageImporter,
            @Named("widgetImporter") ArtifactImporter<Widget> widgetImporter) {
        this.multipartFileImporter = multipartFileImporter;
        this.pageImporter = pageImporter;
        this.widgetImporter = widgetImporter;
    }

    /*
     * BS-14106: In IE, json data is not handle properly by browser when content-type is set to application/json.
     * We need to force it to text/plain for browser not trying to save it and pass it correctly to application.
     * Using text/plain as content-type header in response doesn't affect other browsers.
     */
    @RequestMapping(value = "/import/page", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importPage(@RequestParam("file") MultipartFile file) {
        return multipartFileImporter.importFile(file, pageImporter);
    }

    @RequestMapping(value = "/import/page/{uuid}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importPage(@PathVariable("uuid") String uuid) {
        return pageImporter.forceExecution(uuid);
    }

    @RequestMapping(value = "/import/page/cancel/{uuid}", method = RequestMethod.GET)
    public void cancelPageImport(@PathVariable("uuid") String uuid) {
        pageImporter.cancelImport(uuid);
    }

    @RequestMapping(value = "/import/widget", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importWidget(@RequestParam("file") MultipartFile file) {
        return multipartFileImporter.importFile(file, widgetImporter);
    }

    @RequestMapping(value = "/import/widget/{uuid}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importWidget(@PathVariable("uuid") String uuid) {
        return widgetImporter.forceExecution(uuid);
    }

    @RequestMapping(value = "/import/widget/cancel/{uuid}", method = RequestMethod.GET)
    public void cancelWidgetImport(@PathVariable("uuid") String uuid) {
        widgetImporter.cancelImport(uuid);
    }

}
