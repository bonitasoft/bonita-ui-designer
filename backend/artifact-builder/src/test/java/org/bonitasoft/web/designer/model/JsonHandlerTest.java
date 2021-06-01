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

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.controller.asset.MalformedJsonException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonHandlerTest {

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Test
    public void should_deserialize_json() throws Exception {
        String json = "{\"name\": \"colin\", \"number\": 31}";

        SimpleObject object = jsonHandler.fromJson(json.getBytes(StandardCharsets.UTF_8), SimpleObject.class);

        assertThat(object.getName()).isEqualTo("colin");
        assertThat(object.getNumber()).isEqualTo(31);
    }

    @Test
    public void should_serialize_object_into_json() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        JSONAssert.assertEquals(new String(jsonHandler.toJson(object)), "{\"name\":\"Vincent\",\"number\":1, \"another\": null, \"id\": \"id\"}", false);
    }

    @Test
    public void should_deserialize_json_to_map() throws Exception {
        String json = "{\"name\": \"walter\", \"lastname\": \"bates\"}";

        Map<String, String> map = jsonHandler.fromJsonToMap(json.getBytes(StandardCharsets.UTF_8));

        assertThat(map.get("name")).isEqualTo("walter");
        assertThat(map.get("lastname")).isEqualTo("bates");
    }

    @Test
    public void should_serialize_map_into_json() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("name", "walter");
        map.put("lastname", "bates");

        JSONAssert.assertEquals(new String(jsonHandler.toJson(map)), "{\"name\":\"walter\",\"lastname\":\"bates\"}", false);
    }

    @Test
    public void should_serialize_object_into_json_using_serialization_view() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        JSONAssert.assertEquals(new String(jsonHandler.toJson(object, JsonViewPersistence.class)), "{\"name\":\"Vincent\",\"number\":1}", false);
    }

    @Test
    public void should_serialize_object_into_human_readable_json_using_serialization_view() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        assertThat(new String(jsonHandler.toPrettyJson(object, JsonViewPersistence.class))).isEqualTo("{" + System.lineSeparator() +
                "  \"name\" : \"Vincent\"," + System.lineSeparator() +
                "  \"number\" : 1" + System.lineSeparator() +
                "}");
    }

    @Test
    public void should_format_json_when_using_pretty_print_on_object() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);

        assertThat(jsonHandler.prettyPrint(object)).isEqualTo("{" + System.lineSeparator() +
                "  \"id\" : \"id\"," + System.lineSeparator() +
                "  \"name\" : \"Vincent\"," + System.lineSeparator() +
                "  \"number\" : 1," + System.lineSeparator() +
                "  \"another\" : null" + System.lineSeparator() +
                "}");
    }

    @Test
    public void should_format_json_when_using_pretty_print_on_json() throws Exception {
        assertThat(jsonHandler.prettyPrint("{\"foo\":\"bar\"}")).isEqualTo("{" + System.lineSeparator() +
                "  \"foo\" : \"bar\"" + System.lineSeparator() +
                "}");
    }

    @Test
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

        assertThatThrownBy(() -> jsonHandler.checkValidJson(notjson.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(MalformedJsonException.class);
    }

    @Test
    public void should_check_that_json_is_valid() throws Exception {
        jsonHandler.checkValidJson("{ \"collection\": [\n] }".getBytes(StandardCharsets.UTF_8));
        jsonHandler.checkValidJson("[1, 2, 3, 4]".getBytes(StandardCharsets.UTF_8));

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
        jsonHandler.checkValidJson(json.getBytes(StandardCharsets.UTF_8));
        // ok - no exception expected
    }

    @Test
    public void should_deserialize_json_with_password() throws Exception {
        String json = "{\"name\": \"colin\", \"number\": 31, \"password\": \"abcd\"}";

        SimpleObject object = jsonHandler.fromJson(json.getBytes(StandardCharsets.UTF_8), SimpleObject.class);

        assertThat(object.getName()).isEqualTo("colin");
        assertThat(object.getNumber()).isEqualTo(31);
        assertThat(object.getPassword()).isEqualTo("abcd");
    }

    @Test
    public void should_serialize_into_object_with_ignore_password() throws Exception {
        SimpleObject object = new SimpleObject("id", "Vincent", 1);
        object.setPassword("abcd");

        String expected = new String(jsonHandler.toJson(object));
        JSONAssert.assertEquals(expected, "{\"name\":\"Vincent\",\"number\":1, \"another\": null, \"id\": \"id\"}", false);
    }

}
