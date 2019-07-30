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
package org.bonitasoft.web.designer.visitor;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.widget.BondType;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;

public class FixBondsTypesVisitorTest {

    private FixBondsTypesVisitor fixBondsTypesVisitor = new FixBondsTypesVisitor(asList(
            aProperty().name("foo").bond(BondType.CONSTANT).build(),
            aProperty().name("bar").bond(BondType.EXPRESSION).build()));

    private Component component = aComponent()
            .withPropertyValue("foo", "expression", "value")
            .withPropertyValue("bar", "interpolation", "value")
            .build();

    @Test
    public void should_fix_component_bond_when_visiting_it() {

        fixBondsTypesVisitor.visit(component);

        assertThat(component.getPropertyValues().get("bar").getType()).isEqualTo("constant");
    }

    @Test
    public void should_not_change_bond_if_type_in_inbound_expression_value() {

        fixBondsTypesVisitor.visit(component);

        assertThat(component.getPropertyValues().get("foo").getType()).isEqualTo("constant");
    }

    @Test
    public void should_visit_container_rows() {

        fixBondsTypesVisitor.visit(aContainer().with(component).build());

        assertThat(component.getPropertyValues().get("bar").getType()).isEqualTo("constant");
    }

    @Test
    public void should_visit_previewable_rows() {
        fixBondsTypesVisitor.visit(aPage().with(component).build());

        assertThat(component.getPropertyValues().get("bar").getType()).isEqualTo("constant");
    }

    @Test
    public void should_visit_container_from_tabs_container() {
        fixBondsTypesVisitor.visit(aTabsContainer().with(aTabContainer().with(aContainer().with(component))).build());

        assertThat(component.getPropertyValues().get("bar").getType()).isEqualTo("constant");
    }
}
