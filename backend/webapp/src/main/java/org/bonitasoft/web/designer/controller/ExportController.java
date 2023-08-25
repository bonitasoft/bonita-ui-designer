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

import static java.lang.String.format;
import static org.springframework.http.ContentDisposition.inline;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.util.Optional;

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExportController {

    private final PageService pageService;

    private final FragmentService fragmentService;

    private final WidgetService widgetService;

    private final ArtifactBuilder artifactBuilder;

    @Autowired
    public ExportController(PageService pageService, FragmentService fragmentService, WidgetService widgetService, ArtifactBuilder artifactBuilder) {
        this.pageService = pageService;
        this.fragmentService = fragmentService;
        this.widgetService = widgetService;
        this.artifactBuilder = artifactBuilder;
    }

    @RequestMapping(value = "/export/page/{id}")
    public ResponseEntity<?> handleFileExportPage(@PathVariable("id") String id) throws ModelException, IOException {
        var artifact = pageService.get(id);
        var zipContent = artifactBuilder.build(artifact);
        var zipFileName = getZipFileName(artifact);
        return sendFile(zipFileName, zipContent);
    }


    @RequestMapping(value = "/export/widget/{id}")
    public ResponseEntity<?> handleFileExportWidget(@PathVariable("id") String id) throws ModelException, IOException {
        var artifact = widgetService.get(id);
        var zipContent = artifactBuilder.build(artifact);
        var zipFileName = getZipFileName(artifact);
        return sendFile(zipFileName, zipContent);
    }

    @RequestMapping(value = "/export/fragment/{id}")
    public ResponseEntity<?> handleFileExportFragment(@PathVariable("id") String id) throws ModelException, IOException {
        var artifact = fragmentService.get(id);
        var zipContent = artifactBuilder.build(artifact);
        var zipFileName = getZipFileName(artifact);
        return sendFile(zipFileName, zipContent);
    }

    private ResponseEntity<?> sendFile(String fileName, byte[] content) {
        try {
            var headers = new HttpHeaders();
            headers.setContentDisposition(inline().filename(fileName).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().headers(headers).body(content);
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8).body("Export failed. Check logs for more details");
        }
    }

    protected String getZipFileName(Identifiable identifiable) {
        var maybeName = Optional.ofNullable(identifiable.getName());
        var name = maybeName
                // Clean unwanted chars
                .map(s -> s.replace(' ', '-').replaceAll("[^a-zA-Z0-9-]", ""))
                // FIXME: null as default ??
                .orElse(null);
        var type = identifiable.getType();
        return format("%s-%s.zip", type, name);
    }
}
