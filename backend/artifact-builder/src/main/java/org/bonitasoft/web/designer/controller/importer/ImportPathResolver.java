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

import java.nio.file.Path;

import static java.nio.file.Files.notExists;

public abstract class ImportPathResolver {

    public static Path resolveImportPath(Path extractDir) {
        Path resources = extractDir.resolve("resources");
        if (notExists(resources)) {
            throw new ImportException(ImportException.Type.UNEXPECTED_ZIP_STRUCTURE, "Incorrect zip structure, resources folder is needed");
        }
        return resources;
    }
}
