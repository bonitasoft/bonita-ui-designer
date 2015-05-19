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
package org.bonitasoft.web.designer.visitors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabBuilder.aTab;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;

import java.util.HashMap;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.bonitasoft.web.designer.visitor.PropertyValuesVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValuesVisitorTest {

    @Rule
    public TestResource testResource;

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
                .with(aTab().with(aContainer()
                        .with(aComponent().withReference("component-id").withPropertyValue("foo", "bar", "baz"))
                        .withReference("container-id")))
                .withReference("tabs-container-id")
                .build())).containsOnly(
                entry("tabs-container-id", emptyMap()),
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
        HashMap<String, PropertyValue> propertyValues = new HashMap<>();
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setType("bar");
        propertyValue.setValue("baz");
        propertyValues.put("foo", propertyValue);

        Component component = aComponent().withPropertyValue("foo", "bar", "baz").build();
        Page page = aPage().with(component).build();

        assertThat(propertyValuesVisitor.generate(page))
                .isEqualTo(new TemplateEngine("factory.hbs.js")
                        .with("name", "propertyValues")
                        .with("resources", singletonMap(component.getReference(), propertyValues)).build(this));
    }
}
