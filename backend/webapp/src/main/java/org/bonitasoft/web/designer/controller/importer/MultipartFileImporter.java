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
package org.bonitasoft.web.designer.controller.importer;

import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.ZipException;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.springframework.web.multipart.MultipartFile;

@Named
public class MultipartFileImporter {

    private Unzipper unzip;
    private ImportStore importStore;

    @Inject
    public MultipartFileImporter(Unzipper unzip, ImportStore importStore) {
        this.unzip = unzip;
        this.importStore = importStore;
    }

    public ImportReport importFile(MultipartFile file, final ArtifactImporter importer, boolean force) {
        Path extractDir = unzip(file);
        Function importFn = getImportFunction(importer, force);
        return importFromPath(extractDir, importer, importFn);
    }

    private Function getImportFunction(final ArtifactImporter importer, boolean force) {
        if (force) {
            return new Function<Import, ImportReport>() {

                @Override
                public ImportReport apply(Import anImport) {
                    return importer.forceImport(anImport);
                }
            };
        } else {
            return new Function<Import, ImportReport>() {

                @Override
                public ImportReport apply(Import anImport) {
                    return importer.doImport(anImport);
                }
            };
        }
    }

    private ImportReport importFromPath(Path extractDir, ArtifactImporter importer, Function<Import, ImportReport> importFn) {
        Import anImport = importStore.store(importer, extractDir);
        ImportReport report = null;
        try {
            report = importFn.apply(anImport);
        } finally {
            if (report == null || IMPORTED.equals(report.getStatus())) {
                importStore.remove(anImport.getUuid());
            }
        }
        return report;
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

}
