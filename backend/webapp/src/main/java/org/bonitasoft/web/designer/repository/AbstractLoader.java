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

import static java.nio.file.Files.readAllBytes;
import static org.bonitasoft.web.designer.controller.exception.ImportException.Type.JSON_STRUCTURE;
import static org.bonitasoft.web.designer.controller.exception.ImportException.Type.PAGE_NOT_FOUND;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;

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
        }
        catch (JsonParseException e) {
            throw new ImportException(JSON_STRUCTURE, String.format("Could not read json file [%s]", filename), e);
        }
        catch (NoSuchFileException e) {
            throw new ImportException(PAGE_NOT_FOUND, String.format("Could not load component, unexpected structure in the file [%s]", filename), e);
        }
        catch (IOException e) {
            throw new ServerImportException(String.format("Error while getting component (on file [%s])", filename), e);
        }
    }

    public Path jsonFile(Path directory, String id) {
        return directory.resolve(id).resolve(id + ".json");
    }

    private String getComponentId(Path path) {
        return path.getFileName().toString().replaceAll("\\.\\w+", "");
    }
}
