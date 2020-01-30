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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.core.JsonGenerationException;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedPersister<T extends Identifiable> {

    public static final String INDEX_METADATA = ".index";
    @Value("${designer.version}")
    protected String version;
    protected JacksonObjectMapper objectMapper;
    protected BeanValidator validator;
    protected static final Logger logger = LoggerFactory.getLogger(JsonFileBasedPersister.class);

    public JsonFileBasedPersister(JacksonObjectMapper objectMapper, BeanValidator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Save an identifiable object in a json file
     *
     * @throws IOException
     */
    public void save(Path directory, T content) throws IOException {
        String versionToSet = version;
        // Split version before '_' to avoid patch tagged version compatible
        String[] currentVersion;
        if (versionToSet != null) {
            currentVersion = version.split("_");
            versionToSet = currentVersion[0];
        }
        content.setDesignerVersionIfEmpty(versionToSet);
        validator.validate(content);
        try {
            write(jsonFile(directory, content.getId()), objectMapper.toPrettyJson(content, JsonViewPersistence.class));
            Path metadataPath = updateMetadata(directory, content);
            if (content instanceof HasUUID && !StringUtils.isEmpty(((HasUUID) content).getUUID())) {
                //update index used by the studio to find artifacts given their UUID
                saveInIndex(metadataPath, content);
            }
        } catch (RuntimeException e) {
            //Jackson can sent Runtime exception. We change this one to IO because this exception is caught higher
            throw new IOException(e);
        }
    }

    public Path updateMetadata(Path directory, T content) throws IOException {
        Path metadataPath = directory.getParent().resolve(PageRepository.METADATA);
        forceMkdir(metadataPath.toFile());
        write(jsonFile(metadataPath, content.getId()), objectMapper.toJson(content, JsonViewMetadata.class));
        return metadataPath;
    }

    public synchronized void saveInIndex(Path metadataPath, T content) throws IOException {
        Path indexPath = jsonFile(metadataPath, INDEX_METADATA);
        Map<String, String> index = loadIndex(indexPath);
        try {
            if (getUUIDIfExist(content) != null) {
                index.put(getUUIDIfExist(content), content.getId());
            }
            writeIndexFile(indexPath, index);
        } catch (JsonGenerationException e) {
            logger.error(format("Cannot generate index for file %s. Maybe a migration is required.", content.getId()));
        }
    }

    private synchronized void writeIndexFile(Path indexPath, Map<String, String> index) throws IOException {
        try {
            write(indexPath, objectMapper.toJson(index));
        } catch (JsonGenerationException e) {
            logger.error(format("Cannot generate index. Maybe a migration is required."));
        }
    }

    private synchronized Map<String, String> loadIndex(Path indexPath) throws IOException {
        Map<String, String> index = new HashMap<>();
        if (indexPath.toFile().exists()) {
            byte[] indexFileContent = readAllBytes(indexPath);
            try {
                index = objectMapper.fromJsonToMap(indexFileContent);
            } catch (Exception e) {
                if (indexFileContent.length > 0) {  //file is not empty and cannot be parsed
                    logger.error(String.format("Failed to parse '%s' file with content:\n%s", indexPath, new String(indexFileContent)), e);
                }
                //else file is empty, ignore exception
            }
        }
        return index;
    }

    private String getUUIDIfExist(T content) {
        String uuid = ((HasUUID) content).getUUID();
        if (uuid != null && !uuid.isEmpty()) {
            return uuid;
        }
        return null;
    }

    protected void removeFromIndex(Path metadataPath, T content) throws IOException {
        Path indexPath = jsonFile(metadataPath, INDEX_METADATA);
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
     *
     * @throws IOException
     */
    public void delete(Path directory, T content) throws IOException {
        try {
            Path metadataPath = directory.getParent().resolve(PageRepository.METADATA);
            FileUtils.deleteQuietly(metadataPath.resolve(format("%s.json", content.getId())).toFile());
            FileUtils.deleteDirectory(directory.toFile());
            if (content instanceof HasUUID) {
                //update index used by the studio to find artifacts given their UUID
                removeFromIndex(metadataPath, content);
            }
        } catch (RuntimeException e) {
            //Jackson can sent Runtime exception. We change this one to IO because this exception is caught higher
            throw new IOException(e);
        }
    }

    public Path jsonFile(Path directory, String id) {
        return directory.resolve(id + ".json");
    }

    public synchronized void refreshIndexing(Path metadataFolder, List<T> pages) throws IOException {
        Path indexPath = jsonFile(metadataFolder, INDEX_METADATA);
        Map<String, String> refreshingIndex = new HashMap<>();
        pages.forEach(page -> {
            String uuidIfExist = getUUIDIfExist(page);
            if (uuidIfExist!= null) {
                refreshingIndex.put(uuidIfExist, page.getId());
            }
        });
        writeIndexFile(indexPath, refreshingIndex);
    }
}
