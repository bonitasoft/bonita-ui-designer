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

import static org.apache.commons.io.FileUtils.deleteQuietly;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Named;

import org.bonitasoft.web.designer.repository.exception.NotFoundException;

@Named
public class ImportStore {

    private Map<String, Import> extractedDirPathMap = new ConcurrentHashMap<>();

    public Import store(ArtifactImporter importer, Path path) {
        path.toFile().deleteOnExit();  // whatever happen, this directory should be deleted when app server exit
        String uuid = UUID.randomUUID().toString();
        Import anImport = new Import(importer, uuid, path);
        extractedDirPathMap.put(uuid, anImport);
        return anImport;
    }

    public void remove(String uuid) {
        Import anImport = extractedDirPathMap.remove(uuid);
        if (anImport != null) {
            deleteQuietly(anImport.getPath().toFile());
        }
    }

    public Import get(String uuid) {
        Import anImport = extractedDirPathMap.get(uuid);
        if (anImport == null) {
            throw new NotFoundException();
        }
        return anImport;
    }
}
