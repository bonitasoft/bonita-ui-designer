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

import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Repository<T extends Identifiable> {


    /**
     * Gets component by its id
     */
    T get(String id) throws NotFoundException, RepositoryException;

    /**
     * Gets component by its uuid
     * Return null if the component does not Implement {@link org.bonitasoft.web.designer.model.HasUUID}
     */
    T getByUUID(String uuid) throws NotFoundException, RepositoryException;

    /**
     * Gets component by its path
     * Return null if the component does not Implement {@link org.bonitasoft.web.designer.model.HasUUID}
     *
     * @param path
     * @return
     */
    T get(Path path);

    /**
     * Get all the component in a directory
     */
    List<T> getAll() throws RepositoryException;

    /**
     * Save a component
     */
    T updateLastUpdateAndSave(T toBeSaved) throws RepositoryException;

    /**
     * Save a component but without updating last update date
     */
    T save(T toBeSaved) throws RepositoryException;

    /**
     * Save a list of component
     */
    void saveAll(List<T> toBeSaved) throws RepositoryException;

    /**
     * Deletes component by its id
     */
    void delete(String id) throws NotFoundException, RepositoryException;

    /**
     * Find the path file of a component linked with an id
     */
    Path resolvePath(String id);

    /**
     * Finds all pages which contains an object
     *
     * @throws RepositoryException
     */
    List<T> findByObjectId(String objectId) throws RepositoryException;

    /**
     * Return a Map of object ids, with for each one all pages which contains this object
     *
     * @throws RepositoryException
     */
    Map<String, List<T>> findByObjectIds(List<String> objectIds) throws RepositoryException;

    /**
     * Return the component name
     */
    String getComponentName();

    /**
     * Path where the component files are saved
     */
    Path resolvePathFolder(String id);

    /**
     * Return true if a component with provided id already exists in repository. False otherwise
     */
    boolean exists(String id);

    void walk(FileVisitor<? super Path> visitor) throws IOException;

    void watch(PathListener pathListener) throws IOException;

    T markAsFavorite(String id);

    T unmarkAsFavorite(String id);

    String getNextAvailableId(String name) throws IOException;

    /**
     * @param path
     * @return
     */
    T load(Path path);

    /**
     * @param directory
     * @return
     */
    List<T> loadAll(Path directory) throws IOException;

    List<T> loadAll(Path directory, DirectoryStream.Filter<Path> filter) throws IOException;
}
