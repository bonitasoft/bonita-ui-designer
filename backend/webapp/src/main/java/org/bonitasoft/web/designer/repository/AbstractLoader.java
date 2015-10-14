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
import static java.nio.file.Files.readAllBytes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

/**
 * Load a component
 */
public abstract class AbstractLoader<T extends Identifiable> implements Loader<T> {

    private JacksonObjectMapper objectMapper;
    private Class<T> type;

    public AbstractLoader(JacksonObjectMapper objectMapper, Class<T> type) {
        this.objectMapper = objectMapper;
        this.type = type;
    }

    @Override
    public T get(Path directory, String id) throws IOException {
        return objectMapper.fromJson(readAllBytes(jsonFile(directory, id)), type);
    }

    @Override
    public List<T> getAll(Path directory) throws IOException {
        return getAll(directory, "*");
    }

    protected List<T> getAll(Path directory, String glob) throws IOException {
        List<T> objects = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, glob)) {
            for (Path path : directoryStream) {
                String id = getComponentId(path);
                objects.add(get(directory, id));
            }
        }
        return objects;
    }

    @Override
    public T load(Path directory, String filename) {
        try {
            return objectMapper.fromJson(readAllBytes(directory.resolve(filename)), type);
        } catch (JsonProcessingException e) {
            throw new JsonReadException(format("Could not read json file [%s]", filename), e);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("Could not load component, unexpected structure in the file [%s]", filename));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting component (on file [%s])", filename), e);
        }
    }

    public Path jsonFile(Path directory, String id) {
        return directory.resolve(id).resolve(id + ".json");
    }

    private String getComponentId(Path path) {
        return path.getFileName().toString().replaceAll("\\.\\w+", "");
    }
}
