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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
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

public abstract class AbstractArtifactImporter<T extends Identifiable> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractArtifactImporter.class);

    protected final Repository<T> repository;

    protected final JsonHandler jsonHandler;

    private final ArtifactService<T> artifactService;

    private final DependencyImporter<?>[] dependencyImporters;

    protected AbstractArtifactImporter(JsonHandler jsonHandler, ArtifactService<T> artifactService, Repository<T> repository, DependencyImporter<?>... dependencyImporters) {
        this.jsonHandler = jsonHandler;
        this.repository = repository;
        this.artifactService = artifactService;
        this.dependencyImporters = dependencyImporters;
    }

    public T load(Path path) {
        try {
            var artifact =  jsonHandler.fromJson(path, getArtifactType(), JsonViewPersistence.class);
            if(artifact instanceof Widget){
                ((Widget) artifact).prepareWidgetToDeserialize(path.getParent());
            }
            return artifact;
        } catch (JsonProcessingException e) {
            throw new JsonReadException(format("Could not read json file [%s]", path.getFileName()), e);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("File not found: [%s]", path.getFileName()));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting component (on file [%s])", path.getFileName()), e);
        }
    }

    protected abstract Class<T> getArtifactType();

    /*
     * if uploadedFileDirectory is null, the import is forced
     */
    public ImportReport tryToImportAndGenerateReport(Import anImport, boolean ignoreConflicts) {
        var resources = resolveImportPath(anImport.getPath());
        String modelFile = repository.getComponentName() + ".json";
        try {
            // first load everything
            var element = load(resources.resolve(modelFile));
            if (element != null && element.getArtifactVersion() != null) {
                MigrationStatusReport status = artifactService.getStatusWithoutDependencies(element);
                if (!status.isCompatible()) {
                    var report = new ImportReport(element, null);
                    report.setStatus(ImportReport.Status.INCOMPATIBLE);
                    return report;
                }
            }

            Map<DependencyImporter, List<?>> dependencies = loadArtefactDependencies(element, resources);
            ImportReport report = buildReport(element, dependencies);
            report.setUUID(anImport.getUUID());

            if (ignoreConflicts || report.doesNotOverwriteElements()) {
                if (ignoreConflicts) {
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
            var importException = new ImportException(PAGE_NOT_FOUND,
                    format("Could not load component, unexpected structure in the file [%s]", modelFile), e);
            importException.addInfo("modelfile", modelFile);
            throw importException;
        } catch (JsonReadException e) {
            var importException = new ImportException(JSON_STRUCTURE, format("Could not read json file [%s]", modelFile), e);
            importException.addInfo("modelfile", modelFile);
            throw importException;
        }
    }

    private void saveComponent(Path resources, T element, Map<DependencyImporter, List<?>> dependencies) throws
            IOException {
        if (element instanceof HasUUID && repository.exists(element.getId())) {
            //If an element with the same ID already exist in the repository, generate a new ID
            String newId = repository.getNextAvailableId(element.getName());
            ((HasUUID) element).setId(newId);
        }
        saveArtefactDependencies(resources, dependencies);
        var savedElement = repository.updateLastUpdateAndSave(element);
        artifactService.migrate(savedElement);

    }

    private void deleteComponentWithSameUUID(ImportReport report, T element) {
        var elementToReplace = (T) report.getOverwrittenElement();
        if (report.isOverwritten()
                && elementToReplace instanceof HasUUID
                // FIXME: when using !equals() instead of != feature is broken, we end up with a folder with a friendly name and a folder with a UUID name !
                && elementToReplace.getId() != element.getId()) {
            repository.delete(elementToReplace.getId());
        }
    }

    private ImportReport buildReport(T element, Map<DependencyImporter, List<?>> dependencies) {
        var report = ImportReport.from(element, dependencies);
        if (element instanceof HasUUID) {
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
                logger.info("Imported artifact {} does not have an uuid attribute but its id has the UUID format."
                                + "The migration will give it an uuid attribute with the same value as its id.", element.getId());
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
        for (DependencyImporter<?> importer : dependencyImporters) {
            map.put(importer, importer.load(element, resources));
        }
        return map;
    }
}
