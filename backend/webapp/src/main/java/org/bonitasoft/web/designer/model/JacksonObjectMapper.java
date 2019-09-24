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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Wraps objectMapper to avoid recurrent issue like encoding ones
 */
public class JacksonObjectMapper {

    private ObjectMapper objectMapper;

    public JacksonObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromJson(byte[] bytes, Class<T> type) throws IOException {
        return objectMapper.readValue(bytes, type);
    }

    public <T> T fromJson(byte[] bytes, Class<T> type, Class<?> view) throws IOException {
        return objectMapper.reader().withView(view).forType(type).readValue(bytes);
    }

    public Map<String, String> fromJsonToMap(byte[] bytes) throws IOException {
        TypeFactory factory = TypeFactory.defaultInstance();
        MapType mapType = factory.constructMapType(HashMap.class, String.class, String.class);
        return objectMapper.readValue(bytes, mapType);
    }

    public byte[] toJson(Object object) throws IOException {
        // Use UTF8 to accept any character and have platform-independent files.
        return objectMapper.writeValueAsString(object).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toJson(Object object, Class<?> serializationView) throws IOException {
        // Use UTF8 to accept any character and have platform-independent files.
        return objectMapper.writerWithView(serializationView).writeValueAsString(object).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toJson(Map<String, String> map) throws IOException {
        // Use UTF8 to accept any character and have platform-independent files.
        return objectMapper.writeValueAsString(map).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toPrettyJson(Object object, Class<?> serializationView) throws IOException {
        // Use UTF8 to accept any character and have platform-independent files.
        return objectMapper.writerWithView(serializationView)
                .with(new DefaultPrettyPrinter()
                    .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE))
                .writeValueAsString(object)
                .getBytes(StandardCharsets.UTF_8);
    }

    public String prettyPrint(Object object) throws IOException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }finally {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    public String prettyPrint(String json) throws IOException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fromJson(json.getBytes(StandardCharsets.UTF_8), Object.class));
    }

    public void checkValidJson(byte[] bytes) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(bytes);
        while (parser.nextToken() != null) {
            // do nothing, will throw JsonProcessingException if error occurs
        }
    }

    public <T> T assign(T target, byte[] source) throws IOException {
        return objectMapper.readerForUpdating(target).readValue(source);
    }
}
