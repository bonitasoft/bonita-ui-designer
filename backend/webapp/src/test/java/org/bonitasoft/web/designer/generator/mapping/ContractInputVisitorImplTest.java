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
package org.bonitasoft.web.designer.generator.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.contract.builders.ContractBuilder;
import org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aDataPropertyValue;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aInterpolationPropertyValue;

public class ContractInputVisitorImplTest {

    private final JsonHandler jsonHandler = new JacksonJsonHandler(new ObjectMapper());
    private ContractInputToWidgetMapper contractInputToWidgetMapper;

    @BeforeEach
    public void setUp() throws Exception {
        contractInputToWidgetMapper = new ContractInputToWidgetMapper(new DimensionFactory(), jsonHandler);
    }

    @Test
    public void add_a_one_row_component_when_visiting_a_leaf_contract_input() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper)
                .visit(ContractInputBuilder.aStringContractInput("name"));

        assertThat(page.getRows()).hasSize(1);
        assertThat(page.getRows().get(0)).hasSize(1);
        assertThat(page.getRows().get(0).get(0)).isInstanceOf(Component.class);
    }

    @Test
    public void do_nothing_when_visiting_a_leaf_contract_input_with_unsupported_type() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper)
                .visit(new LeafContractInput("unsupported", IllegalArgumentException.class));

        assertThat(page.getRows()).hasSize(0);
    }

    @Test
    public void add_a_component_embedded_in_a_container_with_buttons_when_visiting_a_multiple_leaf_contract_input()
            throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper)
                .visit((LeafContractInput) ContractInputBuilder.aContractInput("names").mulitple().build());

        assertThat(page.getRows()).hasSize(3);//one for the container label, one for the container, one for the button bar

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getId()).isEqualTo("pbTitle");
        assertThat(firstRow.get(0).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aInterpolationPropertyValue("Names")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(1);
        List<Element> secondRowInner = container.getRows().get(0);
        assertThat(secondRowInner).hasSize(2);
        Component component = (Component) secondRowInner.get(0);
        assertThat(component.getId()).isEqualTo("pbInput");
        assertThat(component.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("")));
        Component removeButton = (Component) secondRowInner.get(1);
        assertThat(removeButton.getId()).isEqualTo("pbButton");

        List<Element> thirdRow = page.getRows().get(2);
        assertThat(thirdRow).hasSize(1);
        Component addButton = (Component) thirdRow.get(0);
        assertThat(addButton.getId()).isEqualTo("pbButton");
        assertThat(addButton.getPropertyValues().get("valueToAdd").getValue()).isNull();

    }

    @Test
    public void add_a_children_components_embedded_in_a_container_when_visiting_a_node_contract_input()
            throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper)
                .visit((NodeContractInput) ContractInputBuilder.aNodeContractInput("employee").withInput(
                        ContractInputBuilder.aStringContractInput("firstName"),
                        ContractInputBuilder.aStringContractInput("lastName")).build());

        assertThat(page.getRows()).hasSize(2);//one for the container label, one for the container

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getId()).isEqualTo("pbTitle");
        assertThat(firstRow.get(0).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aInterpolationPropertyValue("Employee")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(2);
        assertThat(container.getRows().get(0)).hasSize(1);

        Component firstNameComponent = (Component) container.getRows().get(0).get(0);
        assertThat(firstNameComponent.getId()).isEqualTo("pbInput");
        assertThat(firstNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("First Name")));

        Component lastNameComponent = (Component) container.getRows().get(1).get(0);
        assertThat(lastNameComponent.getId()).isEqualTo("pbInput");
        assertThat(lastNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("Last Name")));
    }

    @Test
    public void add_a_children_components_embedded_in_a_container_with_buttons_when_visiting_a_multiple_node_contract_input()
            throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper).visit(
                (NodeContractInput) ContractInputBuilder.aNodeContractInput("employee").withInput(
                        ContractInputBuilder.aStringContractInput("firstName"),
                        ContractInputBuilder.aStringContractInput("lastName")).mulitple().build());

        assertThat(page.getRows()).hasSize(3);//one for the container label, one for the container,one for the button bar

        List<Element> firstRow = page.getRows().get(0);
        assertThat(firstRow).hasSize(1);
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(((Component) firstRow.get(0)).getId()).isEqualTo("pbTitle");
        assertThat(firstRow.get(0).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aInterpolationPropertyValue("Employee")));

        List<Element> secondRow = page.getRows().get(1);
        assertThat(secondRow).hasSize(1);
        assertThat(secondRow.get(0)).isInstanceOf(Container.class);
        Container container = (Container) secondRow.get(0);
        assertThat(container.getRows()).hasSize(3);
        assertThat(container.getRows().get(0)).hasSize(1);

        Component firstNameComponent = (Component) container.getRows().get(0).get(0);
        assertThat(firstNameComponent.getId()).isEqualTo("pbInput");
        assertThat(firstNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("First Name")));

        assertThat(container.getRows().get(1)).hasSize(1);
        Component lastNameComponent = (Component) container.getRows().get(1).get(0);
        assertThat(lastNameComponent.getId()).isEqualTo("pbInput");
        assertThat(lastNameComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("Last Name")));

        assertThat(container.getRows().get(2)).hasSize(1);
        Component removeButton = (Component) container.getRows().get(2).get(0);
        assertThat(removeButton.getId()).isEqualTo("pbButton");

        List<Element> thirdRow = page.getRows().get(2);
        assertThat(thirdRow).hasSize(1);
        Component addButton = (Component) thirdRow.get(0);
        assertThat(addButton.getId()).isEqualTo("pbButton");
        assertThat(addButton.getPropertyValues().get("valueToAdd").getValue()).isEqualTo("{" + System.lineSeparator() +
                "  \"firstName\" : \"\"," + System.lineSeparator() +
                "  \"lastName\" : \"\"" + System.lineSeparator() +
                "}");
    }

    @Test
    public void add_a_complex_children_components_embedded_in_a_container_when_visiting_a_multiple_complex_node_contract_input()
            throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper).visit(
                (NodeContractInput) ContractInputBuilder.aNodeContractInput("complex").mulitple()
                        .withInput(
                                ContractInputBuilder.aNodeContractInput("subcomplex")
                                        .withInput(ContractInputBuilder.aStringContractInput("lastName")).build())
                        .withInput(
                                ContractInputBuilder.aNodeContractInput("subcomplexMultiple").mulitple()
                                        .withInput(ContractInputBuilder.aStringContractInput("friend")).build())
                        .build());

        assertThat(page.getRows()).hasSize(3);//one for the container label, one for the container,one for the button bar

        checkSubcomplexTitle(page.getRows().get(0), "Complex");

        List<Element> complexSecondRow = page.getRows().get(1);
        assertThat(complexSecondRow).hasSize(1);
        assertThat(complexSecondRow.get(0)).isInstanceOf(Container.class);

        Container complexContainer = (Container) complexSecondRow.get(0);
        assertThat(complexContainer.getPropertyValues())
                .contains(entry(ParameterConstants.REPEATED_COLLECTION_PARAMETER,
                        aDataPropertyValue("formInput.complex")));
        assertThat(complexContainer.getRows()).hasSize(6);
        assertThat(complexContainer.getRows().get(0)).hasSize(1);

        checkSubcomplex(complexContainer);

        checkComplexRemoveButton(complexContainer, 5);

        List<Element> thirdRow = page.getRows().get(2);
        assertThat(thirdRow).hasSize(1);
        Component addButton = (Component) thirdRow.get(0);
        assertThat(addButton.getId()).isEqualTo("pbButton");
        assertThat(addButton.getPropertyValues().get("valueToAdd").getValue()).isEqualTo("{" + System.lineSeparator() +
                "  \"subcomplex\" : {" + System.lineSeparator() +
                "    \"lastName\" : \"\"" + System.lineSeparator() +
                "  }," + System.lineSeparator() +
                "  \"subcomplexMultiple\" : [ ]" + System.lineSeparator() +
                "}");
    }

    @Test
    public void should_not_add_simple_aggregated_relation_in_a_container() throws Exception {
        Page page = new Page();

        new ContractInputVisitorImpl(page, contractInputToWidgetMapper)
                .visit((NodeContractInput) ContractBuilder.aContractWithDataRefAndAggregation(EditMode.CREATE).getInput().get(0));

        Container formContainer = (Container) page.getRows().get(1).get(0);
        Component select = (Component) formContainer.getRows().get(3).get(0);
        assertThat(select.getId()).isEqualTo("pbSelect");

    }

    private void checkComplexRemoveButton(Container complexContainer, int index) {
        assertThat(complexContainer.getRows().get(index)).hasSize(1);
        Component removeButton = (Component) complexContainer.getRows().get(index).get(0);
        assertThat(removeButton.getPropertyValues().get("collectionToModify").getValue()).isEqualTo("$collection");
        assertThat(removeButton.getId()).isEqualTo("pbButton");
    }

    private void checkSubcomplexTitle(List<Element> complexFirstRow, String title) {
        assertThat(complexFirstRow).hasSize(1);
        assertThat(((Component) complexFirstRow.get(0)).getId()).isEqualTo("pbTitle");
        assertThat(complexFirstRow.get(0).getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aInterpolationPropertyValue(title)));
    }

    private void checkSubcomplex(Container complexContainer) {
        Component subcomplexComponent = (Component) complexContainer.getRows().get(0).get(0);
        assertThat(subcomplexComponent.getId()).isEqualTo("pbTitle");
        assertThat(subcomplexComponent.getPropertyValues()).contains(
                entry(ParameterConstants.TEXT_PARAMETER, aInterpolationPropertyValue("Subcomplex")));

        List<Element> subcomplexSecondRow = complexContainer.getRows().get(1);
        assertThat(subcomplexSecondRow).hasSize(1);
        assertThat(subcomplexSecondRow.get(0)).isInstanceOf(Container.class);

        Container subComplexContainer = (Container) subcomplexSecondRow.get(0);
        assertThat(subComplexContainer.getPropertyValues())
                .contains(entry(ParameterConstants.REPEATED_COLLECTION_PARAMETER,
                        aDataPropertyValue(null)));
        assertThat(subComplexContainer.getRows()).hasSize(1);
        assertThat(subComplexContainer.getRows().get(0)).hasSize(1);

        Component lastnameInputComponent = (Component) subComplexContainer.getRows().get(0).get(0);
        assertThat(lastnameInputComponent.getId()).isEqualTo("pbInput");
        assertThat(lastnameInputComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("Last Name")),
                entry(ParameterConstants.VALUE_PARAMETER, aDataPropertyValue("$item.subcomplex.lastName")));

        checkSubcomplexTitle(complexContainer.getRows().get(2), "Subcomplex Multiple");

        List<Element> complexFourthRow = complexContainer.getRows().get(3);
        assertThat(complexFourthRow).hasSize(1);
        assertThat(complexFourthRow.get(0)).isInstanceOf(Container.class);

        Container complexSecondContainer = (Container) complexFourthRow.get(0);
        assertThat(complexSecondContainer.getPropertyValues())
                .contains(entry(ParameterConstants.REPEATED_COLLECTION_PARAMETER,
                        aDataPropertyValue("$item.subcomplexMultiple")));
        assertThat(complexSecondContainer.getRows()).hasSize(2);
        assertThat(complexSecondContainer.getRows().get(0)).hasSize(1);

        Component friendsInputComponent = (Component) complexSecondContainer.getRows().get(0).get(0);
        assertThat(friendsInputComponent.getId()).isEqualTo("pbInput");
        assertThat(friendsInputComponent.getPropertyValues()).contains(
                entry(ParameterConstants.LABEL_PARAMETER, aInterpolationPropertyValue("Friend")),
                entry(ParameterConstants.VALUE_PARAMETER, aDataPropertyValue("$item.friend")));

        checkComplexRemoveButton(complexSecondContainer, 1);

        List<Element> addButtonRow = complexContainer.getRows().get(4);
        assertThat(addButtonRow).hasSize(1);
        Component addButton = (Component) addButtonRow.get(0);
        assertThat(addButton.getId()).isEqualTo("pbButton");
        assertThat(addButton.getPropertyValues().get("valueToAdd").getValue()).isEqualTo("{" + System.lineSeparator() +
                "  \"friend\" : \"\"" + System.lineSeparator() +
                "}");
    }
}
