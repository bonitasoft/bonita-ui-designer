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

import static com.google.common.collect.Collections2.transform;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportPathResolver.resolveImportPath;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;

@Named
public class ImporterResolver {

    private Map<String, ArtifactImporter> artifactImporters;

    @Inject
    public ImporterResolver(@Value("#{artifactImporters}") Map<String, ArtifactImporter> artifactImporters) {
        this.artifactImporters = artifactImporters;
    }

    public ArtifactImporter getImporter(String artifactType) {
        ArtifactImporter importer = artifactImporters.get(artifactType);
        if (importer == null) {
            throw new NotFoundException();
        }
        return importer;
    }

    public ArtifactImporter getImporter(Path extractDir) {
        Path resources = resolveImportPath(extractDir);
        for (Map.Entry<String, ArtifactImporter> entry : artifactImporters.entrySet()) {
            if (Files.exists(resources.resolve(entry.getKey() + ".json"))) {
                return entry.getValue();
            }
        }
        throw anImportException();
    }

    private ImportException anImportException() {
        ImportException importException = new ImportException(MODEL_NOT_FOUND, "Could not load component, artifact model file not found");
        importException.addInfo("modelfiles", getModelFiles());
        return importException;
    }

    private Collection<String> getModelFiles() {
        return transform(artifactImporters.keySet(), new Function<String, String>() {

            @Override
            public String apply(String s) {
                return s + ".json";
            }
        });
    }
}
