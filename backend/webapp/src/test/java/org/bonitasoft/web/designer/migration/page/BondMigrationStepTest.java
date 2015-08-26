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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.EXPRESSION;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static org.bonitasoft.web.designer.model.widget.BondType.VARIABLE;
import static org.mockito.Mockito.when;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.bonitasoft.web.designer.visitor.VisitorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BondMigrationStepTest {

    @Mock
    WidgetRepository widgetRepository;

    BondMigrationStep bondMigrationStep;

    Component component = aComponent()
            .withWidgetId("widgetId")
            .withPropertyValue("foo", "data", "bar")
            .build();

    Container container = aContainer()
            .with(component)
            .withPropertyValue("foo", "data", "bar")
            .build();

    Page page = aPage().with(container).build();

    Property property = aProperty().name("foo").build();

    @Before
    public void setUp() throws Exception {
        bondMigrationStep = new BondMigrationStep(new ComponentVisitor(), widgetRepository, new VisitorFactory());

        when(widgetRepository.get("widgetId")).thenReturn(
                aWidget().property(property).build());
    }

    @Test
    public void should_migrate_constant_property() throws Exception {
        property.setBond(CONSTANT);
        property.setDefaultValue("baz");

        bondMigrationStep.migrate(page);

        PropertyValue foo = component.getPropertyValues().get("foo");
        assertThat(foo.getType()).isEqualTo("constant");
        assertThat(foo.getValue()).isEqualTo("baz");
    }

    @Test
    public void should_migrate_interpolated_property() throws Exception {
        property.setBond(INTERPOLATION);

        bondMigrationStep.migrate(page);

        PropertyValue foo = component.getPropertyValues().get("foo");
        assertThat(foo.getType()).isEqualTo("interpolation");
        assertThat(foo.getValue()).isEqualTo("{{bar}}");
    }

    @Test
    public void should_migrate_expression_property() throws Exception {
        property.setBond(EXPRESSION);

        bondMigrationStep.migrate(page);

        PropertyValue foo = component.getPropertyValues().get("foo");
        assertThat(foo.getType()).isEqualTo("expression");
        assertThat(foo.getValue()).isEqualTo("bar");
    }

    @Test
    public void should_migrate_variable_property() throws Exception {
        property.setBond(VARIABLE);

        bondMigrationStep.migrate(page);

        PropertyValue foo = component.getPropertyValues().get("foo");
        assertThat(foo.getType()).isEqualTo("variable");
        assertThat(foo.getValue()).isEqualTo("bar");
    }

    @Test
    public void should_migrate_a_container_properties() throws Exception {
        property.setBond(VARIABLE);

        bondMigrationStep.migrate(page);

        PropertyValue foo = container.getPropertyValues().get("foo");
        assertThat(foo.getType()).isEqualTo("expression");
        assertThat(foo.getValue()).isEqualTo("bar");
    }
}
