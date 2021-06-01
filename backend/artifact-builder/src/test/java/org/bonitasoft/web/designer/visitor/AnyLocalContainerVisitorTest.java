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
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;

@RunWith(MockitoJUnitRunner.class)
public class AnyLocalContainerVisitorTest {

    @InjectMocks
    private AnyLocalContainerVisitor anyLocalContainerVisitor;

    private Component component;

    @Before
    public void setUp() throws Exception {
        component = aComponent().build();
    }

    @Test
    public void should_not_collect_components() throws Exception {
        assertThat(anyLocalContainerVisitor.visit(component)).isEmpty();
    }

    @Test
    public void should_collect_containers() throws Exception {
        Container container = aContainer()
                .with(component)
                .build();

        assertThat(anyLocalContainerVisitor.visit(container)).containsExactly(container);
    }

    @Test
    public void should_collect_tabs_containers_and_content() throws Exception {
        Container container = aContainer().build();
        TabContainer tab = aTabContainer()
                .with(container).build();
        TabsContainer tabsContainer = aTabsContainer()
                .with(tab)
                .build();

        assertThat(anyLocalContainerVisitor.visit(tabsContainer)).containsExactly(tabsContainer, tab, container);
    }

    @Test
    public void should_collect_form_containers_and_content() throws Exception {
        Container container = aContainer().build();
        FormContainer formContainer = aFormContainer().with(container).build();

        assertThat(anyLocalContainerVisitor.visit(formContainer)).containsExactly(formContainer, container);
    }

    @Test
    public void should_collect_container_within_previewable() throws Exception {
        Container container = aContainer().build();

        assertThat(anyLocalContainerVisitor.visit(aPage()
                .with(container)
                .build())).containsExactly(container);
    }

    @Test
    public void should_collect_modalContainer() throws Exception {
        Container container = aContainer().with(component).build();
        ModalContainer modalContainer = aModalContainer()
                .with(container)
                .build();

        assertThat(anyLocalContainerVisitor.visit(modalContainer)).contains(modalContainer, container);
    }
}
