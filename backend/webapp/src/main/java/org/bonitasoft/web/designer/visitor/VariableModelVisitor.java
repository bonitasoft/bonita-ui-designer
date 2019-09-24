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
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.model.page.TabContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the variables used in a page
 */
public class VariableModelVisitor implements ElementVisitor<Map<String, Map<String, Variable>>>, PageFactory {

    @Override
    public Map<String, Map<String, Variable>> visit(Container container) {
        Map<String, Map<String, Variable>> variables = new HashMap<>();
        for (List<Element> rows : container.getRows()) {
            for (Element element : rows) {
                variables.putAll(element.accept(this));
            }
        }
        return variables;
    }

    @Override
    public Map<String, Map<String, Variable>> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, Variable>> visit(TabsContainer tabsContainer) {
        Map<String, Map<String, Variable>> variables = new HashMap<>();
        for (TabContainer tabContainer : tabsContainer.getTabList()) {
            variables.putAll(tabContainer.accept(this));
        }
        return variables;
    }

    @Override
    public Map<String, Map<String, Variable>> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, Variable>> visit(Component component) {
        return emptyMap();
    }

    @Override
    public <P extends Previewable & Identifiable> Map<String, Map<String, Variable>> visit(P previewable) {
        Map<String, Map<String, Variable>> variables = new HashMap<>();
        variables.put(previewable.getId(), previewable.getVariables());
        Container container = new Container();
        container.setRows(previewable.getRows());
        variables.putAll(container.accept(this));
        return variables;
    }

    @Override
    public Map<String, Map<String, Variable>> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {
        return new TemplateEngine("factory.hbs.js")
                .with("name", "variableModel")
                .with("resources", this.visit(previewable))
                .build(this);
    }
}
