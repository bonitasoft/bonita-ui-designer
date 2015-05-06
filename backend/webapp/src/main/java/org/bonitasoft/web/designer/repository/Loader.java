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
package org.bonitasoft.web.designer.repository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.model.Identifiable;

/**
 * Depending on the nature of a component we can have several directory where elements
 * are persisted
 */
public interface Loader<T extends Identifiable> {

    T get(Path directory, String id) throws IOException;

    List<T> getAll(Path directory) throws IOException;

    /**
     * Find all the objects which contain an object id. Each object (widget, page, ...) has its
     * own id calculated with an UUID. To increase performances, we just search if the id is in the text
     * files. Only files which contain a reference are deserialized
     *
     * @throws java.io.IOException
     */
    List<T> findByObjectId(Path directory, String objectId) throws IOException;

    /**
     * Find if an object is used in another objects. Each object (widget, page, ...) has its
     * own id calculated with an UUID. To increase performances, we just search if the id is in the text
     * files. Only files which contain a reference are deserialized
     *
     * @throws java.io.IOException
     */
    boolean contains(Path directory, String objectId) throws IOException;

    /**
     * Use to load the component in the path directory
     */
    T load(Path directory, String filename);

}

