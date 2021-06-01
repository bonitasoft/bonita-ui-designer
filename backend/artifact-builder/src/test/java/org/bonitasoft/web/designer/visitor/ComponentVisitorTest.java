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
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentVisitorTest {

    @Mock
    private FragmentRepository fragmentRepository;

    @InjectMocks
    private ComponentVisitor componentVisitor;

    private Component component;

    @Before
    public void setUp() throws Exception {
        component = aComponent().build();
    }

    @Test
    public void should_collect_component_when_visiting_a_component() throws Exception {

        Iterable<Component> components = componentVisitor.visit(component);

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_container() throws Exception {

        Iterable<Component> components = componentVisitor.visit(aContainer()
                .with(component)
                .build());

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_tabs_container() throws Exception {

        Iterable<Component> components = componentVisitor.visit(aTabsContainer()
                .with(aTabContainer()
                        .with(aContainer()
                                .with(component)))
                .build());

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_formcontainer() throws Exception {

        Iterable<Component> components = componentVisitor.visit(aFormContainer().with(aContainer()
                .with(component)
                .build()).build());

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_previewable() throws Exception {

        Iterable<Component> components = componentVisitor.visit(aPage()
                .with(component)
                .build());

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_modal_container() throws Exception {
        Iterable<Component> components = componentVisitor.visit(aModalContainer()
                .with(aContainer()
                        .with(component))
                .build());

        assertThat(components).containsExactly(component);
    }

    @Test
    public void should_collect_component_from_a_fragment() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment()
                .with(component).build());

        Iterable<Component> components = componentVisitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .build());

        assertThat(components).containsExactly(component);
    }
}
