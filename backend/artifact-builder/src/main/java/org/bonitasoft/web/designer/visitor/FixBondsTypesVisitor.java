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
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Property;

import java.util.Collection;
import java.util.List;

import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.EXPRESSION;

public class FixBondsTypesVisitor implements ElementVisitor<Void> {

    private final List<Property> properties;

    public FixBondsTypesVisitor(List<Property> properties) {
        this.properties = properties;
    }

    @Override
    public Void visit(Container container) {
        container.getRows().stream().flatMap(Collection::stream)
                .forEach(element -> element.accept(this));
        return null;
    }

    @Override
    public Void visit(FormContainer formContainer) {
        visit(formContainer.getContainer());
        return null;
    }

    @Override
    public Void visit(TabsContainer tabsContainer) {
        for (var tab : tabsContainer.getTabList()) {
            visit(tab);
        }
        return null;
    }

    @Override
    public Void visit(ModalContainer modalContainer) {
        visit(modalContainer.getContainer());
        return null;
    }

    @Override
    public Void visit(TabContainer tabContainer) {
        visit(tabContainer.getContainer());
        return null;
    }

    @Override
    public Void visit(Component component) {
        for (Property property : properties) {
            var bondType = property.getBond();
            var propertyValue = component.getPropertyValues().get(property.getName());
            if (propertyValue == null) {
                continue;
            }
            if (EXPRESSION.equals(bondType)) {
                bondType = CONSTANT;
            }
            propertyValue.setType(bondType.toJson());
        }
        return null;
    }

    @Override
    public Void visit(FragmentElement fragmentElement) {
        // nothing to do
        return null;
    }

    @Override
    public Void visit(Previewable previewable) {
        previewable.getRows().stream().flatMap(Collection::stream)
                .forEach(element -> element.accept(this));
        return null;
    }
}
