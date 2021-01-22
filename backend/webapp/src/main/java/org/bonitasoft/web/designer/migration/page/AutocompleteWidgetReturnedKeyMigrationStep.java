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

import java.util.Optional;

import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;

public class AutocompleteWidgetReturnedKeyMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<T> {

    private ComponentVisitor componentVisitor;

    public AutocompleteWidgetReturnedKeyMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) throws Exception {
        try {
            for (Component component : page.accept(componentVisitor)) {
                if ("pbAutocomplete".equals(component.getId())) {
                    PropertyValue displayedKeyValue = component.getPropertyValues().get("displayedKey");
                    if (displayedKeyValue != null) {
                        //put the same value as displayedKey in returnedKey
                        PropertyValue returnedKeyValue = new PropertyValue();
                        returnedKeyValue.setType(BondType.INTERPOLATION.toJson());
                        returnedKeyValue.setValue(displayedKeyValue.getValue());
                        component.getPropertyValues().put("returnedKey", returnedKeyValue);
                        //change the BondType of displayedKey to INTERPOLATION
                        PropertyValue newDisplayedKeyValue = new PropertyValue();
                        newDisplayedKeyValue.setType(BondType.INTERPOLATION.toJson());
                        newDisplayedKeyValue.setValue(displayedKeyValue.getValue());
                        component.getPropertyValues().put("displayedKey", newDisplayedKeyValue);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e) {
           throw e;
        }
    }
}
