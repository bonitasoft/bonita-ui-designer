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
package org.bonitasoft.web.designer.model.widget;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.utils.ListUtil.asList;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.junit.Rule;
import org.junit.Test;

public class WidgetTest {

    @Rule
    public TestResource testResource = new TestResource(this.getClass());

    private ObjectMapper objectMapper = new DesignerConfig().objectMapper();

    @Test
    public void jsonview_light_should_only_manage_id_name_and_light_page() throws Exception {
        String json = objectMapper.writerWithView(JsonViewLight.class).writeValueAsString(createAFilledWidget());

        assertEquals(json, "{"
                + "\"id\":\"UUID2\","
                + "\"name\":\"aName\","
                + "\"custom\":false,"
                + "\"favorite\": true,"
                + "\"type\": \"widget\","
                + "\"usedBy\":{"
                    + "\"page\":[{"
                        + "\"id\":\"UUID\","
                        + "\"name\":\"myPage\","
                        + "\"type\":\"page\","
                        + "\"favorite\": false"
                    + "}],"
                    + "\"widget\":[{"
                        + "\"id\":\"UUID\","
                        + "\"name\":\"aName\","
                        + "\"custom\":false,"
                        + "\"type\": \"widget\","
                        + "\"favorite\": false"
                    + "}]"
                + "}}", true);
    }

    @Test
    public void jsonview_persistence_should_manage_all_fields_except_rows_and_containers() throws Exception {
        Widget widgetInitial = createAFilledWidget();
        //We serialize and deserialize our object
        Widget widgetAfterJsonProcessing = objectMapper.readValue(
                objectMapper.writerWithView(JsonViewPersistence.class).writeValueAsString(widgetInitial),
                Widget.class);

        assertThat(widgetAfterJsonProcessing.getName()).isNotNull();
        assertThat(widgetAfterJsonProcessing.getDescription()).isEqualTo("#widget fils d'son père!");
        assertThat(widgetAfterJsonProcessing.getId()).isNotNull();
        assertThat(widgetAfterJsonProcessing.getUsedBy()).isNull();
        assertThat(widgetAfterJsonProcessing.isFavorite()).isTrue();
    }

    @Test
    public void should_convert_widget_id_in_spinal_case() throws Exception {
        String spinalCase = Widget.spinalCase("CUstomDisplayUTCDate");

        assertThat(spinalCase).isEqualTo("c-ustom-display-u-t-c-date");
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_empty() throws Exception {
        Widget widget = new Widget();
        widget.addUsedBy("component", new ArrayList<Identifiable>());

        assertThat(widget.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_null() throws Exception {
        Widget widget = new Widget();
        widget.addUsedBy("component", null);

        assertThat(widget.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components() throws Exception {
        Page page = aPage().build();
        Widget widget = new Widget();
        widget.addUsedBy("component", asList(page));

        assertThat(widget.getUsedBy().get("component")).containsOnly(page);
    }

    @Test
    public void should_have_a_default_type_on_desieralization() throws Exception {
        byte[] content = toByteArray(this.getClass().getResourceAsStream("widget-with-no-type.json"));

        Widget widget = objectMapper.readValue(content, Widget.class);

        assertThat(widget.getType()).isEqualTo("widget");
    }

    /**
     * Create a filled widget with a value for all fields
     */
    private Widget createAFilledWidget() throws Exception {
        Widget widget = aWidget().id("UUID").build();

        Widget widgetSon = aWidget().id("UUID2").build();
        Page page = aFilledPage("UUID");
        page.setName("myPage");
        widgetSon.addUsedBy("page", asList(page));
        widgetSon.addUsedBy("widget", asList(widget));
        widgetSon.setDescription("#widget fils d'son père!");
        widgetSon.setFavorite(true);
        return widgetSon;
    }

}
