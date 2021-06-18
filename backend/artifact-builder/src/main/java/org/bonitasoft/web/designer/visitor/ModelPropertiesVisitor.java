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

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the data used in a page
 */
@RequiredArgsConstructor
public class ModelPropertiesVisitor implements ElementVisitor<Map<String, Map<String, PropertyValue>>>, PageFactory {

    private final FragmentRepository fragmentRepository;

    @Override
    public Map<String, Map<String, PropertyValue>> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Container container) {
        Map<String, Map<String, PropertyValue>> data = new HashMap<>();
        for (var rows : container.getRows()) {
            for (var element : rows) {
                data.putAll(element.accept(this));
            }
        }
        return data;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(TabsContainer tabsContainer) {
        Map<String, Map<String, PropertyValue>> data = new HashMap<>();
        for (var tabContainer : tabsContainer.getTabList()) {
            data.putAll(tabContainer.accept(this));
        }
        return data;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Component component) {
        return emptyMap();
    }

    @Override
    public <P extends Previewable & Identifiable> Map<String, Map<String, PropertyValue>> visit(P previewable) {
        var container = new Container();
        container.setRows(previewable.getRows());
        return container.accept(this);
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {

        var resources = this.visit(previewable);
        return new TemplateEngine("factory.hbs.js")
                .with("name", "modelProperties")
                .with("resources", resources == null ? resources : new TreeMap<>(resources))
                .build(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FragmentElement fragmentElement) {
        try {
            var fragment = fragmentRepository.get(fragmentElement.getId());
            var props = new HashMap<String, Map<String, PropertyValue>>();
            props.putAll(Map.of(fragmentElement.getReference(), getBindings(fragmentElement, fragment)));
            props.putAll(fragment.toContainer(fragmentElement).accept(this));
            return Map.copyOf(props);
        } catch (RepositoryException | NotFoundException e) {
            throw new GenerationException("Error while generating model properties for fragment " + fragmentElement.getId(), e);
        }
    }

    private Map<String, PropertyValue> getBindings(FragmentElement fragmentElement, final Fragment fragment) {
        var exposedData = fragment.getExposedVariables();

        return fragmentElement.getBinding().entrySet().stream()
                .filter(entry -> exposedData.containsKey(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, new FragmentBindingValueTransformer()));
    }
}
