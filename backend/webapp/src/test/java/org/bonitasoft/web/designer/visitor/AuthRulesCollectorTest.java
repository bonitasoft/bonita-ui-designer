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

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthRulesCollectorTest {

    @Mock
    private WidgetRepository widgetRepository;
    @InjectMocks
    private AuthRulesCollector authRulesCollector;

    private Component mockComponentFor(WidgetBuilder widgetBuilder) throws Exception {
        Widget widget = widgetBuilder.build();
        Component component = aComponent().withWidgetId(widget.getId()).build();
        when(widgetRepository.get(component.getId())).thenReturn(widget);
        return component;
    }

    @Test
    public void should_return_collect_nothing_when_page_does_not_contain_any_widget_with_auth_rules() throws Exception {
        Component component = mockComponentFor(aWidget());
        Page page = aPage().with(component).build();

        Set<String> modules = authRulesCollector.visit(page);

        assertThat(modules).isEmpty();
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets() throws Exception {
        Component component = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));

        Set<String> modules = authRulesCollector.visit(component);

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process");
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets_in_container() throws Exception {
        Component component1 = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));
        Component component2 = mockComponentFor(aWidget().authRules("GET|bpm/userTask"));

        Set<String> modules = authRulesCollector.visit(aContainer().with(component1, component2).build());

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process", "GET|bpm/userTask");
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets_in_formcontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));
        Component component2 = mockComponentFor(aWidget().authRules("GET|bpm/userTask"));

        Set<String> modules = authRulesCollector.visit(aFormContainer().with(aContainer().with(component1, component2)).build());

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process", "GET|bpm/userTask");
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets_in_tabscontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));
        Component component2 = mockComponentFor(aWidget().authRules("GET|bpm/userTask"));

        Set<String> modules = authRulesCollector.visit(aTabsContainer().with(
                aTabContainer().with(aContainer().with(component1)),
                aTabContainer().with(aContainer().with(component2))).build());

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process", "GET|bpm/userTask");
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets_in_previewable() throws Exception {
        Component component1 = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));
        Component component2 = mockComponentFor(aWidget().authRules("GET|bpm/userTask"));

        Set<String> modules = authRulesCollector.visit(aPage().with(component1, component2).build());

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process", "GET|bpm/userTask");
    }

    @Test
    public void should_collect_auth_rules_needed_by_widgets_in_modal_container() throws Exception {
        Component component1 = mockComponentFor(aWidget().authRules("GET|living/application-menu", "POST|bpm/process"));
        Component component2 = mockComponentFor(aWidget().authRules("GET|bpm/userTask"));
        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());

        Set<String> modules = authRulesCollector.visit(aModalContainer().with(aContainer().with(component1, component2)).build());

        assertThat(modules).containsOnly("GET|living/application-menu", "POST|bpm/process", "GET|bpm/userTask");
    }
}
