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

import org.bonitasoft.web.designer.repository.exception.NotFoundException;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.io.FileUtils.deleteQuietly;

public class ImportStore {

    private final Map<String, Import> extractedDirPathMap = new ConcurrentHashMap<>();

    public Import store(AbstractArtifactImporter<?> importer, Path path) {
        path.toFile().deleteOnExit();  // whatever happen, this directory should be deleted when app server exit
        var uuid = UUID.randomUUID().toString();
        var anImport = new Import(importer, uuid, path);
        extractedDirPathMap.put(uuid, anImport);
        return anImport;
    }

    public void remove(String uuid) {
        var anImport = extractedDirPathMap.remove(uuid);
        if (anImport != null) {
            deleteQuietly(anImport.getPath().toFile());
        }
    }

    public Import get(String uuid) {
        var anImport = extractedDirPathMap.get(uuid);
        if (anImport == null) {
            throw new NotFoundException();
        }
        return anImport;
    }
}
