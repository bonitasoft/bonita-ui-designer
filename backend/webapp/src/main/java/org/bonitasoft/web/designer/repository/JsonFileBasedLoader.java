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
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.emptyList;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedLoader<T extends Identifiable> extends AbstractLoader<T> {

    protected static final Logger logger = LoggerFactory.getLogger(JsonFileBasedLoader.class);

    private JacksonObjectMapper objectMapper;
    private Class<T> type;

    public JsonFileBasedLoader(JacksonObjectMapper objectMapper, Class<T> type) {
        super(objectMapper, type);
        this.objectMapper = objectMapper;
        this.type = type;
    }


    @Override
    public List<T> findByObjectId(Path directory, String objectId) throws IOException {
        //Object can be of type <E>
        Path objectPath = resolve(directory, objectId);
        List<T> objects = new ArrayList<>();

        if (!exists(directory)) {
            return emptyList();
        }

        //Each component has its own files in a directory named with its id
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, "[!.]*")) {
            for (Path componentDirectory : directoryStream) {
                //The directory name is the component id
                String id = componentDirectory.getFileName().toString();
                Path componentFile = componentDirectory.resolve(id + ".json");

                //We consider only another objects
                if (objectPath == null || !objectPath.equals(componentFile) && exists(componentFile)) {
                    String content = removeSpaces(new String(readAllBytes(componentFile)));
                    if (content.contains(format("\"id\":\"%s\"", objectId))) {
                        objects.add(objectMapper.fromJson(content.getBytes(), type));
                    }
                }
            }
        }
        return objects;
    }

    @Override
    public T getByUUID(Path directory, String uuid) throws IOException {
        Path indexPath = directory.resolve(".metadata/.index.json");
        if (indexPath.toFile().exists()) {
            byte[] indexFileContent = readAllBytes(indexPath);
            Map<String, String> index = objectMapper.fromJsonToMap(indexFileContent);
            String objectId = index.get(uuid);
            if (objectId != null) {
                Path componentFile = directory.resolve(format("%s/%s.json", objectId, objectId));
                return objectMapper.fromJson(readAllBytes(componentFile), type);
            }
        } else {
            logger.info(format("index file not found [%s]. It will be created when needed of by the migration.", indexPath.toString()));
        }
        return null;
    }

    @Override
    public boolean contains(Path directory, String objectId) throws IOException {
        //Object can be of type <E>
        Path objectPath = resolve(directory, objectId);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, "*")) {
            for (Path componentDirectory : directoryStream) {
                //The directory name is the component id
                String id = componentDirectory.getFileName().toString();
                Path componentFile = componentDirectory.resolve(id + ".json");

                //We consider only another objects
                if (objectPath == null || !objectPath.equals(componentFile)) {
                    byte[] content = readAllBytes(componentFile);
                    if (indexOf(content, objectId.getBytes()) >= 0) {
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
            for (int i = 0; i <= limit; i++) {
                int k = 0;
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
