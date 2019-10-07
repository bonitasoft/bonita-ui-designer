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

import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;

public class TableWidgetStylesMigrationStep<T extends AbstractPage> implements MigrationStep<T> {

    private static final String STRIPED_PROPERTY = "striped";
    private static final String CONDENSED_PROPERTY = "condensed";
    private static final String BORDERED_PROPERTY = "bordered";

    private ComponentVisitor componentVisitor;

    public TableWidgetStylesMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public void migrate(AbstractPage page) {
        for (Component component : page.accept(componentVisitor)) {
            if (isProvidedTableWidget(component.getId())) {
                setPropertyDefaultValue(component, STRIPED_PROPERTY, true);
                setPropertyDefaultValue(component, CONDENSED_PROPERTY, false);
                setPropertyDefaultValue(component, BORDERED_PROPERTY, false);
            }
        }
    }

    private void setPropertyDefaultValue(Component component, String property, boolean defaultValue) {
        if (!component.getPropertyValues().containsKey(property)) {
            PropertyValue propertyValue = new PropertyValue();
            propertyValue.setType(BondType.CONSTANT.toJson());
            propertyValue.setValue(defaultValue);
            component.getPropertyValues().put(property, propertyValue);
        }
    }

    private boolean isProvidedTableWidget(String componentId) {
        return "pbTable".equals(componentId) || "pbDataTable".equals(componentId);
    }
}
