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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aPropertyValue;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModelPropertiesVisitorTest {

    @Mock
    FragmentRepository fragmentRepository;

    @InjectMocks
    ModelPropertiesVisitor modelPropertiesVisitor;

    FragmentElement person = aFragmentElement()
            .withFragmentId("person")
            .withBinding("foo", "bar")
            .withReference("person").build();

    FragmentElement address = aFragmentElement()
            .withFragmentId("address")
            .withBinding("baz", "qux")
            .withReference("address").build();

    @Test
    public void should_generate_a_model_properties_factory() throws Exception {
        mock(aFragment().withId("address").withVariable("baz", aConstantVariable().exposed(true)));
        mock(aFragment().withId("person").withVariable("foo", aConstantVariable().exposed(true)).with(aContainer().with(address)));
        Page page = aPage().with(aContainer().with(person)).build();

        String generated = modelPropertiesVisitor.generate(page);

        assertThat(generated).isEqualTo(
                new TemplateEngine("factory.hbs.js")
                        .with("name", "modelProperties")
                        .with("resources", of(
                                "person", createProperty("foo", "bar"),
                                "address", createProperty("baz", "qux")))
                        .build(this));

    }

    @Test
    public void should_retrieve_nested_fragments_model_properties_for_exposed_variable() throws Exception {
        mock(aFragment().withId("address").withVariable("baz", aConstantVariable().exposed(true)));
        mock(aFragment().withId("person").withVariable("foo", aConstantVariable().exposed(true)).with(aContainer().with(address)));
        Page page = aPage().with(aContainer().with(person)).build();

        Map<String, Map<String, PropertyValue>> bindings = modelPropertiesVisitor.visit(page);

        assertThat(bindings).contains(entry("person", createProperty("foo", "bar")));
        assertThat(bindings).contains(entry("address", createProperty("baz", "qux")));
    }

    @Test
    public void should_not_retrieve_bindings_for_not_exposed_variable() throws Exception {
        mock(aFragment().withId("address").withVariable("baz", aConstantVariable().exposed(false)));
        mock(aFragment().withId("person").withVariable("foo", aConstantVariable().exposed(false)).with(aContainer().with(address)));
        Page page = aPage().with(aContainer().with(person)).build();

        Map<String, Map<String, PropertyValue>> bindings = modelPropertiesVisitor.visit(page);

        assertThat(bindings).contains(entry("person", emptyMap()));
        assertThat(bindings).contains(entry("address", emptyMap()));

    }

    @Test
    public void should_retrieve_nested_fragments_model_properties_for_exposed_variable_with_a_modal_container() throws Exception {
        mock(aFragment().withId("address").withVariable("baz", aConstantVariable().exposed(true)));
        mock(aFragment().withId("person").withVariable("foo", aConstantVariable().exposed(true)).with(aModalContainer().with(aContainer().with(address)).build()));
        Page page = aPage().with(aModalContainer().with(aContainer().with(person))).build();

        Map<String, Map<String, PropertyValue>> bindings = modelPropertiesVisitor.visit(page);

        assertThat(bindings).contains(entry("person", createProperty("foo", "bar")));
        assertThat(bindings).contains(entry("address", createProperty("baz", "qux")));
    }

    private void mock(FragmentBuilder fragmentBuilder) {
        Fragment fragment = fragmentBuilder.build();
        when(fragmentRepository.get(fragment.getId())).thenReturn(fragment);
    }

    private ImmutableMap<String, PropertyValue> createProperty(String name, String value) {
        PropertyValue propertyValue = aPropertyValue().withType(ParameterType.VARIABLE).withValue(value).build();
        return ImmutableMap.of(name, propertyValue);
    }

    private Map<String, PropertyValue> emptyMap() {
        return Maps.newHashMap();
    }

}
