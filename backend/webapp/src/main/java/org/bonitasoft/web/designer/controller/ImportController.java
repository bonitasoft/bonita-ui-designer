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

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ServerImportException;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.bonitasoft.web.designer.controller.utils.UnZipper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipException;

import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.springframework.http.HttpStatus.CREATED;

@Controller
@RequiredArgsConstructor
public class ImportController {

    private final ArtifactBuilder artifactBuilder;

    private final UnZipper unzip;

    /*
     * BS-14106: In IE, json data is not handle properly by browser when content-type is set to application/json.
     * We need to force it to text/plain for browser not trying to save it and pass it correctly to application.
     * Using text/plain as content-type header in response doesn't affect other browsers.
     */
    @PostMapping(value = "/import/{artifactType}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<ImportReport> importArtifact(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "force", defaultValue = "false", required = false) boolean ignoreConflicts,
                                                       @PathVariable(value = "artifactType") String artifactType) {
        checkFilePartIsPresent(file);
        checkFileIsZip(file);
        var extractDir = unzip(file);

        //FIXME: Could be removed and the artifactType path variable also since it can be guessed from zip content easily.
        //(Require UI work if url is changed)
        ImportReport importReport;
        switch (artifactType) {
            case "page":
                importReport = artifactBuilder.importPage(extractDir, ignoreConflicts);
                break;
            case "fragment":
                importReport = artifactBuilder.importFragment(extractDir, ignoreConflicts);
                break;
            case "widget":
                importReport = artifactBuilder.importWidget(extractDir, ignoreConflicts);
                break;
            case "artifact":
                importReport = artifactBuilder.importArtifact(extractDir, ignoreConflicts);
                break;
            default:
                return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(CREATED).body(importReport);
    }

    @PostMapping(value = "/import/{uuid}/force", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(CREATED)
    @ResponseBody
    public ImportReport importPage(@PathVariable("uuid") String uuid) {
        return artifactBuilder.replayImportIgnoringConflicts(uuid);
    }

    @PostMapping(value = "/import/{uuid}/cancel")
    public void cancelPageImport(@PathVariable("uuid") String uuid) {
        artifactBuilder.cancelImport(uuid);
    }

    private void checkFilePartIsPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Part named [file] is needed to successfully import a component");
        }
    }

    private void checkFileIsZip(MultipartFile file) {
        if (isNotZipFile(file)) {
            throw new IllegalArgumentException("Only zip files are allowed when importing a component");
        }
    }

    // some browsers send application/octet-stream for zip files
    // so we check that mimeType is application/zip or application/octet-stream and filename ends with .zip
    private boolean isNotZipFile(MultipartFile file) {
        if (file == null) return false;
        var contentType = file.getContentType();
        var originalFilename = file.getOriginalFilename();
        return !MimeType.APPLICATION_ZIP.matches(contentType)
                && !(MimeType.APPLICATION_OCTETSTREAM.matches(contentType)
                && originalFilename != null && originalFilename.endsWith(".zip")
        );
    }

    private Path unzip(MultipartFile file) {
        try (var is = file.getInputStream()) {
            return unzip.unzipInTempDir(is, "pageDesignerImport");
        } catch (ZipException e) {
            throw new ImportException(CANNOT_OPEN_ZIP, "Cannot open zip file", e);
        } catch (IOException e) {
            throw new ServerImportException("Error while unzipping zip file", e);
        }
    }

}
