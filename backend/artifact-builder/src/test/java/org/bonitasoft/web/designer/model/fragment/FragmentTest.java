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
package org.bonitasoft.web.designer.model.fragment;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.utils.ListUtil.asList;

@RunWith(MockitoJUnitRunner.class)
public class FragmentTest {

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Test
    public void jsonview_light_should_only_manage_id_name_hasValidationErrors_and_light_page() throws Exception {
        byte[] data = jsonHandler.toJson(createAFilledFragment(), JsonViewLight.class);
        String json = new String(data, StandardCharsets.UTF_8);
        JSONAssert.assertEquals(json,
                "{\"id\":\"ID\","
                        + "\"name\":\"name\","
                        + "\"type\":\"fragment\","
                        + "\"favorite\":false,"
                        + "\"hasValidationError\":false,"
                        + "\"status\": {\"compatible\":true, \"migration\":true},"
                        + "\"usedBy\":{"
                        + "\"page\":[{\"id\":\"ID\",\"uuid\":\"UUID\",\"name\":\"myPage\",\"type\":\"page\", \"favorite\":false, \"hasValidationError\":false, \"status\": {\"compatible\":true, \"migration\":true}}],"
                        + "\"fragment\":[{\"id\":\"ID\",\"name\":\"father\",\"type\":\"fragment\", \"favorite\":true, \"hasValidationError\":false, \"status\": {\"compatible\":true, \"migration\":true}}]}}", true);
    }

    @Test
    public void jsonview_persistence_should_manage_all_fields_pages() throws Exception {
        Fragment fragmentInitial = createAFilledFragment();
        fragmentInitial.addVariable("aDAta", new Variable(DataType.CONSTANT, "aConstant"));
        //We serialize and deserialize our object
        byte[] data = jsonHandler.toJson(fragmentInitial, JsonViewPersistence.class);
        Fragment fragmentAfterJsonProcessing = jsonHandler.fromJson(data, Fragment.class);

        Assertions.assertThat(fragmentAfterJsonProcessing.getName()).isNotNull();
        Assertions.assertThat(fragmentAfterJsonProcessing.getId()).isNotNull();
        Assertions.assertThat(fragmentAfterJsonProcessing.getVariables()).isNotEmpty();
        Assertions.assertThat(fragmentAfterJsonProcessing.getRows()).isNotNull();

        //The element specific to lightView have to be null
        assertThat(fragmentAfterJsonProcessing.getUsedBy()).isNull();

    }

    /**
     * Create a filled fragment with a value for all fields
     */
    private Fragment createAFilledFragment() throws Exception {
        Fragment fragment = aFragment().withId("ID").withName("father").favorite().build();
        Fragment fragmentSon = aFragment().withId("ID").withName("name").build();
        Page page = PageBuilder.aFilledPage("ID");
        page.setUUID("UUID");
        page.setName("myPage");
        fragmentSon.addUsedBy("page", asList(page));
        fragmentSon.addUsedBy("fragment", asList(fragment));
        return fragmentSon;
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_empty() throws Exception {
        Fragment fragment = new Fragment();
        fragment.addUsedBy("component", new ArrayList<Identifiable>());

        assertThat(fragment.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components_when_list_is_null() throws Exception {
        Fragment fragment = new Fragment();
        fragment.addUsedBy("component", null);

        assertThat(fragment.getUsedBy()).isNull();
    }

    @Test
    public void should_not_add_useBy_components() throws Exception {
        Page page = aPage().build();
        Fragment fragment = new Fragment();
        fragment.addUsedBy("component", asList(page));

        assertThat(fragment.getUsedBy().get("component")).containsOnly(page);
    }
}
