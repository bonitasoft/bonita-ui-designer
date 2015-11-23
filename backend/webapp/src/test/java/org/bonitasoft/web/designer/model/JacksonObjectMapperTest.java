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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.skyscreamer.jsonassert.JSONAssert;

public class JacksonObjectMapperTest {

    private JacksonObjectMapper objectMapper = new DesignerConfig().objectMapperWrapper();

    @Test
    public void should_deserialize_json() throws Exception {
        String json = "{\"name\": \"colin\", \"number\": 31}";

        SimpleObject object = objectMapper.fromJson(json.getBytes(StandardCharsets.UTF_8), SimpleObject.class);

        assertThat(object.getName()).isEqualTo("colin");
        assertThat(object.getNumber()).isEqualTo(31);
    }

    @Test
    public void should_serialize_object_into_json() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        JSONAssert.assertEquals(new String(objectMapper.toJson(object)), "{\"name\":\"Vincent\",\"number\":1, \"another\": null, \"id\": \"id\"}", false);
    }

    @Test
    public void should_serialize_object_into_json_using_serialization_view() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        JSONAssert.assertEquals(new String(objectMapper.toJson(object, JsonViewPersistence.class)), "{\"name\":\"Vincent\",\"number\":1}", false);
    }

    @Test
    public void should_format_json_when_using_pretty_print_on_object() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        assertThat(objectMapper.prettyPrint(object)).isEqualTo("{" + System.lineSeparator() +
                "  \"id\" : \"id\"," + System.lineSeparator() +
                "  \"name\" : \"Vincent\"," + System.lineSeparator() +
                "  \"number\" : 1" + System.lineSeparator() +
                "}");
    }

    @Test
    public void should_format_json_when_using_pretty_print_on_json() throws Exception {
        assertThat(objectMapper.prettyPrint("{\"foo\":\"bar\"}")).isEqualTo("{" + System.lineSeparator() +
                "  \"foo\" : \"bar\"" + System.lineSeparator() +
                "}");
    }

    @Test(expected = JsonProcessingException.class)
    public void should_check_that_json_is_invalid() throws Exception {
        String notjson = "\n"
                + "    \"fr-FR\": {\n"
                + "        \"Default name\": \"Nom par défaut\",\n"
                + "        \"Hello boys\": \"Coucou les garçooonns\"\n"
                + "    },\n"
                + "    \"ru-RU\": {\n"
                + "        \"Default label\": \"Etiqueta por defecto\",\n"
                + "        \"Hello boys\": \"Bonjourno los garçones\"\n"
                + "    }\n"
                + "}";
        objectMapper.checkValidJson(notjson.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void should_check_that_json_is_valid() throws Exception {
        objectMapper.checkValidJson("{ \"collection\": [\n] }".getBytes(StandardCharsets.UTF_8));
        objectMapper.checkValidJson("[1, 2, 3, 4]".getBytes(StandardCharsets.UTF_8));

        String json = "{\n"
                + "    \"fr-FR\": {\n"
                + "        \"Default name\": \"Nom par défaut\",\n"
                + "        \"Hello boys\": \"Coucou les garçooonns\"\n"
                + "    },\n"
                + "    \"ru-RU\": {\n"
                + "        \"Default label\": \"Etiqueta por defecto\",\n"
                + "        \"Hello boys\": \"Bonjourno los garçones\"\n"
                + "    }\n"
                + "}";
        objectMapper.checkValidJson(json.getBytes(StandardCharsets.UTF_8));
        // ok - no exception expected
    }
}
