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

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedLoader<T extends Identifiable> extends AbstractLoader<T> {

    protected static final Logger logger = LoggerFactory.getLogger(JsonFileBasedLoader.class);

    protected Class<T> type;

    public JsonFileBasedLoader(JsonHandler jsonHandler, Class<T> type) {
        super(jsonHandler, type);
        this.type = type;
    }


    @Override
    public List<T> findByObjectId(Path directory, String objectId) throws IOException {
        List<String> ids = new ArrayList<>();
        ids.add(objectId);
        Map<String, List<T>> map = findByObjectIds(directory, ids);
        List<T> objects = map.get(objectId);
        return Objects.requireNonNullElse(objects, emptyList());
    }

    @Override
    public Map<String, List<T>> findByObjectIds(Path directory, List<String> objectIds) throws IOException {

        if (!exists(directory) || objectIds.isEmpty()) {
            return emptyMap();
        }
        Map<String, List<T>> map = new HashMap<>();

        //Each component has its own files in a directory named with its id
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, "[!.]*")) {
            for (Path componentDirectory : directoryStream) {
                //The directory name is the component id
                var id = componentDirectory.getFileName().toString();
                Path componentFile = componentDirectory.resolve(id + ".json");
                if (!exists(componentFile)) {
                    continue;
                }

                var content = new String(readAllBytes(componentFile), StandardCharsets.UTF_8);
                String contentWithoutSpaces = removeSpaces(content);
                T object;
                try {
                    object = jsonHandler.fromJson(content.getBytes(StandardCharsets.UTF_8), type);
                } catch (IOException ex) {
                    throw new IOException("Json mapping error for " + componentFile, ex);
                }

                for (String objectId : objectIds) {
                    //We consider only another objects
                    var objectPath = resolve(directory, objectId);
                    if (objectPath == null || !objectPath.equals(componentFile) && exists(componentFile)
                            && contentWithoutSpaces.contains(format("\"id\":\"%s\"", objectId))) {
                        List<T> objects = map.computeIfAbsent(objectId, k -> new ArrayList<>());
                        objects.add(object);
                    }
                }
            }
        }
        return map;
    }

    @Override
    public T getByUUID(Path directory, String uuid) throws IOException {
        var indexPath = directory.resolve(".metadata/.index.json");
        if (indexPath.toFile().exists()) {
            var indexFileContent = readAllBytes(indexPath);
            var index = jsonHandler.fromJsonToMap(indexFileContent);
            var objectId = index.get(uuid);
            if (objectId != null) {
                var componentFile = directory.resolve(format("%s/%s.json", objectId, objectId));
                if (componentFile.toFile().exists()) {
                    return jsonHandler.fromJson(readAllBytes(componentFile), type);
                }
            }
        } else {
            logger.info("index file not found [{}]. It will be created when needed of by the migration.", indexPath);
        }
        return null;
    }

    @Override
    public boolean contains(Path directory, String objectId) throws IOException {
        //Object can be of type <E>
        var objectPath = resolve(directory, objectId);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, "*")) {
            for (Path componentDirectory : directoryStream) {
                //The directory name is the component id
                var id = componentDirectory.getFileName().toString();
                var componentFile = componentDirectory.resolve(id + ".json");

                //We consider only another objects
                if (objectPath == null || !objectPath.equals(componentFile)) {
                    var content = readAllBytes(componentFile);
                    if (indexOf(content, objectId.getBytes(StandardCharsets.UTF_8)) >= 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /**
     * Returns the start position of the first occurrence of the specified target within array, or -1 if there is
     * no such occurrence.
     */
    protected int indexOf(byte[] file, byte[] occurence) {
        if (file != null && occurence != null) {
            int len = occurence.length;
            int limit = file.length - len;
            for (var i = 0; i <= limit; i++) {
                var k = 0;
                for (; k < len; k++) {
                    if (occurence[k] != file[i + k]) {
                        break;
                    }
                }
                if (k == len) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String removeSpaces(String text) {
        return text.replaceAll("\\s+", "");
    }
}
