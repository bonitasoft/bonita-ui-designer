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
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

public interface Repository<T extends Identifiable> {


    /**
     * Gets component by its id
     */
    T get(String id) throws NotFoundException, RepositoryException;

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
     * @throws RepositoryException
     */
    List<T> findByObjectId(String objectId) throws RepositoryException;

    /**
     * Find if an object is used in pages
     * @throws RepositoryException
     */
    boolean containsObject(String id);

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

    void watch(PathListener pathListener) throws FileSystemException;

    T markAsFavorite(String id);

    T unmarkAsFavorite(String id);
}
