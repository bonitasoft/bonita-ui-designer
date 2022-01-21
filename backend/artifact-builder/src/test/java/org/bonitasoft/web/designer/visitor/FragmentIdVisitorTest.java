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

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Colin Puy
 */
@RunWith(MockitoJUnitRunner.class)
public class FragmentIdVisitorTest {

    @Mock
    private FragmentRepository fragmentRepository;

    @InjectMocks
    private FragmentIdVisitor fragmentIdVisitor;

    @Before
    public void setUp() {
        when(fragmentRepository.get(anyString())).thenReturn(aFragment().build());  // return empty fragment
    }

    @Test
    public void should_add_fragment_id_to_visited_fragment_ids_when_visiting_fragment() throws Exception {
        FragmentElement fragmentElement = aFragmentElement().withFragmentId("aFragmentId").build();

        assertThat(fragmentIdVisitor.visit(fragmentElement)).containsOnly("aFragmentId");
    }

    @Test
    public void should_visit_each_container_rows() throws Exception {
        Container container = aContainer().with(
                aRow().with(aFragmentElement().withFragmentId("fragment1")),
                aRow().with(aFragmentElement().withFragmentId("fragment2"))).build();

        assertThat(fragmentIdVisitor.visit(container)).containsOnly("fragment1", "fragment2");
    }

    @Test
    public void should_visit_formcontainer() throws Exception {
        Container container = aContainer().with(
                aRow().with(aFragmentElement().withFragmentId("fragment1")),
                aRow().with(aFragmentElement().withFragmentId("fragment2"))).build();

        assertThat(fragmentIdVisitor.visit(aFormContainer().with(container).build())).containsOnly("fragment1", "fragment2");
    }

    @Test
    public void should_visit_each_tabsContainer_containers() throws Exception {
        TabsContainer tabsContainer = aTabsContainer().with(
                aTabContainer().with(aContainer().with(aFragmentElement().withFragmentId("fragment3"))),
                aTabContainer().with(aContainer().with(aFragmentElement().withFragmentId("fragment4")))).build();

        assertThat(fragmentIdVisitor.visit(tabsContainer)).containsOnly("fragment3", "fragment4");
    }

    @Test
    public void should_visit_each_modal_container_rows() throws Exception {
        ModalContainer modalContainer = aModalContainer().with(aContainer().with(
                aRow().with(aFragmentElement().withFragmentId("fragment5")),
                aRow().with(aFragmentElement().withFragmentId("fragment6")))).build();

        assertThat(fragmentIdVisitor.visit(modalContainer)).containsOnly("fragment5", "fragment6");
    }

    @Test
    public void should_visit_fragment_container_when_visiting_a_fragment_element() throws Exception {
        FragmentElement fragmentElement = aFragmentElement().withFragmentId("aFragmentId").build();

        Fragment fragment = aFragment().with(aRow().with(aFragmentElement().withFragmentId("anotherFragmentId"))).build();
        when(fragmentRepository.get(fragmentElement.getId())).thenReturn(fragment);

        assertThat(fragmentIdVisitor.visit(fragmentElement)).containsOnly("aFragmentId", "anotherFragmentId");
    }

    @Test
    public void should_do_noting_when_visiting_a_component() throws Exception {
        assertThat(fragmentIdVisitor.visit(new Component())).isEmpty();
    }
}
