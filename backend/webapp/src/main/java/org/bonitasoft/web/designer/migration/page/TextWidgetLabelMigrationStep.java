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

public class TextWidgetLabelMigrationStep<T extends AbstractPage> implements MigrationStep<T> {

    private ComponentVisitor componentVisitor;

    public TextWidgetLabelMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public void migrate(AbstractPage page) {
        for (Component component : page.accept(componentVisitor)) {
            if ("pbText".equals(component.getId())) {
                updatePropertyValue(component, "labelHidden", BondType.CONSTANT, Boolean.TRUE);
                updatePropertyValue(component, "label", BondType.INTERPOLATION, "");
                updatePropertyValue(component, "labelPosition", BondType.CONSTANT, "top");
                updatePropertyValue(component, "labelWidth", BondType.CONSTANT, 4);
            }
        }
    }

    private void updatePropertyValue(Component component, String propertyName, BondType bondType, Object defaultValue) {
        if (!component.getPropertyValues().containsKey(propertyName)) {
            PropertyValue labelHiddenValue = new PropertyValue();
            labelHiddenValue.setType(bondType.toJson());
            labelHiddenValue.setValue(defaultValue);
            component.getPropertyValues().put(propertyName, labelHiddenValue);
        }
    }
}
