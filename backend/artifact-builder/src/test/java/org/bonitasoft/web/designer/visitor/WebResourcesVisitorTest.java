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
package org.bonitasoft.web.designer.visitor;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.model.data.DataType.BUSINESSDATA;
import static org.bonitasoft.web.designer.model.data.DataType.URL;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.bonitasoft.web.designer.builder.VariableBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebResourcesVisitorTest {

    @Mock
    private FragmentRepository fragmentRepository;
    @Mock
    private WidgetRepository widgetRepository;

    @InjectMocks
    private WebResourcesVisitor webResourcesVisitor;

    private Component buttonGet;
    private Component btnSubmitTask;

    @Before
    public void setUp() throws Exception {
        when(widgetRepository.get(Mockito.any(String.class))).thenReturn(WidgetBuilder.aWidget().build());
        buttonGet = aComponent().withWidgetId("buttonGet")
                .withPropertyValue("action", "constant", "GET")
                .withPropertyValue("url", "constant", "../API/identity/user")
                .build();

        btnSubmitTask = aComponent().withWidgetId("btnSubmitTask")
                .withPropertyValue("action", "constant", WebResourcesVisitor.SUBMIT_TASK)
                .build();
    }

    @Test
    public void should_collect_webResources_when_visiting_a_component() throws Exception {
        Map<String, WebResource> webResourceMap = webResourcesVisitor.visit(buttonGet);

        assertThat(webResourceMap)
                .containsOnly(entry("GET|identity/user", new WebResource("GET", "identity/user", buttonGet.getId())));

        webResourceMap = webResourcesVisitor.visit(btnSubmitTask);

        assertThat(webResourceMap)
                .containsOnly(
                        entry("POST|bpm/userTask", new WebResource("POST", "bpm/userTask", btnSubmitTask.getId())));
    }

    @Test
    public void should_collect_webResources_from_a_container() throws Exception {
        Map<String, WebResource> webResourceMap = webResourcesVisitor.visit(aContainer()
                .with(buttonGet).build());

        assertThat(webResourceMap).hasSize(1);
    }

    @Test
    public void should_collect_webResource_from_a_tabs_container() throws Exception {
        Map<String, WebResource> webResourceMap = webResourcesVisitor.visit(aTabsContainer()
                .with(aTabContainer()
                        .with(aContainer()
                                .with(buttonGet).with(Arrays.asList(buttonGet, btnSubmitTask))))
                .build());

        assertThat(webResourceMap).hasSize(2);
    }

    @Test
    public void should_collect_component_from_a_formcontainer() throws Exception {
        Map<String, WebResource> webResourceMap = webResourcesVisitor.visit(aFormContainer().with(aContainer()
                .with(buttonGet).with(Arrays.asList(buttonGet, btnSubmitTask)))
                .build());

        assertThat(webResourceMap).hasSize(2);
    }

    @Test
    public void should_collect_component_from_a_previewable() throws Exception {
        // don't forget Variable
        var webResourceMap = webResourcesVisitor.visit(aPage()
                .with(btnSubmitTask)
                .withVariable("loadProcess", VariableBuilder.anURLVariable().value("../API/bpm/process"))
                .build());

        assertThat(webResourceMap).hasSize(2);
    }

    @Test
    public void should_collect_component_from_a_modal_container() throws Exception {
        Map<String, WebResource> webResourceMap = webResourcesVisitor.visit(aModalContainer().with(aContainer()
                .with(buttonGet).with(Arrays.asList(buttonGet, btnSubmitTask)))
                .build());

        assertThat(webResourceMap).hasSize(2);
    }

    @Test
    public void should_collect_component_from_a_fragment() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment()
                .with(buttonGet)
                .withVariable("loadProcess", VariableBuilder.anURLVariable().value("../API/bpm/process"))
                .build());

        var webResourceMap = webResourcesVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .build());

        assertThat(webResourceMap)
                                .containsEntry("GET|bpm/process", new WebResource("GET", "bpm/process", "Variable"))
                                .hasSize(2);
    }

    private Page setUpPageForResourcesTests(String id) {
        Page page = aPage().withId(id).build();
        return page;
    }

    private Variable anApiVariable(String value) {
        return new Variable(URL, value);
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_fileUpload_widget() throws Exception {
        Component fileUploadComponent = aComponent()
                .withWidgetId("pbUpload")
                .withPropertyValue("url", ParameterType.CONSTANT.getValue(), "../API/extension/upload")
                .build();

        var properties = webResourcesVisitor.visit(fileUploadComponent);

        assertThat(properties).containsKey("POST|extension/upload");
    }

    @Test
    public void should_add_bonita_resources_found_in_pages_widgets() throws Exception {
        Page page = setUpPageForResourcesTests("myPage");

        page.setVariables(singletonMap("foo", anApiVariable("../API/bpm/userTask?filter=mine")));

        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsKey("GET|bpm/userTask");
    }

    @Test
    public void should_add_bonita_resources_found_when_a_BusinessData_variable_is_declared() throws Exception {
        Page page = setUpPageForResourcesTests("myPage");

        page.setVariables(
                singletonMap("foo", new Variable(BUSINESSDATA, "{\"id\":\"com.company.model.DossierPret\"}")));

        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsOnlyKeys("GET|bdm/businessData", "GET|bdm/businessDataQuery");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_pages_widgets() throws Exception {
        Page page = setUpPageForResourcesTests("myPage");

        HashMap<String, Variable> variables = new HashMap<>();
        variables.put("foo", anApiVariable("../API/extension/CA31/SQLToObject?filter=mine"));
        // Not supported platform side. Prefer use queryParam like ?id=4
        variables.put("bar", anApiVariable("../API/extension/user/4"));
        variables.put("aa", anApiVariable("../API/extension/group/list"));
        variables.put("session", anApiVariable("../API/extension/user/group/unusedid"));
        variables.put("ab", anApiVariable(
                "http://localhost:8080/bonita/portal/API/extension/vehicule/voiture/roue?p=0&c=10&f=case_id={{caseId}}"));
        variables.put("user", anApiVariable("../API/identity/user/{{aaa}}/context"));
        variables.put("task", anApiVariable("../API/bpm/task/1/context"));
        variables.put("case", anApiVariable("../API/bpm/case{{dynamicQueries(true,0)}}"));
        variables.put("custom", anApiVariable("../API/extension/case{{dynamicQueries}}"));
        page.setVariables(variables);

        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsKey("GET|bpm/task")
                .containsKey("GET|identity/user")
                .containsKey("GET|extension/group/list")
                .containsKey("GET|extension/vehicule/voiture/roue")
                .containsKey("GET|extension/user/4")
                .containsKey("GET|extension/user/group/unusedid")
                .containsKey("GET|extension/CA31/SQLToObject")
                .containsKey("GET|extension/case");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_data_table_properties() throws Exception {
        Component dataTableComponent = aComponent().withWidgetId("dataTable")
                .withPropertyValue("apiUrl", "constant", "../API/extension/car")
                .build();

        var properties = webResourcesVisitor.visit(dataTableComponent);

        assertThat(properties).containsKey("GET|extension/car");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_button_with_DELETE_action() throws Exception {
        Component dataTableComponent = aComponent().withWidgetId("dataTable")
                .withPropertyValue("action", "constant", "DELETE")
                .withPropertyValue("url", ParameterType.INTERPOLATION.getValue(), "../API/bpm/document/1")
                .build();

        var properties = webResourcesVisitor.visit(dataTableComponent);
        assertThat(properties).containsKey("DELETE|bpm/document");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_button_with_POST_action() throws Exception {
        Component dataTableComponent = aComponent().withWidgetId("dataTable")
                .withPropertyValue("action", "constant", "POST")
                .withPropertyValue("url", ParameterType.INTERPOLATION.getValue(), "../API/extension/user")
                .build();

        var properties = webResourcesVisitor.visit(dataTableComponent);
        assertThat(properties).containsKey("POST|extension/user");
    }

    @Test
    public void should_add_start_process_resource_if_a_start_process_submit_is_contained_in_the_page()
            throws Exception {
        Component component = aComponent().withWidgetId("button")
                .withPropertyValue("action", "constant", "Start process")
                .build();

        var properties = webResourcesVisitor.visit(component);
        assertThat(properties).containsKey("POST|bpm/process");
    }

    @Test
    public void should_add_submit_task_resource_if_a_start_submit_task_is_contained_in_the_page() throws Exception {
        Component component = aComponent().withWidgetId("button")
                .withPropertyValue("action", "constant", "Submit task")
                .build();

        var properties = webResourcesVisitor.visit(component);

        assertThat(properties).containsKey("POST|bpm/userTask");
    }

    @Test
    public void should_combined_start_process_submit_task_and_bonita_resources() throws Exception {
        Page page = aPage().with(aContainer().with(Arrays.asList(
                aComponent().withWidgetId("button")
                        .withPropertyValue("action", "constant", "Start process").build(),
                aComponent().withWidgetId("button")
                        .withPropertyValue("action", "constant", "Submit task").build()))
                .build())
                .build();

        page.setVariables(singletonMap("foo", anApiVariable("/bonita/API/bpm/userTask")));

        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsKey("POST|bpm/userTask")
                .containsKey("GET|bpm/userTask")
                .containsKey("POST|bpm/process");
    }

    private Page setUpPageWithFragmentForResourcesTests() {
        Fragment fragment = aFragment().withId("myFragment").build();
        HashMap<String, Variable> variables = new HashMap<>();
        variables.put("fragAPI", anApiVariable("../API/bpm/process/1"));
        variables.put("fragAPIExt", anApiVariable("../API/extension/user/4"));
        fragment.setVariables(variables);

        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("myFragment");
        fragmentElement.setDimension(Map.of("md", 8));

        Page page = aPage().withId("myPage").with(fragmentElement).build();
        page.setVariables(singletonMap("foo", anApiVariable("../API/bpm/userTask?filter=mine")));

        TreeSet<String> fragmentIds = new TreeSet<String>();
        fragmentIds.add("myFragment");
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);

        return page;
    }

    @Test
    public void should_add_bonita_resources_found_in_fragments() throws Exception {
        Page page = setUpPageWithFragmentForResourcesTests();

        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsKey("GET|bpm/userTask")
                .containsKey("GET|bpm/process")
                .containsKey("GET|extension/user/4");
    }

    @Test
    public void should_add_bonita_resources_found_in_a_custom_widget() throws Exception {
        Widget widget = WidgetBuilder.aWidget().withId("timeLineWidget").custom()
                .withWebResources(new WebResource("POST", "customInfo/definition", "timeLineWidget")).build();
        when(widgetRepository.get("timeLineWidget")).thenReturn(widget);
        Page page = aPage().with(aContainer().with(Arrays.asList(
                aComponent().withWidgetId("timeLineWidget")
                        .build()))
                .build())
                .build();
        var properties = webResourcesVisitor.visit(page);

        assertThat(properties).containsKey("POST|customInfo/definition");
    }

}
