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
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.readAllBytes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Load a component
 */
public abstract class AbstractLoader<T extends Identifiable> implements Loader<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLoader.class);
    private final Class<T> type;
    protected JsonHandler jsonHandler;

    protected AbstractLoader(JsonHandler jsonHandler, Class<T> type) {
        this.jsonHandler = jsonHandler;
        this.type = type;
    }

    public T get(Path path) {
        try {
            var artifact = jsonHandler.fromJson(readAllBytes(path), type);
            return applyMetadata(path, artifact);
        } catch (JsonProcessingException e) {
            throw new JsonReadException(format("Could not read json file [%s]", path.getFileName()), e);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("File not found: [%s]", path.getFileName()));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting component (on file [%s])", path.getFileName()), e);
        }
    }

    protected T applyMetadata(Path path, T artifact) throws IOException {
        var metadata = path.getParent().getParent().resolve(format(".metadata/%s.json", path.getParent().getFileName()));
        if (exists(metadata)) {
            try {
                byte[] content = readAllBytes(metadata);
                jsonHandler.checkValidJson(content);
                artifact = jsonHandler.assign(artifact, content);
            }catch (IOException e) {
                logger.warn("Cannot apply metadata to {}. {} is not a valid Json file.", path, metadata);
            }
        }
        return artifact;
    }

    private Optional<T> tryGet(Path path) {
        try {
            return Optional.of(get(path));
        } catch (NotFoundException | JsonReadException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> getAll(Path directory) throws IOException {
        return getAll(directory, "[!.]*");
    }

    protected List<T> getAll(Path directory, String glob) throws IOException {
        List<T> objects = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            for (Path path : directoryStream) {
                String id = getComponentId(path);
                java.util.Optional<T> e = tryGet(directory.resolve(format("%s/%s.json", id, id)));
                if (e.isPresent()) {
                    objects.add(e.get());
                } else {
                    logger.error("{} {} cannot be loaded, your repository may be corrupted", type.getSimpleName(), id);
                }
            }
        }
        return objects;
    }

    @Override
    public String getNextAvailableObjectId(Path directory, String objectName) throws IOException {
        if (!Files.exists(directory.resolve(objectName))) {
            return objectName;
        }
        var pattern = Pattern.compile(objectName + "(\\d+)$");
        String nextAvailableObjectId;
        try (final Stream<Path> directoryStream = Files.list(directory)) {
            OptionalInt maxInt = directoryStream.map(directoryPath -> pattern.matcher(directoryPath.getFileName().toString()))
                    .filter(Matcher::find)
                    .mapToInt(m -> Integer.parseInt(m.group(1)))
                    .max();
            nextAvailableObjectId = objectName + (maxInt.orElse(0) + 1);
        }
        return nextAvailableObjectId;
    }

    @Override
    public T load(Path path) {
        try {
            return jsonHandler.fromJson(readAllBytes(path), type, JsonViewPersistence.class);
        } catch (JsonProcessingException e) {
            throw new JsonReadException(format("Could not read json file [%s]", path.getFileName()), e);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("File not found: [%s]", path.getFileName()));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting component (on file [%s])", path.getFileName()), e);
        }
    }

    public Path resolve(Path directory, String id) {
        return directory.resolve(format("%s/%s.json", id, id));
    }

    private String getComponentId(Path path) {
        return path.getFileName().toString().replaceAll("\\.\\w+", "");
    }

    @Override
    public List<T> loadAll(Path directory) throws IOException {
        return loadAll(directory, "[!.]*");
    }

    protected List<T> loadAll(Path directory, String glob) throws IOException {
        List<T> objects;
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, glob)) {
            objects = loadAll(directory, directoryStream);
        }
        return objects;
    }

    public List<T> loadAll(Path directory, DirectoryStream.Filter<Path> filter) throws IOException {
        List<T> objects;
        try (DirectoryStream<Path> directoryStream = newDirectoryStream(directory, filter)) {
            objects = loadAll(directory, directoryStream);
        }
        return objects;
    }

    private List<T> loadAll(Path directory, DirectoryStream<Path> directoryStream) {
        List<T> objects = new ArrayList<>();
        for (Path path : directoryStream) {
            String id = getComponentId(path);
            objects.add(load(resolve(directory, id)));
        }
        return objects;
    }
}
