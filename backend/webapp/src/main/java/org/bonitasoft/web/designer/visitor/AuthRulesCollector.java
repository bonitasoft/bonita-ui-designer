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

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

public class AuthRulesCollector implements ElementVisitor<Set<String>> {

    private WidgetRepository widgetRepository;

    public AuthRulesCollector(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    @Override
    public Set<String> visit(Container container) {
        return visitRows(container.getRows());
    }

    @Override
    public Set<String> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(TabsContainer tabsContainer) {
        Set<String> authRules = new HashSet<>();
        for (TabContainer tabContainer : tabsContainer.getTabList()) {
            authRules.addAll(tabContainer.accept(this));
        }
        return authRules;
    }

    @Override
    public Set<String> visit(TabContainer tabContainer) {
        Set<String> authRules = new HashSet<>();
        authRules.addAll(tabContainer.getContainer().accept(this));
        return authRules;
    }

    @Override
    public Set<String> visit(Component component) {
        Widget widget = widgetRepository.get(component.getId());
        Set<String> authRules = widget.getAuthRules();
        if (authRules == null) {
            return emptySet();
        }
        return authRules;
    }

    @Override
    public Set<String> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public <P extends Previewable & Identifiable> Set<String> visit(P previewable) {
        return visitRows(previewable.getRows());
    }

    protected <P extends Previewable & Identifiable> Set<String> visitRows(List<List<Element>> rows) {
        Set<String> modules = new HashSet<>();
        for (List<Element> elements : rows) {
            for (Element element : elements) {
                modules.addAll(element.accept(this));
            }
        }
        return modules;
    }

}
