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

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;

import java.util.Optional;

import static java.lang.String.format;

@Slf4j
public class AccessibilityCheckListAndRadioButtonsMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<T> {

    private ComponentVisitor componentVisitor;

    public AccessibilityCheckListAndRadioButtonsMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) throws Exception {
        try {
            for (Component component : page.accept(componentVisitor)) {
                if (isProvidedCheckListOrRadiobuttonsWidget(component.getId())) {
                    var msg = format("Internal HTML template for Checklist and Radiobuttons have been update. Please check your css selector if you apply some custom style on theses widgets. You can find more details in Editor > Help > Migration section.");
                    log.info("[MIGRATION] [{}] {}", page.getName(),msg);
                    return Optional.of(MigrationStepReport.warningMigrationReport(page.getName(), msg, this.getClass().getName()));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean isProvidedCheckListOrRadiobuttonsWidget(String componentId) {
        return "pbChecklist".equals(componentId) || "pbRadioButtons".equals(componentId);
    }
}
