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
import static org.bonitasoft.web.designer.builder.DataBuilder.*;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aConstantPropertyValue;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.aDataPropertyValue;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.*;

import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;

public class ContractToPageMapperTest {

    @Test
    public void visit_a_contract_when_creating_a_page() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aSimpleTaskContract());

        assertThat(page.getRows()).hasSize(5);
    }

    private ContractToPageMapper makeContractToPageMapper() {
        return new ContractToPageMapper(new ContractInputToWidgetMapper());
    }

    @Test
    public void create_a_page_with_a_sentData_json_data() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aContractWithMultipleInput());

        assertThat(page.getData()).contains(entry("sentData", aJSONData().value("{}").build()));
    }

    @Test
    public void create_a_page_with_a_context_url_data_for_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aContractWithMultipleInput());

        assertThat(page.getData()).contains(entry("context", anURLData().value("/bonita/API/bpm/userTask/{{taskId}}/context").build()));
    }

    @Test
    public void create_a_page_with_a_id_expression_data_for_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aContractWithMultipleInput());

        assertThat(page.getData()).contains(entry("taskId", aUrlParameterData().value("id").build()));
    }

    @Test
    public void create_a_page_without_a_context_url_data_for_process() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aSimpleProcessContract());

        assertThat(page.getData()).doesNotContain(entry("context", anURLData().value("/bonita/API/bpm/userTask/{{id}}/context").build()));
    }

    @Test
    public void create_a_page_without_an_id_expression_data_for_process() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aSimpleProcessContract());

        assertThat(page.getData()).doesNotContain(entry("taskId", aUrlParameterData().value("id").build()));
    }

    @Test
    public void create_a_page_with_a_submit_button_sending_contract() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createPage("myPage", aContractWithMultipleInput());

        Component submitButon = (Component) page.getRows().get(3).get(0);
        assertThat(submitButon.getWidgetId()).isEqualTo("pbButton");
        assertThat(submitButon.getPropertyValues()).contains(
                entry(ParameterConstants.DATA_TO_SEND_PARAMETER, aDataPropertyValue("sentData")),
                entry(ParameterConstants.ACTION_PARAMETER, aConstantPropertyValue(ButtonAction.SUBMIT_TASK.getValue())));
    }
}
