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
package org.bonitasoft.web.designer.utils.assertions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.api.AbstractAssert;

/**
 * Custom assertj assert to assert things on {@link Path}
 *
 * @author Colin PUY
 */
public class PathAssert extends AbstractAssert<PathAssert, Path> {

    protected PathAssert(Path actual) {
        super(actual, PathAssert.class);
    }

    public PathAssert isEqualTo(String path) {
        isEqualTo(Paths.get(path));
        return this;
    }

    public PathAssert isEqualTo(Path path) {
        if (!actual.toFile().equals(path.toFile())) {
            failWithMessage("Expected path to be %s but was %s", actual, path);
        }
        return this;
    }

    public PathAssert exists() {
        if (!Files.exists(actual)) {
            failWithMessage("Expected path %s to exists on file system but wasn't", actual);
        }
        return this;
    }

    public PathAssert doesNotExists() {
        if (Files.exists(actual)) {
            failWithMessage("Expected path %s to not exists on file system", actual);
        }
        return this;
    }

}
