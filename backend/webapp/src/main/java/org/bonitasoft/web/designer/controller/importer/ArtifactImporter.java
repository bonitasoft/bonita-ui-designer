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

import static java.lang.String.format;
import static java.nio.file.Files.notExists;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.CANNOT_OPEN_ZIP;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.JSON_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.PAGE_NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipException;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.importer.ImportException.Type;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Loader;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtifactImporter<T extends Identifiable> {

    protected static final Logger logger = LoggerFactory.getLogger(ArtifactImporter.class);

    private Unzipper unzip;
    private Repository<T> repository;
    private Loader<T> loader;
    private DependencyImporter[] dependencyImporters;
    private Map<String, String> extractedDirPathMap = new ConcurrentHashMap<>();

    public ArtifactImporter(Unzipper unzip, Repository<T> repository, Loader<T> loader, DependencyImporter... dependencyImporters) {
        this.loader = loader;
        this.repository = repository;
        this.unzip = unzip;
        this.dependencyImporters = dependencyImporters;
    }

    public void cancelImport(String uuid) {
        String dir = extractedDirPathMap.remove(uuid);
        if (StringUtils.isNotBlank(dir)) {
            deleteQuietly(unzip.getTemporaryZipPath().resolve(dir).toFile());
        }
    }

    public ImportReport execute(InputStream is) {
        Path extractDir = unzip(is);
        return importFromPath(extractDir);
    }

    public ImportReport forceExecution(String uuid) {
        if (extractedDirPathMap.get(uuid) != null) {
            Path extractDir = unzip.getTemporaryZipPath().resolve(extractedDirPathMap.get(uuid));
            return forceImportFromPath(extractDir);
        }
        throw new UnsupportedOperationException("Cannot forceExecution import with null UUID");
    }

    public ImportReport forceImportFromPath(Path extractDir) {
        Path resources = getPath(extractDir);
        try {
            return tryToImportAndGenerateReport(resources, null);
        } finally {
            deleteQuietly(extractDir.toFile());
        }
    }

    public ImportReport importFromPath(Path extractDir) {
        Path resources = getPath(extractDir);

        ImportReport importReport = null;
        try {
            importReport = tryToImportAndGenerateReport(resources, extractDir);
            return importReport;
        } finally {
            if (importReport == null || StringUtils.isBlank(importReport.getUUID())) {
                deleteQuietly(extractDir.toFile());
            } else {
                extractDir.toFile().deleteOnExit();
            }
        }
    }

    /*
     * if uploadedFileDirectory is null, the import is forced
     */
    private ImportReport tryToImportAndGenerateReport(Path resources, Path uploadedFileDirectory) {
        String modelFile = repository.getComponentName() + ".json";
        try {
            // first load everything
            T element = loader.load(resources, modelFile);
            Map<DependencyImporter, List<?>> dependencies = loadArtefactDependencies(element, resources);

            ImportReport report = buildReport(element, dependencies);

            if (uploadedFileDirectory == null
                    || (report.doesNotOverrideElements())) {
                // then save them
                saveArtefactDependencies(resources, dependencies);
                repository.save(element);
            } else {
                String uuid = UUID.randomUUID().toString();
                extractedDirPathMap.put(uuid, uploadedFileDirectory.getFileName().toFile().getName());
                report.setUUID(uuid);
            }
            return report;
        } catch (IOException | RepositoryException e) {
            String errorMessage = "Error while " + ((e instanceof IOException) ? "unzipping" : "saving") + " artefacts";
            logger.error(errorMessage + ", check uploaded content", e);
            throw new ServerImportException(errorMessage, e);
        } catch (NotFoundException e) {
            ImportException importException = new ImportException(PAGE_NOT_FOUND,
                    format("Could not load component, unexpected structure in the file [%s]", modelFile), e);
            importException.addInfo("modelfile", modelFile);
            throw importException;
        } catch (JsonReadException e) {
            ImportException importException = new ImportException(JSON_STRUCTURE, format("Could not read json file [%s]", modelFile), e);
            importException.addInfo("modelfile", modelFile);
            throw importException;
        }
    }

    private Path getPath(Path extractDir) {
        Path resources = extractDir.resolve("resources");
        if (notExists(resources)) {
            logger.error("Incorrect zip structure, a resources folder is needed");
            throw new ImportException(Type.UNEXPECTED_ZIP_STRUCTURE, "Incorrect zip structure");
        }
        return resources;
    }

    private ImportReport buildReport(T element, Map<DependencyImporter, List<?>> dependencies) {
        ImportReport report = ImportReport.from(element, dependencies);
        if (repository.exists(element.getId())) {
            report.setOverridden(true);
        }
        return report;
    }

    private void saveArtefactDependencies(Path resources, Map<DependencyImporter, List<?>> map) {
        for (Entry<DependencyImporter, List<?>> entry : map.entrySet()) {
            entry.getKey().save(entry.getValue(), resources);
        }
    }

    private Map<DependencyImporter, List<?>> loadArtefactDependencies(T element, Path resources) throws IOException {
        Map<DependencyImporter, List<?>> map = new HashMap<>();
        for (DependencyImporter importer : dependencyImporters) {
            map.put(importer, importer.load(element, resources));
        }
        return map;
    }

    private Path unzip(InputStream is) {
        try {
            return unzip.unzipInTempDir(is, "pageDesignerImport");
        } catch (ZipException e) {
            logger.error("Cannot open zip file", e);
            throw new ImportException(CANNOT_OPEN_ZIP, "Cannot open zip file", e);
        } catch (IOException e) {
            throw new ServerImportException("Error while unzipping zip file", e);
        }
    }

}
