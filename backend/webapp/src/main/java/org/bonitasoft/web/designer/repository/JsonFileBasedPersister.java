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
/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.web.designer.repository;

import static java.nio.file.Files.write;

import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.springframework.beans.factory.annotation.Value;

/**
 * This Persister is used to manage the persistence logic for a component. Each of them are serialized in a json file
 */
public class JsonFileBasedPersister<T extends Identifiable> {

    @Value("${designer.version}")
    private String version;
    private JacksonObjectMapper objectMapper;
    private BeanValidator validator;

    public JsonFileBasedPersister(JacksonObjectMapper objectMapper, BeanValidator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Save an identifiable object in a json file
     * @throws IOException
     */
    public void save(Path directory, String id, T content) throws IOException {
        content.setDesignerVersionIfEmpty(version);
        validator.validate(content);
        try {
            write(jsonFile(directory, id), objectMapper.toJson(content, JsonViewPersistence.class));
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
