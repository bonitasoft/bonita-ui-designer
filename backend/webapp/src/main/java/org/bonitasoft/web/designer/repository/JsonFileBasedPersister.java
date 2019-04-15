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
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.forceMkdir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewMetadata;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedPersister<T extends Identifiable> {

    @Value("${designer.version}")
    private String version;
    private JacksonObjectMapper objectMapper;
    private BeanValidator validator;
    private static final Logger logger = LoggerFactory.getLogger(JsonFileBasedPersister.class);

    public JsonFileBasedPersister(JacksonObjectMapper objectMapper, BeanValidator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Save an identifiable object in a json file
     * @throws IOException
     */
    public void save(Path directory, T content) throws IOException {
        content.setDesignerVersionIfEmpty(version);
        // Split version before '_' to avoid patch tagged version compatible
        String[] currentVersion;
        if(version != null){
            currentVersion = version.split("_");
            content.setDesignerVersionIfEmpty(currentVersion[0]);
        }
        validator.validate(content);
        try {
            write(jsonFile(directory, content.getId()), objectMapper.toPrettyJson(content, JsonViewPersistence.class));
            Path metadataPath = updateMetadata(directory, content);
            if (content instanceof HasUUID && !StringUtils.isEmpty(((HasUUID) content).getUUID())) {
                //update index used by the studio to find artifacts given their UUID
                saveInIndex(metadataPath, content);
            }
        }
        catch (RuntimeException e){
            //Jackson can sent Runtime exception. We change this one to IO because this exception is caught higher
            throw new IOException(e);
        }
    }

    public Path updateMetadata(Path directory, T content) throws IOException {
        Path metadataPath = directory.getParent().resolve(".metadata");
        forceMkdir(metadataPath.toFile());
        write(jsonFile(metadataPath, content.getId()), objectMapper.toJson(content, JsonViewMetadata.class));
        return metadataPath;
    }

    public void saveInIndex(Path metadataPath, T content) throws IOException {
        Path indexPath = jsonFile(metadataPath, ".index");
        Map<String, String> index;
        if (!indexPath.toFile().exists()) {
            index = new HashMap<>();
        } else {
            byte[] indexFileContent = readAllBytes(indexPath);
            index = objectMapper.fromJsonToMap(indexFileContent);
        }
        index.put(((HasUUID) content).getUUID(), content.getId());
        try{
            write(indexPath, objectMapper.toJson(index));
        }catch (JsonGenerationException e){
            logger.error(format("Cannot generate index for file %s. Maybe a migration is required.",content.getId()));
        }

    }

    protected void removeFromIndex(Path metadataPath, T content) throws IOException {
        Path indexPath = jsonFile(metadataPath, ".index");
        Map<String, String> index;
        if (indexPath.toFile().exists()) {
            byte[] indexFileContent = readAllBytes(indexPath);
            index = objectMapper.fromJsonToMap(indexFileContent);
            String uuidInIndex = ((HasUUID) content).getUUID();
            String idInIndex = index.get(uuidInIndex);
            //only remove from index if the ID bound to the UUID matches the id of the content to delete
            if (content.getId().equals(idInIndex)) {
                index.remove(uuidInIndex);
            }
            write(indexPath, objectMapper.toJson(index));
        }
    }

    /**
     * delete an identifiable object in a json file
     * @throws IOException
     */
    public void delete(Path directory, T content) throws IOException {
        try {
            Path metadataPath = directory.getParent().resolve(".metadata");
            FileUtils.deleteQuietly(metadataPath.resolve(format("%s.json", content.getId())).toFile());
            FileUtils.deleteDirectory(directory.toFile());
            if (content instanceof HasUUID) {
                //update index used by the studio to find artifacts given their UUID
                removeFromIndex(metadataPath, content);
            }
        }
        catch (RuntimeException e){
            //Jackson can sent Runtime exception. We change this one to IO because this exception is caught higher
            throw new IOException(e);
        }
    }

    public Path jsonFile(Path directory, String id) {
        return directory.resolve(id + ".json");
    }

}
