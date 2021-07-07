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

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import java.time.Instant;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walkFileTree;
import static org.apache.commons.io.FileUtils.copyDirectory;

/**
 * File based repository
 *
 * @author Colin Puy
 */
public abstract class AbstractRepository<T extends Identifiable> implements Repository<T> {

    private final Watcher watcher;

    private final Path templatePath;

    protected JsonFileBasedPersister<T> persister;

    protected Path path;

    protected BeanValidator validator;

    protected Loader<T> loader;

    protected AbstractRepository(Path path, JsonFileBasedPersister<T> persister, Loader<T> loader, BeanValidator validator, Watcher watcher, Path templatePath) {
        this.path = path;
        this.persister = persister;
        this.validator = validator;
        this.loader = loader;
        this.watcher = watcher;
        this.templatePath = templatePath;
    }

    @Override
    public T get(String id) throws NotFoundException, RepositoryException {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id can not be null when loading a component");
        }
        try {
            return loader.get(path.resolve(format("%s/%s.json", id, id)));
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("Non existing %s [%s]", getComponentName(), id));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting %s [%s]", getComponentName(), id), e);
        }
    }

    @Override
    public T getByUUID(String uuid) throws RepositoryException {
        try {
            return loader.getByUUID(path, uuid);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting %s with UUID %s", getComponentName(), uuid), e);
        }
    }

    public T get(Path path) {
        try {
            return loader.get(path);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting %s with path %s", getComponentName(), path), e);
        }
    }

    @Override
    public List<T> getAll() throws RepositoryException {
        try {
            return loader.getAll(path);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting %ss", getComponentName()), e);
        }
    }

    @Override
    public T updateLastUpdateAndSave(T component) throws RepositoryException {
        component.setLastUpdate(Instant.now());
        return save(component);
    }

    @Override
    public T save(T component) throws RepositoryException {
        if (StringUtils.isBlank(component.getId())) {
            throw new IllegalArgumentException(format("Error while saving %s: No id set.", getComponentName()));
        }

        validator.validate(component);
        createComponentDirectory(component);

        try {
            persister.save(resolvePathFolder(component.getId()), component);
            return component;
        } catch (IOException e) {
            throw new RepositoryException(format("Error while saving %s [%s]", getComponentName(), component.getId()), e);
        }
    }

    @Override
    public void saveAll(List<T> toBeSaved) throws RepositoryException {
        if (toBeSaved != null) {
            for (T component : toBeSaved) {
                updateLastUpdateAndSave(component);
            }
        }
    }

    @Override
    public List<T> findByObjectId(String id) throws RepositoryException {
        try {
            return loader.findByObjectId(path, id);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while searching %ss using an object", getComponentName()), e);
        }
    }

    @Override
    public Map<String, List<T>> findByObjectIds(List<String> ids) throws RepositoryException {
        try {
            return loader.findByObjectIds(path, ids);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while searching %ss using an object", getComponentName()), e);
        }
    }

    @Override
    public Path resolvePath(String id) {
        return path.resolve(id);
    }

    @Override
    public void delete(String id) throws NotFoundException, RepositoryException {
        var component = get(id);
        try {
            persister.delete(resolvePathFolder(component.getId()), component);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while deleting %s [%s]", getComponentName(), id), e);
        }
    }

    @Override
    public Path resolvePathFolder(String id) {
        return resolvePath(id);
    }

    public void createComponentDirectory(T component) {
        try {
            var componentDirectory = path.resolve(component.getId());
            if (!Files.exists(componentDirectory)) {
                createDirectories(componentDirectory);
                initializeComponentDirectoryFromTemplate(component);
            }
        } catch (IOException ex) {
            throw new RepositoryException(format("Impossible to create the folder to save the component [ %s ] : %s", component.getId(), path), ex);
        }
    }

    /**
     * Initialize Component directory content from template if any matching template found
     *
     * @param component the component for which, the directory should be initialized
     */
    private void initializeComponentDirectoryFromTemplate(T component) {
        var componentTemplateDir = templatePath.resolve(getComponentName());
        if (Files.exists(componentTemplateDir)) {
            try {
                copyDirectory(componentTemplateDir.toFile(), resolvePath(component.getId()).toFile());
            } catch (IOException e) {
                throw new RepositoryException(format("Failed to initialize %s [%s] from template\"", getComponentName(), component.getId()), e);
            }
        }
    }

    public boolean exists(String id) {
        return Files.exists(resolvePath(id).resolve(id + ".json"));
    }

    @Override
    public void walk(FileVisitor<? super Path> visitor) throws IOException {
        walkFileTree(path, visitor);
    }

    @Override
    public void watch(final PathListener pathListener) throws IOException {
        watcher.watch(path, pathListener);
    }

    @Override
    public T markAsFavorite(String id) {
        var component = get(id);
        component.setFavorite(true);
        return save(component);
    }

    @Override
    public T unmarkAsFavorite(String id) {
        var component = get(id);
        component.setFavorite(false);
        return save(component);
    }

    @Override
    public String getNextAvailableId(String name) {
        try {
            return loader.getNextAvailableObjectId(path, name);
        } catch (IOException e) {
            throw new RepositoryException("Failed to generate object ID", e);
        }
    }

    public JsonFileBasedPersister<T> getPersister() {
        return persister;
    }


    @Override
    public T load(Path path) {
        return loader.load(path);
    }

    @Override
    public List<T> loadAll(Path directory) throws IOException {
        return loader.loadAll(directory);
    }

    @Override
    public List<T> loadAll(Path directory, DirectoryStream.Filter<Path> filter) throws IOException {
        return loader.loadAll(directory, filter);
    }
}
