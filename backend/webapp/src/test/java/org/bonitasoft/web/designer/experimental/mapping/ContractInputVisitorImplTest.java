/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.experimental.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aConstantPropertyValue;

import java.util.List;

import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;

public class ContractInputVisitorImplTest {

    @Test
    public void add_a_one_row_component_when_visiting_a_leaf_contract_input() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, new ContractInputToWidgetMapper()).visit(ContractInputBuilder.aStringContractInput("name"));

        assertThat(page.getRows()).hasSize(1);
        assertThat(page.getRows().get(0)).hasSize(1);
        assertThat(page.getRows().get(0).get(0)).isInstanceOf(Component.class);
    }

    @Test
    public void do_nothing_when_visiting_a_leaf_contract_input_with_unsupported_type() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, new ContractInputToWidgetMapper()).visit(new LeafContractInput("unsupported", IllegalArgumentException.class));

        assertThat(page.getRows()).hasSize(0);
    }

    @Test
    public void add_a_component_embedded_in_a_container_with_buttons_when_visiting_a_multiple_leaf_contract_input() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, new ContractInputToWidgetMapper()).visit((LeafContractInput) ContractInputBuilder.aContractInput("names").mulitple().build());

        assertThat(page.getRows()).hasSize(3);//one for the container label, one for the container, one for the button bar

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getWidgetId()).isEqualTo("pbTitle");
        assertThat(((Component) firstRow.get(0)).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aConstantPropertyValue("Names")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(1);
        assertThat(container.getRows().get(0)).hasSize(1);
        Component component = (Component) container.getRows().get(0).get(0);
        assertThat(component.getWidgetId()).isEqualTo("pbInput");
        assertThat(component.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aConstantPropertyValue("")));

        List<Element> thirdRow = page.getRows().get(2);
        assertThat(thirdRow).hasSize(2);
        Component addButton = (Component) thirdRow.get(0);
        assertThat(addButton.getWidgetId()).isEqualTo("pbButton");
        Component removeButton = (Component) thirdRow.get(1);
        assertThat(removeButton.getWidgetId()).isEqualTo("pbButton");
    }

    @Test
    public void add_a_children_components_embedded_in_a_container_when_visiting_a_node_contract_input() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, new ContractInputToWidgetMapper()).visit((NodeContractInput) ContractInputBuilder.aNodeContractInput("employee").withInput(
                ContractInputBuilder.aStringContractInput("firstName"),
                ContractInputBuilder.aStringContractInput("lastName")).build());

        assertThat(page.getRows()).hasSize(2);//one for the container label, one for the container

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getWidgetId()).isEqualTo("pbTitle");
        assertThat(((Component) firstRow.get(0)).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aConstantPropertyValue("Employee")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(2);
        assertThat(container.getRows().get(0)).hasSize(1);

        Component firstNameComponent = (Component) container.getRows().get(0).get(0);
        assertThat(firstNameComponent.getWidgetId()).isEqualTo("pbInput");
        assertThat(firstNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aConstantPropertyValue("First Name")));

        Component lastNameComponent = (Component) container.getRows().get(1).get(0);
        assertThat(lastNameComponent.getWidgetId()).isEqualTo("pbInput");
        assertThat(lastNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aConstantPropertyValue("Last Name")));
    }

    @Test
    public void add_a_children_components_embedded_in_a_container_with_buttons_when_visiting_a_multiple_node_contract_input() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, new ContractInputToWidgetMapper()).visit((NodeContractInput) ContractInputBuilder.aNodeContractInput("employee").withInput(
                ContractInputBuilder.aStringContractInput("firstName"),
                ContractInputBuilder.aStringContractInput("lastName")).mulitple().build());

        assertThat(page.getRows()).hasSize(3);//one for the container label, one for the container,one for the button bar

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getWidgetId()).isEqualTo("pbTitle");
        assertThat(((Component) firstRow.get(0)).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aConstantPropertyValue("Employee")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(2);
        assertThat(container.getRows().get(0)).hasSize(1);

        Component firstNameComponent = (Component) container.getRows().get(0).get(0);
        assertThat(firstNameComponent.getWidgetId()).isEqualTo("pbInput");
        assertThat(firstNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aConstantPropertyValue("First Name")));

        Component lastNameComponent = (Component) container.getRows().get(1).get(0);
        assertThat(lastNameComponent.getWidgetId()).isEqualTo("pbInput");
        assertThat(lastNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aConstantPropertyValue("Last Name")));

        List<Element> thirdRow = page.getRows().get(2);
        assertThat(thirdRow).hasSize(2);
        Component addButton = (Component) thirdRow.get(0);
        assertThat(addButton.getWidgetId()).isEqualTo("pbButton");
        Component removeButton = (Component) thirdRow.get(1);
        assertThat(removeButton.getWidgetId()).isEqualTo("pbButton");
    }
}
