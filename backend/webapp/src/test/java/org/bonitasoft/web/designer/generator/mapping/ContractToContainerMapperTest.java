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

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContractWithDataRefAndAggregation;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContractWithDataRef;

public class ContractToContainerMapperTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Test
    public void should_set_value_relative_to_business_data_in_edit_mode() throws Exception {
        ContractToContainerMapper contractToContainerMapper = makeContractToContainerMapper();

        Container container = contractToContainerMapper.create(aSimpleContractWithDataRef(EditMode.EDIT));

        Container formContainer = (Container) container.getRows().get(1).get(0);
        List<Element> firstRow = formContainer.getRows().get(0);
        Component firstNameInput = (Component) firstRow.get(0);
        PropertyValue pValue = firstNameInput.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("employee.firstName");

        List<Element> fourthRow = formContainer.getRows().get(4);
        Container managerContainer = (Container) fourthRow.get(0);
        Element managerfirstName = managerContainer.getRows().get(0).get(0);
        pValue = managerfirstName.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("employee_manager.firstName");

        List<Element> sixthRow = formContainer.getRows().get(6);
        Container addressContainer = (Container) sixthRow.get(0);
        PropertyValue repeatedCollection = addressContainer.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollection.getType()).isEqualTo("variable");
        assertThat(repeatedCollection.getValue()).isEqualTo("employee_addresses");

        Component streetInput = (Component) addressContainer.getRows().get(0).get(0);
        pValue = streetInput.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("$item.street");
    }

    @Test
    public void should_set_value_relative_to_formInput_in_create_mode() throws Exception {
        ContractToContainerMapper contractToContainerMapper = makeContractToContainerMapper();

        Container container = contractToContainerMapper.create(aSimpleContractWithDataRef(EditMode.CREATE));

        Container formContainer = (Container) container.getRows().get(1).get(0);
        List<Element> firstRow = formContainer.getRows().get(0);
        Component firstNameInput = (Component) firstRow.get(0);
        PropertyValue pValue = firstNameInput.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("formInput.employeeInput.firstName");

        List<Element> fourthRow = formContainer.getRows().get(4);
        Container managerContainer = (Container) fourthRow.get(0);
        Element managerfirstName = managerContainer.getRows().get(0).get(0);
        pValue = managerfirstName.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("formInput.employeeInput.manager.firstName");

        List<Element> sixthRow = formContainer.getRows().get(6);
        Container addressContainer = (Container) sixthRow.get(0);
        PropertyValue repeatedCollection = addressContainer.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollection.getType()).isEqualTo("variable");
        assertThat(repeatedCollection.getValue()).isEqualTo("formInput.employeeInput.addresses");

        Component streetInput = (Component) addressContainer.getRows().get(0).get(0);
        pValue = streetInput.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("$item.street");
    }

    @Test
    public void should_create_a_Select_widget_for_aggregated_dataRef() throws Exception {
        ContractToContainerMapper contractToContainerMapper = makeContractToContainerMapper();

        Container container = contractToContainerMapper.create(aContractWithDataRefAndAggregation(EditMode.EDIT));
        Container formContainer = (Container) container.getRows().get(1).get(0);
        List<Element> fourthRow = formContainer.getRows().get(3);
        Component managerSelect = (Component) fourthRow.get(0);
        assertThat(managerSelect.getId()).isEqualTo("pbSelect");

        PropertyValue pValue = managerSelect.getPropertyValues().get("availableValues");
        assertThat(pValue.getType()).isEqualTo("expression");
        assertThat(pValue.getValue()).isEqualTo("employee_query");

        pValue = managerSelect.getPropertyValues().get("value");
        assertThat(pValue.getType()).isEqualTo("variable");
        assertThat(pValue.getValue()).isEqualTo("employee.manager");
    }

    private ContractToContainerMapper makeContractToContainerMapper() {
        return new ContractToContainerMapper(new ContractInputToWidgetMapper(new DimensionFactory(), jsonHandler));
    }
}
