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

import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipException;

import org.bonitasoft.web.designer.controller.importer.ArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.Import;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.ImporterResolver;
import org.bonitasoft.web.designer.controller.importer.PathImporter;
import org.bonitasoft.web.designer.controller.importer.ServerImportException;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
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

    private PathImporter pathImporter;
    private ImporterResolver importerResolver;
    private ImportStore importStore;
    private Unzipper unzip;

    @Inject
    public ImportController(
            PathImporter pathImporter,
            ImporterResolver importerResolver,
            ImportStore importStore,
            Unzipper unzip) {
        this.pathImporter = pathImporter;
        this.importerResolver = importerResolver;
        this.importStore = importStore;
        this.unzip = unzip;
    }

    /*
     * BS-14106: In IE, json data is not handle properly by browser when content-type is set to application/json.
     * We need to force it to text/plain for browser not trying to save it and pass it correctly to application.
     * Using text/plain as content-type header in response doesn't affect other browsers.
     */
    @RequestMapping(value = "/import/{artifactType}", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importArtifact(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "force", defaultValue = "false", required = false) boolean force,
            @PathVariable(value = "artifactType") String artifactType) {
        checkFilePartIsPresent(file);
        checkFileIsZip(file);
        Path extractDir = unzip(file);
        ArtifactImporter importer = getArtifactImporter(artifactType, extractDir);
        if (force) {
            return pathImporter.forceImportFromPath(extractDir, importer);
        }
        return pathImporter.importFromPath(extractDir, importer);
    }

    private ArtifactImporter getArtifactImporter(String artifactType, Path extractDir) {
        if ("artifact".equals(artifactType)) {
            return importerResolver.getImporter(extractDir);
        }
        return importerResolver.getImporter(artifactType);
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
        return !MimeType.APPLICATION_ZIP.matches(file.getContentType())
                && !(MimeType.APPLICATION_OCTETSTREAM.matches(file.getContentType()) && file.getOriginalFilename().endsWith(".zip"));
    }

    private Path unzip(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            return unzip.unzipInTempDir(is, "pageDesignerImport");
        } catch (ZipException e) {
            throw new ImportException(CANNOT_OPEN_ZIP, "Cannot open zip file", e);
        } catch (IOException e) {
            throw new ServerImportException("Error while unzipping zip file", e);
        }
    }

    @RequestMapping(value = "/import/{uuid}/force", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ImportReport importPage(@PathVariable("uuid") String uuid) {
        Import anImport = importStore.get(uuid);
        return anImport.getImporter().forceImport(anImport);
    }

    @RequestMapping(value = "/import/{uuid}/cancel", method = RequestMethod.POST)
    public void cancelPageImport(@PathVariable("uuid") String uuid) {
        importStore.remove(uuid);
    }
}
