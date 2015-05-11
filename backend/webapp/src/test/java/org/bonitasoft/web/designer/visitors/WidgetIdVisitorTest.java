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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetIdVisitorTest {

    @InjectMocks
    private WidgetIdVisitor visitor;

    @Test
    public void should_add_widgetId_of_component() {
        Component component = createComponentWithWidget("foo");
        assertThat(visitor.visit(component)).containsExactly("foo");
    }

    @Test
    public void should_traverse_container() {
        Container container = new Container();
        List<Element> row1 = Arrays.<Element>asList(createComponentWithWidget("foo"));
        List<Element> row2 = Arrays.<Element>asList(createComponentWithWidget("bar"));
        container.setRows(Arrays.asList(row1, row2));

        assertThat(visitor.visit(container)).containsOnly("foo", "bar");
    }

    @Test
    public void should_traverse_formcontainer() {
        FormContainer formContainer = new FormContainer();
        Container container = new Container();
        List<Element> row1 = Arrays.<Element>asList(createComponentWithWidget("foo"));
        List<Element> row2 = Arrays.<Element>asList(createComponentWithWidget("bar"));
        container.setRows(Arrays.asList(row1, row2));
        formContainer.setContainer(container);

        assertThat(visitor.visit(formContainer)).containsOnly("foo", "bar");
    }

    @Test
    public void should_traverse_tabs_container() {
        TabsContainer tabsContainer = new TabsContainer();
        Container container1 = new Container();
        List<Element> row1 = Arrays.<Element>asList(createComponentWithWidget("foo"));
        container1.setRows(Arrays.asList(row1));
        Tab tab1 = new Tab();
        tab1.setContainer(container1);

        tabsContainer.setTabs(Arrays.asList(tab1));

        assertThat(visitor.visit(tabsContainer)).containsExactly("foo");
    }

    private Component createComponentWithWidget(String widgetId) {
        Component component = new Component();
        component.setId(widgetId);
        return component;
    }
}
