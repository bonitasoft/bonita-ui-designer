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
package org.bonitasoft.web.designer.migration.page;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import java.util.Collection;

public class MigrationTabsContainerVisitor implements ElementVisitor<Void> {

    @Override
    public Void visit(Container container) {
        container.getRows().stream().flatMap(Collection::stream)
                .forEach(element -> element.accept(this));
        return null;
    }

    @Override
    public Void visit(FormContainer formContainer) {
        return visit(formContainer.getContainer());
    }

    @Override
    public Void visit(TabsContainer tabsContainer) {
        createPropertyValue(tabsContainer, "vertical", BondType.CONSTANT, Boolean.FALSE);
        createPropertyValue(tabsContainer, "type", BondType.CONSTANT, "tabs");
        for (TabContainer tab : tabsContainer.getTabList()) {
            visit(tab);
        }
        return null;
    }

    @Override
    public Void visit(TabContainer tabContainer) {
        createPropertyValue(tabContainer, "cssClasses", BondType.CONSTANT, "");
        createPropertyValue(tabContainer, "hidden", BondType.CONSTANT, false);
        createPropertyValue(tabContainer, "disabled", BondType.CONSTANT, false);
        visit(tabContainer.getContainer());
        return null;
    }

    @Override
    public Void visit(ModalContainer modalContainer) {
        visit(modalContainer.getContainer());
        return null;
    }

    @Override
    public Void visit(Component component) {
        return null;
    }

    @Override
    public Void visit(FragmentElement fragmentElement) {
        return null;
    }

    @Override
    public <P extends Previewable & Identifiable> Void visit(P previewable) {
        previewable.getRows().stream().flatMap(Collection::stream)
                .forEach(element -> element.accept(this));
        return null;
    }

    private void createPropertyValue(Component component, String propertyName, BondType bondType, Object defaultValue) {
        if (!component.getPropertyValues().containsKey(propertyName)) {
            var newPropertyValue = new PropertyValue();
            newPropertyValue.setType(bondType.toJson());
            newPropertyValue.setValue(defaultValue);
            component.getPropertyValues().put(propertyName, newPropertyValue);
        }
    }
}

