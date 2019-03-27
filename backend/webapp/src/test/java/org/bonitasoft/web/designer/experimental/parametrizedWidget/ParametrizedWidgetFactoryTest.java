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
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aFileContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLongContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aOffsetDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.anIntegerContractInput;

import org.bonitasoft.web.designer.experimental.assertions.AbstractParametrizedWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.ButtonWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.DatePickerWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.DateTimePickerWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.InputWidgetAssert;
import org.bonitasoft.web.designer.experimental.assertions.TitleWidgetAssert;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.PropertyValue;
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
    public void create_a_datepicker_for_localdate_contract_input_with_description_keeping_default_placeholder()
            throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        LeafContractInput contractInput = aLocalDateContractInput("name");
        contractInput.setDescription("My description");

        InputWidget component = (InputWidget) elementFactory.createParametrizedWidget(contractInput);

        InputWidgetAssert.assertThat(component).hasPlaceholder("Enter a date (mm/dd/yyyy)");
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

    @Deprecated
    @Test
    public void create_a_datepicker_for_date_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aDateContractInput("creationDate"));

        assertThat(component).isInstanceOf(DatePickerWidget.class);
    }

    @Test
    public void create_a_datepicker_for_local_date_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory
                .createParametrizedWidget(aLocalDateContractInput("creationLocalDate"));

        assertThat(component).isInstanceOf(DatePickerWidget.class);
    }

    @Test
    public void create_a_datetimepicker_for_local_date_time_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory
                .createParametrizedWidget(aLocalDateTimeContractInput("creationLocalDateTime"));

        assertThat(component).isInstanceOf(DateTimePickerWidget.class);
    }

    @Test
    public void create_a_checkbox_for_boolean_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory
                .createParametrizedWidget(new LeafContractInput("isValidated", Boolean.class));

        assertThat(component).isInstanceOf(CheckboxWidget.class);
        assertThat(component.getLabel()).isEqualTo("Is Validated");
    }

    @Test
    public void create_a_file_upload_for_file_contract_input_with_a_default_url() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        AbstractParametrizedWidget component = elementFactory.createParametrizedWidget(aFileContractInput("document"));

        assertThat(component).isInstanceOf(FileUploadWidget.class);
        assertThat(component.getLabel()).isEqualTo("Document");
        assertThat(((FileUploadWidget) component).getUrl()).isEqualTo("../API/formFileUpload");
    }

    /**
     * @Deprecated
     */
    @Test
    public void should_create_a_datepicker_for_date_contract_input_with_date_format() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        DatePickerWidget component = (DatePickerWidget) elementFactory
                .createParametrizedWidget(aDateContractInput("creationDate"));

        DatePickerWidgetAssert.assertThat(component).hasPlaceholder();
        DatePickerWidgetAssert.assertThat(component).hasDateFormat("MM/dd/yyyy");
    }

    @Test
    public void should_create_a_datepicker_for_date_contract_input_with_local_date_format() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        DatePickerWidget component = (DatePickerWidget) elementFactory
                .createParametrizedWidget(aLocalDateContractInput("creationLocalDate"));

        DatePickerWidgetAssert.assertThat(component).hasPlaceholder();
        DatePickerWidgetAssert.assertThat(component).hasDateFormat("MM/dd/yyyy");
    }

    @Test
    public void should_create_a_datetimepicker_for_date_contract_input_with_local_date_time_format() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        DateTimePickerWidget component = (DateTimePickerWidget) elementFactory
                .createParametrizedWidget(aLocalDateTimeContractInput("creationLocalDateTime"));

        DateTimePickerWidgetAssert.assertThat(component).hasDateFormat("MM/dd/yyyy");
        DateTimePickerWidgetAssert.assertThat(component).hasTimeFormat("h:mm:ss a");
        DateTimePickerWidgetAssert.assertThat(component).hasDatePlaceholder();
        DateTimePickerWidgetAssert.assertThat(component).hasTimePlaceholder();
        assertThat(component.isWithTimeZone()).isEqualTo(false);
    }

    @Test
    public void should_create_a_datetimepicker_for_date_contract_input_with_off_date_time_format() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        DateTimePickerWidget component = (DateTimePickerWidget) elementFactory
                .createParametrizedWidget(aOffsetDateTimeContractInput("creationOffSetDateTime"));
        assertThat(component.isWithTimeZone()).isEqualTo(true);
        DateTimePickerWidgetAssert.assertThat(component).hasDateFormat("MM/dd/yyyy");
        DateTimePickerWidgetAssert.assertThat(component).hasTimeFormat("h:mm:ss a");
        DateTimePickerWidgetAssert.assertThat(component).hasDatePlaceholder();
        DateTimePickerWidgetAssert.assertThat(component).hasTimePlaceholder();
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
    public void should_be_supported_date_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aLocalDateContractInput("creationDate"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void should_be_supported_local_date_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aLocalDateContractInput("creationLocalDate"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void should_be_supported_local_date_time_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aLocalDateTimeContractInput("creationLocalDateTime"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void should_be_supported_local_offset_date_time_contract_input() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aLocalDateTimeContractInput("creationOffsetDateTime"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void file_contract_input_is_supported() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        boolean isSupported = elementFactory.isSupported(aFileContractInput("document"));

        assertThat(isSupported).isTrue();
    }

    @Test
    public void create_add_button() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createAddButton(null);

        assertThat(button.getDimension()).isEqualTo(12);
        ButtonWidgetAssert.assertThat(button).hasButtonStyle(ButtonStyle.PRIMARY.getValue()).isNotDisabled();
        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.ADD_TO_COLLECTION.getValue());

        AbstractParametrizedWidgetAssert.assertThat(button)
        .hasLabel( "<span class=\"glyphicon glyphicon-plus\"></span>")
        .hasAlignment(Alignment.LEFT.getValue())
                .isDisplayed();
    }

    @Test
    public void create_remove_button() throws Exception {
        ParametrizedWidgetFactory elementFactory = createFactory();

        ButtonWidget button = elementFactory.createRemoveButton();

        assertThat(button.getDimension()).isEqualTo(12);
        assertThat(button.getCollectionPosition()).isEqualTo("Item");
        assertThat(button.getRemoveItem()).isEqualTo("$item");
        assertThat(button.getCollectionToModify()).isEqualTo("$collection");

        AbstractParametrizedWidgetAssert.assertThat(button).hasAlignment(Alignment.RIGHT.getValue());
        ButtonWidgetAssert.assertThat(button).hasButtonStyle(ButtonStyle.DANGER.getValue()).isNotDisabled();
        AbstractParametrizedWidgetAssert.assertThat(button)
        .hasLabel( "<span class=\"glyphicon glyphicon-remove\"></span>")
        .isDisplayed();
        ButtonWidgetAssert.assertThat(button).hasAction(ButtonAction.REMOVE_FROM_COLLECTION.getValue());

        PropertyValue PropertyValue = button.toPropertyValues().get("removeItem");
        assertThat(PropertyValue.getType()).isEqualTo("variable");
        assertThat(PropertyValue.getValue()).isEqualTo("$item");
    }

    @Test
    public void should_create_link_widget() {
        ParametrizedWidgetFactory elementFactory = createFactory();
        String linkText = "linkText";
        String linkUrl = "linkUrk";
        ButtonStyle linkStyle = ButtonStyle.LINK;

        LinkWidget link = elementFactory.createLink(linkText, linkUrl, linkStyle);

        assertThat(link.getText()).isEqualTo(linkText);
        assertThat(link.getTargetUrl()).isEqualTo(linkUrl);
        assertThat(link.getButtonStyle()).isEqualTo(linkStyle.getValue());
        assertThat(link.getDimension()).isEqualTo(12);
        AbstractParametrizedWidgetAssert.assertThat(link).hasAlignment(Alignment.LEFT.getValue());
    }

    private ParametrizedWidgetFactory createFactory() {
        return new ParametrizedWidgetFactory();
    }

}
