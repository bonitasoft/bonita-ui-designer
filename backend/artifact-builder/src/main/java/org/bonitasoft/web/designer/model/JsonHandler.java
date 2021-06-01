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
package org.bonitasoft.web.designer.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Wraps objectMapper to avoid recurrent issue like encoding ones
 */
public interface JsonHandler {
    <T> T fromJson(byte[] bytes, Class<T> type) throws IOException;

    <T> T fromJson(Path jsonFile, Class<T> type, Class<?> view) throws IOException;

    <T> T fromJson(byte[] bytes, Class<T> type, Class<?> view) throws IOException;

    Map<String, String> fromJsonToMap(byte[] bytes) throws IOException;

    byte[] toJson(Object object) throws IOException;

    byte[] toJson(Object object, Class<?> serializationView) throws IOException;

    String toJsonString(Object object) throws IOException;

    String toJsonString(Object object, Class<?> serializationView) throws IOException;

    byte[] toJson(Map<String, String> map) throws IOException;

    byte[] toPrettyJson(Object object, Class<?> serializationView) throws IOException;

    String prettyPrint(Object object) throws IOException;

    String prettyPrint(String json) throws IOException;

    void checkValidJson(byte[] bytes) throws IOException;

    <T> T assign(T target, byte[] source) throws IOException;
}
