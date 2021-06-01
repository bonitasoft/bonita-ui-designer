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
package org.bonitasoft.web.designer.controller.importer;

import java.util.HashMap;
import java.util.Map;

public class ImportException extends RuntimeException {

    private final Type type;
    private Map<String, Object> infos;

    public ImportException(Type type, String msg) {
        super(msg);
        this.type = type;
    }

    public ImportException(Type type, String msg, Exception e) {
        super(msg, e);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Object> getInfos() {
        return infos;
    }

    public void addInfo(String name, Object value) {
        if (infos == null) {
            infos = new HashMap<>();
        }
        infos.put(name, value);
    }

    public enum Type {
        SERVER_ERROR,
        PAGE_NOT_FOUND,
        MODEL_NOT_FOUND,
        UNEXPECTED_ZIP_STRUCTURE,
        CANNOT_OPEN_ZIP,
        JSON_STRUCTURE
    }
}
