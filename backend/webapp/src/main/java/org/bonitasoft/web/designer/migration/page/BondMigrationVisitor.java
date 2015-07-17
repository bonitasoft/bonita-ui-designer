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

import static com.google.common.collect.Iterables.concat;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.ImmutableMap;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

@Named
public class BondMigrationVisitor implements ElementVisitor<Void> {

    private final WidgetRepository widgetRepository;

    private Map<BondType, BondMigrationStrategy> migrationStrategies = ImmutableMap.<BondType, BondMigrationStrategy>builder()
            .put(BondType.CONSTANT, new ConstantBondMigrationStrategy())
            .put(BondType.INTERPOLATION, new InterpolationBondMigrationStrategy())
            .put(BondType.EXPRESSION, new ExpressionBondMigrationStrategy())
            .put(BondType.VARIABLE, new VariableBondMigrationStrategy())
            .build();

    @Inject
    public BondMigrationVisitor(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    public void migrate(Element element, Widget widget) {
        Map<String, PropertyValue> propertyValues = element.getPropertyValues();
        for (Map.Entry<String, PropertyValue> entry : propertyValues.entrySet()) {
            Property property = widget.getProperty(entry.getKey());
            BondType bondType = property != null ? property.getBond() : BondType.EXPRESSION;
            migrationStrategies.get(bondType).migrate(property, entry.getValue());
        }
    }

    @Override
    public Void visit(Container container) {
        migrate(container, widgetRepository.get("pbContainer"));
        for (Element element : concat(container.getRows())) {
            element.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FormContainer formContainer) {
        migrate(formContainer, widgetRepository.get("pbFormContainer"));
        formContainer.getContainer().accept(this);
        return null;
    }

    @Override
    public Void visit(TabsContainer tabsContainer) {
        migrate(tabsContainer, widgetRepository.get("pbTabsContainer"));
        for (Tab tab : tabsContainer.getTabs()) {
            tab.getContainer().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Component component) {
        migrate(component, widgetRepository.get(component.getId()));
        return null;
    }

    @Override
    public <P extends Previewable & Identifiable> Void visit(P previewable) {
        for (Element element : concat(previewable.getRows())) {
            element.accept(this);
        }
        return null;
    }
}
