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

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.model.data.DataType.URL;
import static org.mockito.Mockito.when;

import java.util.*;

import com.google.common.collect.ImmutableSet;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.visitor.AuthRulesCollector;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PagePropertiesBuilderTest {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Mock
    private ComponentVisitor componentVisitor;

    @Mock
    private AuthRulesCollector authRulesCollector;

    @InjectMocks
    private PagePropertiesBuilder pagePropertiesBuilder;

    private Page page;

    private Component startProcessComponent;

    private Component submitTaskComponent;

    @Before
    public void setUp() throws Exception {
        page = new Page();
        when(componentVisitor.visit(page)).thenReturn(Collections.<Component>emptyList());
        startProcessComponent = aComponent()
                .withPropertyValue("foo", "constant", "Start process")
                .build();
        submitTaskComponent = aComponent()
                .withPropertyValue("foo", "constant", "Submit task")
                .build();
    }

    private Data anApiData(String value) {
        return new Data(URL, value);
    }

    @Test
    public void should_build_a_well_formed_page_property_file() throws Exception {
        page.setId("aPageId");
        page.setName("aPageName");

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("name=custompage_aPageName" + LINE_SEPARATOR);
        assertThat(properties).contains("displayName=aPageName page" + LINE_SEPARATOR);
        assertThat(properties).contains("description=aPageName page generated with Bonita UI designer");
        assertThat(properties).contains("resources=[]");
    }

    @Test
    public void should_add_bonita_resource_found_in_page_data() throws Exception {
        page.setData(singletonMap("foo", anApiData("/bonita/API/living/application-menu")));

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

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[GET|bpm/archivedUserTask, GET|identity/user, GET|bpm/userTask, GET|identity/group]");
    }

    @Test
    public void should_add_bonita_resources_found_in_pages_widgets() throws Exception {
        Set<String> authRules = new TreeSet<>();
        authRules.add("GET|living/application-menu");
        authRules.add("POST|bpm/process");
        page.setData(singletonMap("foo", anApiData("../API/bpm/userTask?filter=mine")));
        when(authRulesCollector.visit(page)).thenReturn(authRules);

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[GET|bpm/userTask, GET|living/application-menu, POST|bpm/process]");
    }

    @Test
    public void should_not_add_a_resource_which_is_not_a_bonita_resource() throws Exception {
        page.setData(singletonMap("foo", anApiData("../API/path/to/wathever/resource")));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[]");
    }

    @Test
    public void should_add_start_process_resource_if_a_start_process_submit_is_contained_in_the_page() throws Exception {
        when(componentVisitor.visit(page)).thenReturn(singleton(startProcessComponent));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[POST|bpm/process]");
    }

    @Test
    public void should_add_submit_task_resource_if_a_start_submit_task_is_contained_in_the_page() throws Exception {
        when(componentVisitor.visit(page)).thenReturn(singleton(submitTaskComponent));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[POST|bpm/userTask]");
    }

    @Test
    public void should_combined_start_process_submit_task_and_bonita_resources() throws Exception {
        when(componentVisitor.visit(page))
                .thenReturn(asList(startProcessComponent, submitTaskComponent));
        page.setData(singletonMap("foo", anApiData("/bonita/API/bpm/userTask")));

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("resources=[GET|bpm/userTask, POST|bpm/process, POST|bpm/userTask]");
    }

    @Test
    public void should_add_contentType() throws Exception {
        page.setType("layout");

        String properties = new String(pagePropertiesBuilder.build(page));

        assertThat(properties).contains("contentType=layout");
    }
}
