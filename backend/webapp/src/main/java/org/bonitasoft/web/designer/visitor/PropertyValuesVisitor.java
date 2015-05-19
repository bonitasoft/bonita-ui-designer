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

import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.rendering.TemplateEngine;

/**
 * An element visitor which traverses the tree of elements recursively to collect property values in a page
 */
public class PropertyValuesVisitor implements ElementVisitor<Map<String, Map<String, PropertyValue>>>, PageFactory {

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Component component) {
        return singletonMap(component.getReference(), component.getPropertyValues());
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Container container) {
        Map<String, Map<String, PropertyValue>> propertyValues = getPropertyValuesFor(container.getRows());
        propertyValues.put(container.getReference(), container.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FormContainer formContainer) {
        Map<String, Map<String, PropertyValue>> propertyValues = formContainer.getContainer().accept(this);
        propertyValues.put(formContainer.getReference(), formContainer.getPropertyValues());
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(TabsContainer tabsContainer) {
        Map<String, Map<String, PropertyValue>> propertyValues = new HashMap<>();
        propertyValues.put(tabsContainer.getReference(), tabsContainer.getPropertyValues());
        for (Tab tab : tabsContainer.getTabs()) {
            if (tab.getContainer() != null) {
                propertyValues.putAll(tab.getContainer().accept(this));
            }
        }
        return propertyValues;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Previewable previewable) {
        return getPropertyValuesFor(previewable.getRows());
    }

    public String generate(Previewable previewable) {
        return new TemplateEngine("factory.hbs.js")
                .with("name", "propertyValues")
                .with("resources", this.visit(previewable))
                .build(this);
    }

    private Map<String, Map<String, PropertyValue>> getPropertyValuesFor(List<List<Element>> rows) {
        Map<String, Map<String, PropertyValue>> propertyValues = new HashMap<>();
        for (List<Element> row : rows) {
            for (Element element : row) {
                propertyValues.putAll(element.accept(this));
            }
        }
        return propertyValues;
    }
}
