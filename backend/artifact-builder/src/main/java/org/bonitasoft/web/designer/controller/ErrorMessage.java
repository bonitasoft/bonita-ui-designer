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
package org.bonitasoft.web.designer.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of error messages, part of the response when exception occurs
 *
 * @author Colin Puy
 */
public class ErrorMessage {

    private final String type;
    private final String message;
    private Map<String, Object> infos;

    public ErrorMessage(Exception exception) {
        this.type = exception.getClass().getSimpleName();
        this.message = exception.getMessage();
    }

    public ErrorMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void addInfos(Map<String, Object> infos) {
        if (infos != null) {
            if (this.infos == null) {
                this.infos = new HashMap<>();
            }
            this.infos.putAll(infos);
        }
    }

    public void addInfo(String key, Object value) {
        if (this.infos == null) {
            this.infos = new HashMap<>();
        }
        this.infos.put(key, value);
    }

    public Map<String, Object> getInfos() {
        return infos;
    }
}
