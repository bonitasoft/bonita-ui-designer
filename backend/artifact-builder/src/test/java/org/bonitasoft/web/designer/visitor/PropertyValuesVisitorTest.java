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

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValuesVisitorTest {

    @Rule
    public TestResource testResource = new TestResource(this.getClass());

    @Mock
    private FragmentRepository fragmentRepository;

    @InjectMocks
    private PropertyValuesVisitor propertyValuesVisitor;

    private PropertyValue propertyValue;

    @Before
    public void setUp() throws Exception {
        propertyValue = new PropertyValue();
        propertyValue.setType("bar");
        propertyValue.setValue("baz");
    }

    @Test
    public void should_associate_component_paramFeter_values_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aComponent()
                .withReference("component-id")
                .withPropertyValue("foo", "bar", "baz")
                .build())).containsExactly(entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_container_property_values_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aContainer()
                .withReference("container-id")
                .withPropertyValue("foo", "bar", "baz")
                .build())).containsExactly(entry("container-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_component_property_values_contained_in_a_container_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aContainer()
                .with(aComponent().withReference("component-id").withPropertyValue("foo", "bar", "baz"))
                .withReference("container-id")
                .build())).containsOnly(
                entry("container-id", emptyMap()),
                entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_component_parameter_values_contained_in_a_formcontainer_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(
                aFormContainer()
                        .with(aContainer()
                                        .with(aComponent().withReference("component-id").withPropertyValue("foo", "bar", "baz"))
                                        .withReference("container-id")
                                        .build()
                        )
                        .withReference("formcontainer-id")
                        .build()))
                .containsOnly(
                        entry("formcontainer-id", emptyMap()),
                        entry("container-id", emptyMap()),
                        entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_formcontainer_parameter_values_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aFormContainer()
                .with(aContainer().withReference("container-id").build())
                .withReference("formcontainer-id")
                .withPropertyValue("foo", "bar", "baz")
                .build())).containsOnly(
                entry("formcontainer-id", singletonMap("foo", propertyValue)),
                entry("container-id", emptyMap()));
    }

    @Test
    public void should_associate_tabs_container_property_values_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aTabsContainer()
                .withReference("tabs-container-id")
                .withPropertyValue("foo", "bar", "baz")
                .build())).containsExactly(entry("tabs-container-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_component_property_values_contained_in_a_tab_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aTabsContainer()
                .with(aTabContainer().with(aContainer()
                        .with(aComponent().withReference("component-id").withPropertyValue("foo", "bar", "baz"))
                        .withReference("container-id")).withReference("tab-id").build())
                .withReference("tabs-container-id")
                .build())).containsOnly(
                entry("tabs-container-id", emptyMap()),
                entry("tab-id", emptyMap()),
                entry("container-id", emptyMap()),
                entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_component_property_values_contained_in_a_previewable_with_its_id() throws Exception {

        assertThat(propertyValuesVisitor.visit(aPage()
                .with(aComponent()
                        .withReference("component-id")
                        .withPropertyValue("foo", "bar", "baz"))
                .build())).containsExactly(
                entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_generate_a_service_containing_parameter_values() throws Exception {
        Component component = aComponent().withPropertyValue("foo", "bar", "baz").withReference("component-ref").build();
        Page page = aPage().with(component).build();

        String service = propertyValuesVisitor.generate(page);

        assertThat(service).isEqualTo(testResource.load("property-value-visitor.result.js"));
    }

    @Test
    public void should_associate_component_property_values_contained_in_a_modal_container_with_its_id() throws Exception {
        assertThat(propertyValuesVisitor.visit(aModalContainer()
                .with(aContainer().with(aComponent().withReference("component-id").withPropertyValue("foo", "bar", "baz"))
                        .withReference("container-id").build())
                .build())).containsOnly(
                entry("modalContainer-reference", emptyMap()),
                entry("container-id", emptyMap()),
                entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_fragment_property_values_with_its_id() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment().build());

        assertThat(propertyValuesVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .withReference("fragment-element-id")
                .withPropertyValue("foo", "bar", "baz")
                .build())).containsExactly(
                entry("fragment-element-id", singletonMap("foo", propertyValue)));
    }

    @Test
    public void should_associate_component_property_values_contained_in_a_fragment_with_its_id() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment()
                .with(aRow().with(aComponent()
                        .withReference("component-id")
                        .withPropertyValue("foo", "bar", "baz")))
                .build());

        assertThat(propertyValuesVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .withReference("fragment-element-id")
                .build())).containsOnly(
                entry("fragment-element-id", emptyMap()),
                entry("component-id", singletonMap("foo", propertyValue)));
    }

    @Test(expected = GenerationException.class)
    public void should_throw_a_generation_error_when_fragment_is_not_found() {
        when(fragmentRepository.get("fragment-id")).thenThrow(new NotFoundException(""));

        propertyValuesVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id").build());
    }

    @Test(expected = GenerationException.class)
    public void should_throw_a_generation_error_when_there_is_a_repository_error_while_looking_for_a_fragment() {
        when(fragmentRepository.get("fragment-id")).thenThrow(new RepositoryException("", new RuntimeException()));

        propertyValuesVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id").build());
    }
}
