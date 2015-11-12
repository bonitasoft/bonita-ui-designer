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

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.walkFileTree;
import static org.apache.commons.io.FileUtils.copyDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileSystemException;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.joda.time.Instant;

/**
 * File based repository
 *
 * @author Colin Puy
 */
public abstract class AbstractRepository<T extends Identifiable> implements Repository<T> {

    protected JsonFileBasedPersister<T> persister;
    protected Path path;
    protected BeanValidator validator;
    protected Loader<T> loader;
    private Watcher watcher;

    public AbstractRepository(Path path, JsonFileBasedPersister<T> persister, Loader<T> loader, BeanValidator validator, Watcher watcher) {
        this.path = path;
        this.persister = persister;
        this.validator = validator;
        this.loader = loader;
        this.watcher = watcher;
    }

    public abstract String getComponentName();

    @Override
    public T get(String id) throws NotFoundException, RepositoryException {
        try {
            return loader.get(path, id);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("Non existing %s [%s]", getComponentName(), id));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting %s [%s]", getComponentName(), id), e);
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
    public boolean containsObject(String id) throws RepositoryException {
        try {
            return loader.contains(path, id);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while searching %ss which used an object", getComponentName()), e);
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
            persister.save(resolvePathFolder(component.getId()), component.getId(), component);
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
            throw new RepositoryException(format("Error while searching %ss which used an object", getComponentName()), e);
        }
    }

    @Override
    public Path resolvePath(String id) {
        return path.resolve(id);
    }

    @Override
    public void delete(String id) throws NotFoundException, RepositoryException {
        T component = get(id);
        try {
            FileUtils.deleteDirectory(path.resolve(component.getId()).toFile());
        } catch (IOException e) {
            throw new RepositoryException(format("Error while deleting %s [%s]", getComponentName(), id), e);
        }
    }

    public Path resolvePathFolder(String id) {
        return resolvePath(id);
    }

    public void createComponentDirectory(T component) {
        try {
            Path componentDirectory = path.resolve(component.getId());
            if (!Files.exists(componentDirectory)) {
                createDirectories(componentDirectory);
                copyTemplate(component, this.getClass().getResource(format("/templates/%s", getComponentName())));
            }
        } catch (IOException ex) {
            throw new RepositoryException(format("Impossible to create the folder to save the component [ %s ] : %s", component.getId(), path), ex);
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
    public void watch(final PathListener pathListener) throws FileSystemException {
        watcher.watch(path, new FileListener() {
            @Override
            public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
                pathListener.pathCreated(watcher.resolve(fileChangeEvent));
            }

            @Override
            public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
                pathListener.pathDeleted(watcher.resolve(fileChangeEvent));
            }

            @Override
            public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
                pathListener.pathChanged(watcher.resolve(fileChangeEvent));
            }
        });
    }

    /**
     * Copy template found at templateUrl into component
     *
     * @param component to update with template
     * @param templateUrl point at component template directory
     */
    private void copyTemplate(T component, URL templateUrl) {
        try {
            if (templateUrl != null) {
                copyDirectory(new File(templateUrl.toURI()), resolvePath(component.getId()).toFile());
            }
        } catch (IOException | URISyntaxException e) {
            throw new RepositoryException(format("Failed to initialize %s [%s] from template\"", getComponentName(), component.getId()), e);
        }
    }

    @Override
    public T markAsFavorite(String id) {
        T component = get(id);
        component.setFavorite(true);
        return save(component);
    }

    @Override
    public T unmarkAsFavorite(String id) {
        T component = get(id);
        component.setFavorite(false);
        return save(component);
    }
}
