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
package org.bonitasoft.web.designer.experimental.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.Test;

public class ContractInputToWidgetMapperTest {

    @Test
    public void should_string_contract_input_create_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aStringContractInput("firstName"), Collections.<List<Element>> emptyList());

        assertThat(element.getId()).isEqualTo("pbInput");
    }

    @Test
    public void should_map_simple_numeric_input_to_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aLongContractInput("updateTime"), Collections.<List<Element>> emptyList());

        assertThat(element.getId()).isEqualTo("pbInput");
    }

    @Test
    public void should_map_input_name_to_a_widget_display_label() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper
                .toElement(aStringContractInput("aComplicatedCamelCaseInputName"), Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("label").getValue()).isEqualTo("A Complicated Camel Case Input Name");
    }

    @Test
    public void should_map_a_numeric_contract_input_to_an_input_with_number_type() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aLongContractInput("timestamp"), Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("type").getValue()).isEqualTo("number");
    }

    @Test
    public void should_map_a_non_numeric_contract_input_to_an_input_with_text_type() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aStringContractInput("name"), Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("type").getValue()).isEqualTo("text");
    }

    @Test
    public void should_map_contract_input_description_to_an_input_placeholder() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        LeafContractInput contractInput = (LeafContractInput) aStringContractInput("name");
        contractInput.setDescription("name of the user");
        Element element = contractInputToWidgetMapper.toElement(contractInput, Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("placeholder").getValue()).isEqualTo("name of the user");
    }

    @Test
    public void should_submit_button_have_contract_action() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(aSimpleContract(), ButtonAction.SUBMIT_TASK);

        assertThat(element.getPropertyValues().get("action").getValue()).isEqualTo("Submit task");
    }

    @Test
    public void should_submit_button_send_sentData_variable() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(aSimpleContract(), ButtonAction.SUBMIT_TASK);

        PropertyValue PropertyValue = element.getPropertyValues().get("dataToSend");
        assertThat(PropertyValue.getType()).isEqualTo("variable");
        assertThat(PropertyValue.getValue()).isEqualTo("formOutput");
    }

    @Test
    public void submit_button_should_redirect_to_bonita_on_success() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(aSimpleContract(), ButtonAction.SUBMIT_TASK);

        PropertyValue PropertyValue = element.getPropertyValues().get("targetUrlOnSuccess");
        assertThat(PropertyValue.getType()).isEqualTo("interpolation");
        assertThat(PropertyValue.getValue()).isEqualTo("/bonita");
    }

    @Test
    public void should_string_contract_input_has_value_configured_on_sentData() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aStringContractInput("firstName"), Collections.<List<Element>> emptyList());

        PropertyValue valueParameter = element.getPropertyValues().get("value");
        assertThat(valueParameter.getType()).isEqualTo("variable");
        assertThat(valueParameter.getValue()).isEqualTo("formOutput.firstName");
    }

    @Test
    public void should_configure_value_property_of_container_with_$item_when_generating_a_multiple_string_in_a_multiple_node_contract_input() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput contractInput = aContractInput("names").mulitple().withType(String.class.getName()).build();
        aNodeContractInput("employee").mulitple()
                .withInput(contractInput).build();
        Element container = contractInputToWidgetMapper.toElement(contractInput,
                new ArrayList<List<Element>>());

        assertThat(container).isInstanceOf(Container.class);
        PropertyValue repeatedCollectionPropetyValue = container.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$item.names");
    }

    @Test
    public void should_configure_value_property_of_container_with_sentData_when_generating_a_multiple_node_input() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Container container = contractInputToWidgetMapper.toContainer((NodeContractInput) aNodeContractInput("employee").mulitple().build(),
                new ArrayList<List<Element>>());

        PropertyValue repeatedCollectionPropetyValue = container.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("formOutput.employee");
    }

    @Test
    public void should_configure_collection_property_of_add_button_with_iterator_if_in_a_repeated_container() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput skills = aNodeContractInput("skills").mulitple().build();
        aNodeContractInput("employee").mulitple().withInput(skills);
        Component button = contractInputToWidgetMapper.createAddButton(skills);

        PropertyValue repeatedCollectionPropetyValue = button.getPropertyValues().get("collectionToModify");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$item.skills");
    }

    @Test
    public void should_configure_collection_property_of_remove_button_with_iterator_if_in_a_repeated_container() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput skills = aNodeContractInput("skills").mulitple().build();
        aNodeContractInput("employee").mulitple().withInput(skills);
        Component button = contractInputToWidgetMapper.createRemoveButton(skills);

        PropertyValue repeatedCollectionPropetyValue = button.getPropertyValues().get("collectionToModify");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$item.skills");
    }

    private ContractInputToWidgetMapper makeContractInputToWidgetMapper() {
        return new ContractInputToWidgetMapper();
    }

}
