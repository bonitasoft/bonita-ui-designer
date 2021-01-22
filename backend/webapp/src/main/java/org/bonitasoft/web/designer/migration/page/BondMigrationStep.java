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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;


public class BondMigrationStep<T extends AbstractPage> implements MigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(BondMigrationStep.class);
    private ComponentVisitor componentVisitor;
    private WidgetRepository widgetRepository;
    private VisitorFactory visitorFactory;

    private Map<BondType, BondMigrationStrategy> migrationStrategies = ImmutableMap.<BondType, BondMigrationStrategy>builder()
            .put(BondType.CONSTANT, new ConstantBondMigrationStrategy())
            .put(BondType.INTERPOLATION, new InterpolationBondMigrationStrategy())
            .put(BondType.EXPRESSION, new ExpressionBondMigrationStrategy())
            .put(BondType.VARIABLE, new VariableBondMigrationStrategy())
            .build();

    public BondMigrationStep(ComponentVisitor componentVisitor, WidgetRepository widgetRepository, VisitorFactory visitorFactory) {
        this.componentVisitor = componentVisitor;
        this.widgetRepository = widgetRepository;
        this.visitorFactory = visitorFactory;
    }

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) throws Exception {
        try {
            for (Component component : page.accept(componentVisitor)) {
                Widget widget = widgetRepository.get(component.getId());
                for (Entry<String, PropertyValue> entry : component.getPropertyValues().entrySet()) {
                    Property property = widget.getProperty(entry.getKey());
                    String formerType = entry.getValue().getType();

                    migrationStrategies
                            .get(property != null ? property.getBond() : BondType.EXPRESSION)
                            .migrate(property, entry.getValue());

                    logTypeChange(component.getId(), formerType, entry);
                }
            }

            for (Element element : page.accept(visitorFactory.createAnyContainerVisitor())) {
                for (Map.Entry<String, PropertyValue> entry : element.getPropertyValues().entrySet()) {
                    migrationStrategies
                            .get(BondType.EXPRESSION)
                            .migrate(new Property(), entry.getValue());

                    logTypeChange(element.getClass().getSimpleName(), entry.getValue().getType(), entry);
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            throw e;
        }
    }

    private void logTypeChange(String name, String formerType, Entry<String, PropertyValue> entry) {
        String currentType = entry.getValue().getType();
        if (!formerType.equals(currentType)) {
            logger.info(format("[MIGRATION] %s property <%s> value type has been changed from <%s> to <%s>",
                    name,
                    entry.getKey(),
                    formerType,
                    currentType));
        }
    }

    @Override
    public String getErrorMessage() {
        return "";
    }
}
