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
package org.bonitasoft.web.designer.experimental.parametrizedWidget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.*;

import org.bonitasoft.web.designer.experimental.assertions.AbstractParametrizedWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.ButtonWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.DatePickerWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.InputWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.TitleWidgetAssert;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParametrizedWidgetFactoryTest implements ParameterConstants {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create_an_input_widget_for_string_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aStringContractInput("name"));

        assertThat(component).isInstanceOf(InputWidget.class);
    }

    @Test
    public void create_a_text_input_for_string_contract_input_with_a_display_label() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        InputWidget component = (InputWidget) elementFactory.createParametrizedWidget(aStringContractInput("name"));

        InputWidgetAssert.assertThat(component)
                .hasLabelPosition(LabelPosition.TOP.getValue())
                .hasLabelWidth(1)
                .hasType(InputType.TEXT.getValue());
        AbstractParametrizedWidgetAssert.assertThat(component).isDisplayed()
                .hasLabel("Name")
                .isNotLabelHidden()
                .isNotReadonly();
    }

    @Test
    public void create_a_text_input_for_string_contract_input_with_description_as_placeholder() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        LeafContractInput contractInput = aStringContractInput("name");
        contractInput.setDescription("My description");
        InputWidget component = (InputWidget) elementFactory.createParametrizedWidget(contractInput);

        InputWidgetAssert.assertThat(component).hasPlaceholder(contractInput.getDescription());
    }

    @Test
    public void create_a_text_input_for_string_contract_input_with_full_with() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aStringContractInput("name"));

        assertThat(component.getDimension()).isEqualTo(12);
    }

    @Test
    public void create_an_input_widget_for_numeric_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aLongContractInput("name"));

        assertThat(component).isInstanceOf(InputWidget.class);
    }

    @Test
    public void create_a_datepicker_for_date_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aDateContractInput("creationDate"));

        assertThat(component).isInstanceOf(DatePickerWidget.class);
    }

    @Test
    public void create_a_checkbox_for_boolean_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(new LeafContractInput("isValidated", Boolean.class));

        assertThat(component).isInstanceOf(CheckboxWidget.class);
        assertThat(component.getLabel()).isEqualTo("Is Validated");
    }

    @Test
    public void create_a_file_upload_for_file_contract_input_with_a_default_url() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aFileContractInput("document"));

        assertThat(component).isInstanceOf(FileUploadWidget.class);
        assertThat(component.getLabel()).isEqualTo("Document");
        assertThat(((FileUploadWidget) component).getUrl()).isEqualTo("/bonita/API/formFileUpload");
    }

    @Test
    public void create_a_datepicker_for_date_contract_input_with_date_format() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        DatePickerWidget component = (DatePickerWidget) elementFactory.createParametrizedWidget(aDateContractInput("creationDate"));

        DatePickerWidgetAssert.assertThat(component).hasDateFormat("MM/dd/yyyy");
    }

    @Test
    public void throw_a_IllegalArgumentException_for_unsupported_input_type() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        thrown.expect(IllegalArgumentException.class);

        elementFactory.createParametrizedWidget(new LeafContractInput("unsupported", IllegalArgumentException.class));
    }

    @Test
    public void create_a_label_component_with_contract_input_name_as_display_label() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        TitleWidget component = elementFactory.createTitle(aStringContractInput("isValid"));

        TitleWidgetAssert.assertThat(component).hasText("Is Valid");
        AbstractParametrizedWidgetAssert.assertThat(component).hasAlignment(LabelPosition.LEFT.getValue()).isDisplayed();
    }

    @Test
    public void create_a_label_component_with_contract_input_with_full_with() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createTitle(aStringContractInput("isValid"));

        assertThat(component.getDimension()).isEqualTo(12);
    }

    @Test
    public void create_a_container_with_full_size() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        WidgetContainer container = elementFactory.createWidgetContainer();

        assertThat(container.getDimension()).isEqualTo(12);
    }

    @Test
    public void create_a_displayed_container() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        WidgetContainer container = elementFactory.createWidgetContainer();

        AbstractParametrizedWidgetAssert.assertThat(container).isDisplayed();
    }

    @Test
    public void create_submit_button_with_submit_task_action_for_a_contract() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createSubmitButton(aSimpleContract(), ButtonAction.SUBMIT_TASK);

        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.SUBMIT_TASK.getValue());
    }

    @Test
    public void create_submit_button_with_start_process_action_for_a_contract() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createSubmitButton(aSimpleContract(), ButtonAction.START_PROCESS);

        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.START_PROCESS.getValue());
    }

    @Test
    public void create_submit_button_with_submit_label() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createSubmitButton(aSimpleContract(), ButtonAction.START_PROCESS);

        ButtonWidgetAssert.assertThat(button).hasButtonStyle(ButtonStyle.PRIMARY.getValue()).isNotDisabled();
        AbstractParametrizedWidgetAssert.assertThat(button)
                .hasLabel("Submit")
                .hasAlignment(Alignment.CENTER.getValue())
                .isDisplayed();
    }

    @Test
    public void text_contract_input_is_supported() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aStringContractInput("name"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void integer_contract_input_is_supported() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(anIntegerContractInput("age"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void date_contract_input_is_supported() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aDateContractInput("creationDate"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void file_contract_input_is_supported() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aFileContractInput("document"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void create_add_button_with_add_to_collection_action() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createAddButton();

        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.ADD_TO_COLLECTION.getValue());
    }

    @Test
    public void create_add_button_with_label() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createAddButton();

        ButtonWidgetAssert.assertThat(button).hasButtonStyle(ButtonStyle.SUCCESS.getValue()).isNotDisabled();
        AbstractParametrizedWidgetAssert.assertThat(button).hasLabel("Add").hasAlignment(Alignment.RIGHT.getValue()).isDisplayed();
    }

    @Test
    public void create_add_button_with_a_width_at_10() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createAddButton();

        assertThat(button.getDimension()).isEqualTo(10);
    }

    @Test
    public void create_remove_button_with_remove_from_collection_action() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.REMOVE_FROM_COLLECTION.getValue());
    }

    @Test
    public void create_remove_button_with_label() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        ButtonWidgetAssert.assertThat(button).hasButtonStyle(ButtonStyle.DANGER.getValue()).isNotDisabled();
        AbstractParametrizedWidgetAssert.assertThat(button).hasLabel("Remove").isDisplayed();
    }

    @Test
    public void create_remove_button_with_left_alignment() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        AbstractParametrizedWidgetAssert.assertThat(button).hasAlignment(Alignment.LEFT.getValue());
    }

    @Test
    public void create_remove_button_with_a_width_at_2() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        assertThat(button.getDimension()).isEqualTo(2);
    }

    @Test
    public void create_remove_button_which_remove_last_item() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        assertThat(button.getCollectionPosition()).isEqualTo("Last");
    }

    private ParametrizedWidgetFactory createFactory() {
        return new ParametrizedWidgetFactory();
    }

}
