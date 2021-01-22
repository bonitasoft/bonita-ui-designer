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

public class TextWidgetInterpretHTMLMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<T> {

    private ComponentVisitor componentVisitor;

    public TextWidgetInterpretHTMLMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) {
        for (Component component : page.accept(componentVisitor)) {
            if ("pbText".equals(component.getId()) && !component.getPropertyValues().containsKey("allowHTML")) {
                PropertyValue interpretHTMLValue = new PropertyValue();
                interpretHTMLValue.setType(BondType.CONSTANT.toJson());
                interpretHTMLValue.setValue(Boolean.TRUE);
                component.getPropertyValues().put("allowHTML", interpretHTMLValue);
            }
        }
        return Optional.empty();
    }
}
