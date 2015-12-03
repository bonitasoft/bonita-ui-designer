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

import static com.google.common.collect.Lists.transform;
import static java.lang.String.format;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.JSON_STRUCTURE;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.PAGE_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportPathResolver.resolveImportPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.designer.controller.importer.dependencies.ComponentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Loader;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

public class ArtifactImporter<T extends Identifiable> {

    protected static final Logger logger = LoggerFactory.getLogger(ArtifactImporter.class);

    private Repository<T> repository;
    private Loader<T> loader;
    private DependencyImporter[] dependencyImporters;

    public ArtifactImporter(Repository<T> repository, Loader<T> loader, DependencyImporter... dependencyImporters) {
        this.loader = loader;
        this.repository = repository;
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
            T element = loader.load(resources, modelFile);
            Map<DependencyImporter, List<?>> dependencies = loadArtefactDependencies(element, resources);

            ImportReport report = buildReport(element, dependencies);
            report.setUUID(anImport.getUuid());

            if (force || report.doesNotOverrideElements()) {
                // then save them
                saveArtefactDependencies(resources, dependencies);
                prepareForImport(element);
                repository.updateLastUpdateAndSave(element);
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

    private ImportReport buildReport(T element, Map<DependencyImporter, List<?>> dependencies) {
        ImportReport report = ImportReport.from(element, dependencies);
        if (repository.exists(element.getId())) {
            report.setOverridden(true);
        }
        return report;
    }

    private void saveArtefactDependencies(Path resources, Map<DependencyImporter, List<?>> map) {
        for (Entry<DependencyImporter, List<?>> entry : map.entrySet()) {
            DependencyImporter importer = entry.getKey();
            List<?> elements = entry.getValue();
            if (importer instanceof ComponentDependencyImporter) {
                elements = prepareForImport((List<? extends Identifiable>) elements);
            }
            importer.save(elements, resources);
        }
    }

    private List<? extends Identifiable> prepareForImport(List<? extends Identifiable> list) {
        return transform(list, new Function<Identifiable, Identifiable>() {

            @Override
            public Identifiable apply(Identifiable input) {
                return prepareForImport(input);
            }
        });
    }

    private Identifiable prepareForImport(Identifiable element) {
        if (element instanceof ResetOnImport) {
            ((ResetOnImport) element).prepareForImport();
        }
        return element;
    }

    private Map<DependencyImporter, List<?>> loadArtefactDependencies(T element, Path resources) throws IOException {
        Map<DependencyImporter, List<?>> map = new HashMap<>();
        for (DependencyImporter importer : dependencyImporters) {
            map.put(importer, importer.load(element, resources));
        }
        return map;
    }

}
