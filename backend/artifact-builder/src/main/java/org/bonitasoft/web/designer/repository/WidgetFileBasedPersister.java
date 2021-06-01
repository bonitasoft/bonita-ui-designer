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
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.HasUUID;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static java.nio.file.Files.write;

/**
 * This Persister is used to manage the persistence logic for a widget. Each of them are serialized in a json file
 */
public class WidgetFileBasedPersister extends JsonFileBasedPersister<Widget> {

    protected static final Logger logger = LoggerFactory.getLogger(WidgetFileBasedPersister.class);

    public WidgetFileBasedPersister(JsonHandler jsonHandler, BeanValidator validator, UiDesignerProperties uiDesignerProperties) {
        super(jsonHandler, validator, uiDesignerProperties);
    }

    /**
     * Save an identifiable object in a json file
     *
     * @throws IOException
     */
    @Override
    public void save(Path directory, Widget content) throws IOException {
        var versionToSet = this.uiDesignerProperties.getVersion();
        // Split version before '_' to avoid patch tagged version compatible
        String[] currentVersion;
        if (versionToSet != null) {
            currentVersion = versionToSet.split("_");
            versionToSet = currentVersion[0];
        }
        content.setDesignerVersionIfEmpty(versionToSet);
        var artifactVersion = content.getArtifactVersion();
        if (artifactVersion == null || Version.isSupportingModelVersion(artifactVersion)) {
            content.setModelVersionIfEmpty(this.uiDesignerProperties.getModelVersion());
        }
        validator.validate(content);

        var templateFileName = content.getId() + ".tpl.html";
        var templatePath = getFilePath(directory, templateFileName);
        var templateValue = content.getTemplate();
        var controllerFileName = content.getId() + ".ctrl.js";
        var controllerPath = getFilePath(directory, controllerFileName);
        var controllerValue = content.getController();
        try {
            if (templateValue != null && !templateValue.startsWith("@")) {
                write(templatePath, templateValue.getBytes(StandardCharsets.UTF_8));
                content.setTemplate("@" + templateFileName);
            }
            if (controllerValue != null && !controllerValue.startsWith("@")) {
                write(controllerPath, controllerValue.getBytes(StandardCharsets.UTF_8));
                content.setController("@" + controllerFileName);
            }
            write(jsonFile(directory, content.getId()), jsonHandler.toPrettyJson(content, JsonViewPersistence.class));
            // restore template and controller memory values once the widget has been persisted
            content.setTemplate(templateValue);
            content.setController(controllerValue);
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

    public Path getFilePath(Path directory, String fileName) {
        return directory.resolve(fileName);
    }

}
