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
package org.bonitasoft.web.designer.controller.importer;

import java.io.IOException;
import java.io.InputStream;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.bonitasoft.web.designer.rest.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Named
public class MultipartFileImporter {

    private static final Logger logger = LoggerFactory.getLogger(MultipartFileImporter.class);

    public ResponseEntity<ErrorMessage> importFile(MultipartFile file, ArtefactImporter importer){
        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>(new ErrorMessage("Argument", "Part named [file] is needed to successfully import a component"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (isNotZipFile(file)) {
            return new ResponseEntity<>(new ErrorMessage("File content", "Only zip files are allowed when importing a component"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try (InputStream is = file.getInputStream()){
            importer.execute(is);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (ImportException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getType().toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (IOException e) {
            logger.error("Technical error when importing a component", e);
            return new ResponseEntity<>(new ErrorMessage(e), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // some browsers send application/octet-stream for zip files
    // so we check that mimeType is application/zip or application/octet-stream and filename ends with .zip
    private boolean isNotZipFile(MultipartFile file) {
        return !MimeType.APPLICATION_ZIP.matches(file.getContentType())
                && !(MimeType.APPLICATION_OCTETSTREAM.matches(file.getContentType()) && file.getOriginalFilename().endsWith(".zip"));
    }
}
