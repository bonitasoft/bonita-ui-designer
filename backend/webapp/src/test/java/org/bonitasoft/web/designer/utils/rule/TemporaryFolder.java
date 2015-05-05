/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.utils.rule;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TemporaryFolder extends org.junit.rules.TemporaryFolder {

    public Path toPath() {
        return Paths.get(this.getRoot().getPath());
    }

    /**
     * Returns a new fresh folder with the given name under the temporary
     * folder.
     */
    public Path newFolderPath(String... folder) throws IOException {
        return Paths.get(newFolder(folder).getPath());
    }

    /**
     * Returns a new fresh file with the given name under the temporary
     * folder.
     */
    public Path newFilePath(String file) throws IOException {
        return Paths.get(newFile(file).getPath());
    }
}
