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
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleTaskContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLongContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.Test;

public class ContractInputToWidgetMapperTest {

    @Test
    public void should_string_contract_input_create_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aStringContractInput("firstName"), Collections.<List<Element>> emptyList());

        assertThat(element.getWidgetId()).isEqualTo("pbInput");
    }

    @Test
    public void should_map_simple_numeric_input_to_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aLongContractInput("updateTime"), Collections.<List<Element>> emptyList());

        assertThat(element.getWidgetId()).isEqualTo("pbInput");
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

        Element element = contractInputToWidgetMapper.createSubmitButton(aSimpleTaskContract());

        assertThat(element.getPropertyValues().get("action").getValue()).isEqualTo("Submit task");
    }

    @Test
    public void should_submit_button_send_sentData_data() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(aSimpleTaskContract());

        PropertyValue PropertyValue = element.getPropertyValues().get("dataToSend");
        assertThat(PropertyValue.getType()).isEqualTo("data");
        assertThat(PropertyValue.getValue()).isEqualTo("sentData");
    }

    @Test
    public void should_string_contract_input_has_value_configured_on_sentData() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aStringContractInput("firstName"), Collections.<List<Element>> emptyList());

        PropertyValue valueParameter = element.getPropertyValues().get("value");
        assertThat(valueParameter.getType()).isEqualTo("data");
        assertThat(valueParameter.getValue()).isEqualTo("sentData.firstName");
    }

    private ContractInputToWidgetMapper makeContractInputToWidgetMapper() {
        return new ContractInputToWidgetMapper();
    }

}
