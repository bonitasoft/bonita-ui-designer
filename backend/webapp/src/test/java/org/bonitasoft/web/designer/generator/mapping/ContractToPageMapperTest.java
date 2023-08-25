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
import org.bonitasoft.web.designer.generator.mapping.data.BusinessQueryDataFactory;
import org.bonitasoft.web.designer.generator.parametrizedWidget.*;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.page.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.PropertyValueBuilder.*;
import static org.bonitasoft.web.designer.builder.VariableBuilder.*;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.*;

@ExtendWith(MockitoExtension.class)
public class ContractToPageMapperTest {

    JsonHandler jsonHandler = new JacksonJsonHandler(new ObjectMapper());
    private ContractInputToWidgetMapper contractToWidgetMapper = new ContractInputToWidgetMapper(new DimensionFactory(), jsonHandler);
    ContractToContainerMapper contractToContainerMapper = new ContractToContainerMapper(contractToWidgetMapper);

    @Test
    public void visit_a_contract_when_creating_a_page() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(getTaskFormContainerContent(page).getRows()).hasSize(6);
    }

    private ContractToPageMapper makeContractToPageMapper() {
        return new ContractToPageMapper(contractToWidgetMapper, contractToContainerMapper, jsonHandler, new DimensionFactory(), new BusinessQueryDataFactory());
    }

    @Test
    public void should_create_a_page_with_the_form_type() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getType()).isEqualTo("form");
    }

    @Test
    public void should_create_a_page_with_a_form_container() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getRows()).hasSize(2);
        assertThat(page.getRows().get(1).get(0)).isInstanceOf(FormContainer.class);
    }

    @Test
    public void create_a_page_with_form_input_and_output() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("formInput", aJSONVariable().value(jsonHandler.prettyPrint("{\"names\":[]}")).build()));
        assertThat(page.getVariables()).contains(entry("formOutput", anExpressionVariable().value("return {\n\tnames: $data.formInput.names\n}").build()));
    }

    @Test
    public void create_a_page_with_a_context_url_data_for_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("context", anURLVariable().value("../API/bpm/userTask/{{taskId}}/context").build()));
    }

    @Test
    public void create_a_page_with_a_id_expression_data_for_task_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("taskId", aUrlParameterVariable().value("id").build()));
    }

    @Test
    public void create_a_page_without_a_context_url_data_for_process_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.PROCESS);

        assertThat(page.getVariables()).doesNotContain(entry("context", anURLVariable().value("/bonita/API/bpm/userTask/{{taskId}}/context").build()));
    }

    @Test
    public void create_a_page_without_an_id_expression_data_for_process() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.PROCESS);

        assertThat(page.getVariables()).doesNotContain(entry("taskId", aUrlParameterVariable().value("id").build()));
    }

    @Test
    public void create_a_page_without_output_data_for_overview_scope() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContract().build(), FormScope.OVERVIEW);

        assertThat(page.getVariables()).isEmpty();
    }

    @Test
    public void create_a_page_with_a_submit_button_sending_contract() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithMultipleInput(), FormScope.TASK);

        Component submitButon = (Component) getTaskFormContainerContent(page).getRows().get(3).get(0);
        assertThat(submitButon.getId()).isEqualTo("pbButton");
        assertThat(submitButon.getPropertyValues()).contains(
                entry(ParameterConstants.DATA_TO_SEND_PARAMETER, anExpressionPropertyValue("formOutput")),
                entry(ParameterConstants.ACTION_PARAMETER, aConstantPropertyValue(ButtonAction.SUBMIT_TASK.getValue())),
                entry(ParameterConstants.TARGET_URL_ON_SUCCESS_PARAMETER, aInterpolationPropertyValue("/bonita")));
    }

    @Test
    public void create_a_page_and_fetch_associated_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("task", anURLVariable().value("../API/bpm/userTask/{{taskId}}").build()));
    }

    @Test
    public void create_a_page_display_task_name() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();
        TitleWidget title = new TitleWidget();
        title.setLevel("Level 1");
        title.setText("{{ task.displayName }}");
        title.setAlignment(Alignment.CENTER);

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);
        assertThat(grabTaskInformation(page).getRows().get(0).get(0).getPropertyValues().containsKey("class")).isFalse();
        assertThat(grabTaskInformation(page).getRows().get(0).get(0)).isEqualToIgnoringGivenFields(title.toComponent(new DimensionFactory()), "reference");
    }

    @Test
    public void create_a_page_display_task_description() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();
        TextWidget description = new TextWidget();
        description.setText("{{ task.displayDescription }}");

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContract(), FormScope.TASK);

        assertThat(grabTaskInformation(page).getRows().get(1).get(0)).isEqualToIgnoringGivenFields(description.toComponent(new DimensionFactory()), "reference");
    }

    @Test
    public void should_create_an_empty_container_when_contract_is_empty_and_scope_is_overview() throws Exception {
        Contract anEmptyContract = aContract().build();

        Page page = makeContractToPageMapper().createFormPage("myPage", anEmptyContract, FormScope.OVERVIEW);

        Container container = (Container) page.getRows().get(0).get(0);
        assertThat(container.getRows().get(0)).isEqualTo(new ArrayList<>());
    }

    @Test
    public void should_create_a_page_with_a_business_data_when_contract_contains_data_reference_on_a_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContractWithDataRef(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("employee",anURLVariable().value("../{{context.employee_ref.link}}").build()));
        assertThat(page.getVariables()).contains(entry("employee_addresses",anURLVariable().value("{{employee|lazyRef:'addresses'}}").build()));
        assertThat(page.getVariables()).contains(entry("employee_manager",anURLVariable().value("{{employee|lazyRef:'manager'}}").build()));
        assertThat(page.getVariables()).contains(entry("employee_manager_addresses",anURLVariable().value("{{employee_manager|lazyRef:'addresses'}}").build()));
        assertThat(page.getVariables()).doesNotContain(entry("employee_addresses_country",anURLVariable().value("{{employee_addresses|lazyRef:'country'}}").build()));
    }

    @Test
    public void should_create_a_page_with_a_query_varaible_when_contract_contains_data_reference_with_aggregation() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aContractWithDataRefAndAggregation(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getVariables()).contains(entry("employee_query",anURLVariable().value("../API/bdm/businessData/org.test.Employee?q=find&p=0&c=99").build()));
    }

    @Test
    public void should_create_a_page_without_formInput_when_contract_contains__only_data_reference_on_a_task() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContractWithDataRef(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getVariables().keySet()).doesNotContain("formInput");
    }

    @Test
    public void should_create_a_Text_widget_for_submit_error() throws Exception {
        ContractToPageMapper contractToPageMapper = makeContractToPageMapper();

        Page page = contractToPageMapper.createFormPage("myPage", aSimpleContractWithDataRef(EditMode.EDIT), FormScope.TASK);

        assertThat(page.getVariables().keySet()).contains("submit_errors_list");
       FormContainer formContainer = (FormContainer) page.getRows().get(1).get(0);
       Component submitButton = (Component) formContainer.getContainer().getRows().get(2).get(0);

       assertThat(submitButton.getId()).isEqualTo("pbButton");
       PropertyValue dataFromErrorProperty = submitButton.getPropertyValues().get("dataFromError");
       assertThat(dataFromErrorProperty).isNotNull();
       assertThat(dataFromErrorProperty.getValue()).isEqualTo("formOutput._submitError");

       Component errorText = (Component) formContainer.getContainer().getRows().get(3).get(0);
       assertThat(errorText.getId()).isEqualTo("pbText");
       PropertyValue hiddenProperty = errorText.getPropertyValues().get("hidden");
       assertThat(hiddenProperty).isNotNull();
       assertThat(hiddenProperty.getValue()).isEqualTo("!formOutput._submitError.message");
    }

    private Container grabTaskInformation(Page page) {
        return (Container) page.getRows().get(0).get(0);
    }

    public Container getTaskFormContainerContent(Page page) {
        return ((FormContainer) page.getRows().get(1).get(0)).getContainer();
    }
}
