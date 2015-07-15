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

import static java.nio.file.Files.notExists;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.bonitasoft.web.designer.controller.exception.ImportException.Type.CANNOT_OPEN_ZIP;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipException;

import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.controller.exception.ImportException.Type;
import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Loader;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArtefactImporter<T extends Identifiable> {

    protected static final Logger logger = LoggerFactory.getLogger(ArtefactImporter.class);

    private Unzipper unzip;
    private Repository<T> repository;
    private Loader<T> loader;
    private DependencyImporter[] dependencyImporters;

    public ArtefactImporter(Unzipper unzip, Repository<T> repository, Loader<T> loader, DependencyImporter... dependencyImporters) {
        this.loader = loader;
        this.repository = repository;
        this.unzip = unzip;
        this.dependencyImporters = dependencyImporters;
    }

    public ImportReport execute(InputStream is) {
        Path extractDir = unzip(is);

        Path resources = extractDir.resolve("resources");
        if (notExists(resources)) {
            logger.error("Incorrect zip structure, a resources folder is needed");
            throw new ImportException(Type.UNEXPECTED_ZIP_STRUCTURE, "Incorrect zip structure");
        }

        try {
            // first load everything
            T element = loader.load(resources, repository.getComponentName() + ".json");
            Map<DependencyImporter, List<?>> dependencies = loadArtefactDependencies(element, resources);

            // then save them
            saveArtefactDependencies(resources, dependencies);
            repository.save(element);

            return ImportReport.from(element, dependencies);
        } catch (IOException e) {
            logger.error("Error while getting artefacts, verify the zip content", e);
            throw new ServerImportException("Error while getting artefacts", e);
        } catch (RepositoryException e) {
            logger.error("Error while saving artefacts, verify the zip content", e);
            throw new ServerImportException("Error while saving artefacts", e);
        } finally {
            deleteQuietly(extractDir.toFile());
        }
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
