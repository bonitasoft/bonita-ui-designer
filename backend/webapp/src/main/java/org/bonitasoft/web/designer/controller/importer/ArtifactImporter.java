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

import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Loader;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.JSON_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.PAGE_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportPathResolver.resolveImportPath;

public class ArtifactImporter<T extends Identifiable> {

    protected static final Logger logger = LoggerFactory.getLogger(ArtifactImporter.class);

    private Repository<T> repository;
    private Loader<T> loader;
    private final ArtifactService<T> artifactService;
    private DependencyImporter[] dependencyImporters;

    public ArtifactImporter(Repository<T> repository, ArtifactService<T> artifactService, Loader<T> loader, DependencyImporter... dependencyImporters) {
        this.loader = loader;
        this.repository = repository;
        this.artifactService = artifactService;
        this.dependencyImporters = dependencyImporters;
    }

    public ImportReport forceImport(Import anImport) {
        return tryToImportAndGenerateReport(anImport, true);
    }

    public ImportReport doImport(Import anImport) {
        return tryToImportAndGenerateReport(anImport, false);
    }

    /*
     * if uploadedFileDirectory is null, the import is forced
     */
    private ImportReport tryToImportAndGenerateReport(Import anImport, boolean force) {
        Path resources = resolveImportPath(anImport.getPath());
        String modelFile = repository.getComponentName() + ".json";
        try {
            // first load everything
            T element = loader.load(resources.resolve(modelFile));
            Map<DependencyImporter, List<?>> dependencies = loadArtefactDependencies(element, resources);

            ImportReport report = buildReport(element, dependencies);
            report.setUUID(anImport.getUUID());

            if (force || report.doesNotOverwriteElements()) {
                if (force) {
                    //delete element that will be overwritten
                    deleteComponentWithSameUUID(report, element);
                }
                // then save them
                saveComponent(resources, element, dependencies);
                report.setStatus(ImportReport.Status.IMPORTED);
            } else {
                report.setStatus(ImportReport.Status.CONFLICT);
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

    private void saveComponent(Path resources, T element, Map<DependencyImporter, List<?>> dependencies) throws IOException {
        if (element instanceof HasUUID && repository.exists(element.getId())) {
            //If an element with the same ID already exist in the repository, generate a new ID
            String newId = repository.getNextAvailableId(element.getName());
            ((HasUUID) element).setId(newId);
        }
        saveArtefactDependencies(resources, dependencies);
        T savedElement = repository.updateLastUpdateAndSave(element);
        artifactService.migrate(savedElement);

    }

    private void deleteComponentWithSameUUID(ImportReport report, T element) {
        T elementToReplace = (T) report.getOverwrittenElement();
        if (report.isOverwritten() && elementToReplace instanceof HasUUID
                && elementToReplace.getId() != element.getId()) {
            repository.delete(elementToReplace.getId());
        }
    }

    private ImportReport buildReport(T element, Map<DependencyImporter, List<?>> dependencies) {
        ImportReport report = ImportReport.from(element, dependencies);
        if(element instanceof HasUUID) {
            checkIfElementWithUUIDIsOverwritten(element, report);
        } else if (repository.exists(element.getId())) {
            setOverwritten(report, element.getId());
        }
        return report;
    }

    private void checkIfElementWithUUIDIsOverwritten(T element, ImportReport report) {
        String elementUUID = ((HasUUID) element).getUUID();
        T overwrittenElement = null;
        if (!StringUtils.isEmpty(elementUUID)) {
            // if there is already a artifact with this UUID, it will be replaced
            overwrittenElement = repository.getByUUID(elementUUID);
        } else {
            try {
                UUID.fromString(element.getId());
                logger.info(
                        format("Imported artifact %s does not have an uuid attribute but its id has the UUID format."
                                + "The migration will give it an uuid attribute with the same value as its id.", element.getId()));
                // Migration will give the artifact the same uuid attribute as its ID
                // so if there is already an artifact with this UUID, it will be replaced
                overwrittenElement = repository.getByUUID(element.getId());
            } catch (IllegalArgumentException e) {
                logger.info(
                        format("Imported artifact %s does not have an uuid attribute and its id does not have the UUID format."
                                + "A new UUID will be generated by the migration so it will not replace another artifact.", element.getId()));
            }
        }
        if (overwrittenElement != null) {
            setOverwritten(report, overwrittenElement.getId());
        }
    }

    private void setOverwritten(ImportReport report, String elementId) {
        report.setOverwritten(true);
        report.setOverwrittenElement(repository.get(elementId));
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
}
