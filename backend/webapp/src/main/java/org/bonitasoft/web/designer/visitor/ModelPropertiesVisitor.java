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

import static com.google.common.collect.Maps.filterKeys;
import static com.google.common.collect.Maps.transformEntries;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
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

/**
 * An element visitor which traverses the tree of elements recursively to collect all the data used in a page
 */
public class ModelPropertiesVisitor implements ElementVisitor<Map<String, Map<String, PropertyValue>>>, PageFactory {

    private FragmentRepository fragmentRepository;

    @Inject
    public ModelPropertiesVisitor(FragmentRepository fragmentRepository) {
        this.fragmentRepository = fragmentRepository;
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(Container container) {
        Map<String, Map<String, PropertyValue>> data = new HashMap<>();
        for (List<Element> rows : container.getRows()) {
            for (Element element : rows) {
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
        for (TabContainer tabContainer : tabsContainer.getTabList()) {
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
        Container container = new Container();
        container.setRows(previewable.getRows());
        return container.accept(this);
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {
        return new TemplateEngine("factory.hbs.js")
                .with("name", "modelProperties")
                .with("resources", this.visit(previewable))
                .build(this);
    }

    @Override
    public Map<String, Map<String, PropertyValue>> visit(FragmentElement fragmentElement) {
        try {
            Fragment fragment = fragmentRepository.get(fragmentElement.getId());
            return ImmutableMap.<String, Map<String, PropertyValue>>builder()
                    .putAll(singletonMap(fragmentElement.getReference(), getBindings(fragmentElement, fragment)))
                    .putAll(fragment.toContainer(fragmentElement).accept(this))
                    .build();
        } catch (RepositoryException | NotFoundException e) {
            throw new GenerationException("Error while generating model properties for fragment " + fragmentElement.getId(), e);
        }
    }

    private Map<String, PropertyValue> getBindings(FragmentElement fragmentElement, final Fragment fragment) {

        final Map<String, Variable> exposedData = fragment.getExposedVariables();
        Map<String, String> bindings = filterKeys(fragmentElement.getBinding(), new Predicate<String>() {

            @Override
            public boolean apply(String dataName) {
                return exposedData.containsKey(dataName);
            }
        });

        return transformEntries(bindings, new FragmentBindingValueTransformer());
    }
}
