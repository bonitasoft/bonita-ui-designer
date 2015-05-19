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

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.rendering.TemplateEngine;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the data used in a page
 */
public class DataModelVisitor implements ElementVisitor<Map<String, Map<String, Data>>>, PageFactory {

    @Override
    public Map<String, Map<String, Data>> visit(Container container) {
        Map<String, Map<String, Data>> data = new HashMap<>();
        for (List<Element> rows : container.getRows()) {
            for (Element element : rows) {
                data.putAll(element.accept(this));
            }
        }
        return data;
    }

    @Override
    public Map<String, Map<String, Data>> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, Data>> visit(TabsContainer tabsContainer) {
        Map<String, Map<String, Data>> data = new HashMap<>();
        for (Tab tab : tabsContainer.getTabs()) {
            data.putAll(tab.getContainer().accept(this));
        }
        return data;
    }

    @Override
    public Map<String, Map<String, Data>> visit(Component component) {
        return emptyMap();
    }

    @Override
    public <P extends Previewable & Identifiable> Map<String, Map<String, Data>> visit(P previewable) {
        Map<String, Map<String, Data>> data = new HashMap<>();
        data.put(previewable.getId(), previewable.getData());
        Container container = new Container();
        container.setRows(previewable.getRows());
        data.putAll(container.accept(this));
        return data;
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {
        return new TemplateEngine("factory.hbs.js")
                .with("name", "dataModel")
                .with("resources", this.visit(previewable))
                .build(this);
    }
}
