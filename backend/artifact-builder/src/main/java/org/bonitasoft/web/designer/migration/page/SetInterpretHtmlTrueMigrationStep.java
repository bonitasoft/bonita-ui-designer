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

import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.AnyLocalContainerVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

public class SetInterpretHtmlTrueMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(SetInterpretHtmlTrueMigrationStep.class);
    private final ComponentVisitor componentVisitor;

    private static final String ALLOW_HTML_PROPERTY = "allowHTML";

    private static final String[] widgetWithAddedAllowHtml= {"pbAutocomplete", "pbButton", "pbCheckbox",
    "pbChecklist", "pbDatePicker", "pbDateTimePicker", "pbInput", "pbLink", "pbRadioButtons", "pbRichTextarea", "pbSaveButton", "pbSelect",
    "pbTextarea", "pbTitle", "pbUpload"};

    public SetInterpretHtmlTrueMigrationStep(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
    }

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) {
        boolean pageChanged = false;
        for (Component component : page.accept(componentVisitor)) {
            if (isAddedAllowHtmlProperty(component.getId())) {
                setAllowHtmlPropertyDefaultValue(component);
                pageChanged = true;
            }
        }

        // TabContainer is a specific case, since it's a container
        for (Element element : page.accept(new AnyLocalContainerVisitor())) {
            if (element instanceof TabContainer) {
                setAllowHtmlPropertyDefaultValue((TabContainer) element);
                pageChanged = true;
            }
        }

        if (pageChanged) {
            String mess = String.format(
                    "'Interpret HTML' property has been added in some widgets for page %s. Now, you can disable it if you don't need it. " +
                            "If you keep it enabled, this could potentially lead to HTML injections attacks. Please take into account these risks.",
                    page.getName());
            logger.info("[MIGRATION] " + mess);
            return Optional.of(MigrationStepReport.warningMigrationReport(page.getName(), mess, this.getClass().getName()));
        }
        return Optional.empty();
    }

    @Override
    public String getErrorMessage() {
        return "An error occurred during set allowHtml property migration";
    }

    private void setAllowHtmlPropertyDefaultValue(Component component) {
        if (!component.getPropertyValues().containsKey(ALLOW_HTML_PROPERTY)) {
            PropertyValue propertyValue = new PropertyValue();
            propertyValue.setType(BondType.CONSTANT.toJson());
            propertyValue.setValue(true);
            component.getPropertyValues().put(ALLOW_HTML_PROPERTY, propertyValue);
        }
    }

    private boolean isAddedAllowHtmlProperty(String componentId) {
        return Arrays.asList(widgetWithAddedAllowHtml).contains(componentId);
    }
}
