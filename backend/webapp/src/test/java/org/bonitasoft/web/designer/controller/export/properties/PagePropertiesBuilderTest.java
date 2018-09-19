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
package org.bonitasoft.web.designer.controller.export.properties;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.data.DataType.URL;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.bonitasoft.web.designer.controller.PageResource;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PagePropertiesBuilderTest {

    @InjectMocks
    private PagePropertiesBuilder pagePropertiesBuilder;

    private Page page;

    @Mock
    private PageResource pageResource;


    @Before
    public void setUp() throws Exception {
        page = new Page();
        page.setName("myPage");
    }

    private Data anApiData(String value) {
        return new Data(URL, value);
    }

    @Test
    public void should_build_a_well_formed_page_property_file() throws Exception {
        page.setId("aPageId");
        page.setName("aPageName");
        page.setDescription("a page description with special characters &'\"é");
        page.setDisplayName("a display name with special characters &'\"é");

        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList(""));
        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("contentType=page");
        assertThat(properties).contains("name=custompage_aPageName");
        assertThat(properties).contains("displayName=a display name with special characters &'\"\\u00E9" );
        assertThat(properties).contains("description=a page description with special characters &'\"\\u00E9");
        assertThat(properties).contains("resources=[]");
    }

    @Test
    public void should_build_a_page_property_file_when_description_and_displayName_are_not_updated() throws Exception {
        page.setId("aPageId");
        page.setName("aPageName");

        String properties = new String(pagePropertiesBuilder.build(page));
        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList(""));
        assertThat(properties).contains("contentType=page");
        assertThat(properties).contains("name=custompage_aPageName");
        assertThat(properties).contains("displayName=aPageName" );
        assertThat(properties).contains("description=Page generated with Bonita UI designer");
        assertThat(properties).contains("resources=[]");
    }

    @Test
    public void should_add_bonita_resource_found_in_page_data() throws Exception {
        page.setData(singletonMap("foo", anApiData("/bonita/API/living/application-menu")));
        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList("GET|living/application-menu"));
        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[GET|living/application-menu]");
    }

    @Test
    public void should_add_relative_bonita_resource_found_in_page_data() throws Exception {
        Map<String, Data> dataMap = new TreeMap<String, Data>();
        dataMap.put("foo", anApiData("../API/bpm/userTask?filter=mine"));
        dataMap.put("bar", anApiData("../API/identity/user/1"));
        dataMap.put("other", anApiData("../API/identity/group/1?param=value"));
        dataMap.put("archived", anApiData("../API/bpm/archivedUserTask?filter=mine&o=name DESC"));

        page.setData(dataMap);
        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList("GET|bpm/archivedUserTask", "GET|identity/user", "GET|bpm/userTask", "GET|identity/group"));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[GET|bpm/archivedUserTask, GET|identity/user, GET|bpm/userTask, GET|identity/group]");
    }



    @Test
    public void should_not_add_a_resource_which_is_not_a_bonita_resource() throws Exception {
        page.setData(singletonMap("foo", anApiData("../API/path/to/wathever/resource")));
        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList(""));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[]");
    }



    @Test
    public void should_add_contentType() throws Exception {
        page.setType("layout");
        when(pageResource.getResources(page.getId())).thenReturn(Arrays.asList(""));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("contentType=layout");
    }
}
