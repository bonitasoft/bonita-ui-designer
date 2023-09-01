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

import com.fasterxml.jackson.core.JsonGenerationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewMetadata;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.forceMkdir;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedPersister<T extends Identifiable> {

    public static final String INDEX_METADATA = ".index";
    protected static final Logger logger = LoggerFactory.getLogger(JsonFileBasedPersister.class);
    protected JsonHandler jsonHandler;
    protected BeanValidator validator;
    protected UiDesignerProperties uiDesignerProperties;

    public JsonFileBasedPersister(JsonHandler jsonHandler, BeanValidator validator, UiDesignerProperties uiDesignerProperties) {
        this.jsonHandler = jsonHandler;
        this.validator = validator;
        this.uiDesignerProperties = uiDesignerProperties;
    }

    /**
     * Save an identifiable object in a json file
     *
     * @throws IOException
     */
    public void save(Path directory, T content) throws IOException {
        var versionToSet = uiDesignerProperties.getVersion();
        // Split version before '_' to avoid patch tagged version compatible
        if (versionToSet != null) {
            var currentVersion = versionToSet.split("_");
            versionToSet = currentVersion[0];
        }

        content.setDesignerVersionIfEmpty(versionToSet);
        var artifactVersion = content.getArtifactVersion();
        if (artifactVersion == null || Version.isSupportingModelVersion(artifactVersion)) {
            content.setModelVersionIfEmpty(uiDesignerProperties.getModelVersion());
        }
        validator.validate(content);
        try {
            write(jsonFile(directory, content.getId()), jsonHandler.toPrettyJson(content, JsonViewPersistence.class));
            var metadataPath = updateMetadata(directory, content);
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
        var metadataPath = directory.getParent().resolve(PageRepository.METADATA);
        forceMkdir(metadataPath.toFile());
        write(jsonFile(metadataPath, content.getId()), jsonHandler.toJson(content, JsonViewMetadata.class));
        return metadataPath;
    }

    public synchronized void saveInIndex(Path metadataPath, T content) throws IOException {
        var indexPath = jsonFile(metadataPath, INDEX_METADATA);
        var index = loadIndex(indexPath);
        try {
            if (getUUIDIfExist(content) != null) {
                index.put(getUUIDIfExist(content), content.getId());
            }
            writeIndexFile(indexPath, index);
        } catch (JsonGenerationException e) {
            logger.error("Cannot generate index for file {}. Maybe a migration is required.", content.getId());
        }
    }

    private synchronized void writeIndexFile(Path indexPath, Map<String, String> index) throws IOException {
        try {
            if (!indexPath.getParent().toFile().exists()) {
                forceMkdir(indexPath.getParent().toFile());
            }
            write(indexPath, jsonHandler.toJson(index));
        } catch (JsonGenerationException e) {
            logger.error("Cannot generate index. Maybe a migration is required.");
        }
    }

    private synchronized Map<String, String> loadIndex(Path indexPath) throws IOException {
        Map<String, String> index = new HashMap<>();
        if (indexPath.toFile().exists()) {
            byte[] indexFileContent = readAllBytes(indexPath);
            try {
                index = jsonHandler.fromJsonToMap(indexFileContent);
            } catch (Exception e) {
                if (indexFileContent.length > 0) { //file is not empty and cannot be parsed
                    logger.error("Failed to parse '{}' file with content:\n{}",
                            indexPath, new String(indexFileContent, StandardCharsets.UTF_8), e);
                }
                //else file is empty, ignore exception
            }
        }
        return index;
    }

    private String getUUIDIfExist(T content) {
        var uuid = ((HasUUID) content).getUUID();
        if (uuid != null && !uuid.isEmpty()) {
            return uuid;
        }
        return null;
    }

    protected void removeFromIndex(Path metadataPath, T content) throws IOException {
        var indexPath = jsonFile(metadataPath, INDEX_METADATA);
        Map<String, String> index;
        if (indexPath.toFile().exists()) {
            var indexFileContent = readAllBytes(indexPath);
            index = jsonHandler.fromJsonToMap(indexFileContent);
            var uuidInIndex = ((HasUUID) content).getUUID();
            var idInIndex = index.get(uuidInIndex);
            //only remove from index if the ID bound to the UUID matches the id of the content to delete
            if (content.getId().equals(idInIndex)) {
                index.remove(uuidInIndex);
            }
            write(indexPath, jsonHandler.toJson(index));
        }
    }

    /**
     * delete an identifiable object in a json file
     *
     * @throws IOException
     */
    public void delete(Path directory, T content) throws IOException {
        try {
            var metadataPath = directory.getParent().resolve(PageRepository.METADATA);
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
        var indexPath = jsonFile(metadataFolder, INDEX_METADATA);
        Map<String, String> refreshingIndex = new HashMap<>();
        pages.forEach(page -> {
            String uuidIfExist = getUUIDIfExist(page);
            if (uuidIfExist != null) {
                refreshingIndex.put(uuidIfExist, page.getId());
            }
        });
        writeIndexFile(indexPath, refreshingIndex);
    }
}
