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

import java.io.File;
import java.util.Date;

import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;

import com.google.common.base.CaseFormat;

public class ParametrizedWidgetFactory {

    public AbstractParametrizedWidget createParametrizedWidget(ContractInput input) {
        if (aTextInput(input)) {
            InputWidget inputWidget = createInputWidget(input);
            inputWidget.setType(InputType.TEXT);
            return inputWidget;
        }
        if (aNumericInput(input)) {
            InputWidget inputWidget = createInputWidget(input);
            inputWidget.setType(InputType.NUMBER);
            return inputWidget;
        }
        if (aDateInput(input)) {
            return createDatePicker(input);
        }
        if (aBooleanInput(input)) {
            return createCheckBox(input);
        }
        if (aFileInput(input)) {
            return createFileUploadWidget(input);
        }
        throw new IllegalArgumentException("Unsupported contract input type");
    }

    private FileUploadWidget createFileUploadWidget(ContractInput input) {
        FileUploadWidget fileUploadWidget = new FileUploadWidget();
        fileUploadWidget.setLabel(inputDisplayLabel(input));
        fileUploadWidget.setLabelPosition(LabelPosition.TOP);
        fileUploadWidget.setPlaceholder(input.getDescription());
        return fileUploadWidget;
    }

    public boolean isSupported(ContractInput input) {
        return aTextInput(input) || aNumericInput(input) || aDateInput(input) || aBooleanInput(input) || aFileInput(input);
    }

    // contract sent by studio contain things like that "java.lang.Boolean" for type
    private boolean aBooleanInput(ContractInput input) {
        return input.getType() != null && input.getType().toLowerCase().endsWith("boolean");
    }

    private boolean aFileInput(ContractInput input) {
        return input.getType() != null && input.getType().equals(File.class.getName());
    }

    private CheckboxWidget createCheckBox(ContractInput input) {
        CheckboxWidget checkbox = new CheckboxWidget();
        checkbox.setLabel(inputDisplayLabel(input));
        return checkbox;
    }

    protected DatePickerWidget createDatePicker(ContractInput input) {
        DatePickerWidget datePickerComponent = inputDefaultWidgetParameters(input, new DatePickerWidget());
        datePickerComponent.setDateFormat("MM/dd/yyyy");
        return datePickerComponent;
    }

    public WidgetContainer createWidgetContainer() {
        WidgetContainer container = new WidgetContainer();
        return container;
    }

    protected InputWidget createInputWidget(ContractInput input) {
        return inputDefaultWidgetParameters(input, new InputWidget());
    }

    private <T extends InputWidget> T inputDefaultWidgetParameters(ContractInput input, T inputComponent) {
        inputComponent.setLabel(inputDisplayLabel(input));
        inputComponent.setLabelPosition(LabelPosition.TOP);
        inputComponent.setPlaceholder(input.getDescription());
        return inputComponent;
    }

    private boolean aDateInput(ContractInput input) {
        return Date.class.getName().equals(input.getType());
    }

    private boolean aNumericInput(ContractInput input) {
        try {
            Class<?> clazz = Class.forName(input.getType());
            return Number.class.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean aTextInput(ContractInput input) {
        return String.class.getName().equals(input.getType());
    }

    private String inputDisplayLabel(ContractInput contractInput) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, contractInput.getName())
                .replaceAll("((?<=[a-z])(?=[A-Z]))|((?<=[A-Z])(?=[A-Z][a-z]))", " ");
    }

    public TitleWidget createTitle(String title) {
        TitleWidget titleComponent = new TitleWidget();
        titleComponent.setText(title);
        return titleComponent;
    }

    public TitleWidget createTitle(ContractInput input) {
        return createTitle(inputDisplayLabel(input));
    }

    public ButtonWidget createSubmitButton(Contract contract, ButtonAction actionType) {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("Submit");
        buttonComponent.setButtonStyle(ButtonStyle.PRIMARY);
        buttonComponent.setAlignment(Alignment.CENTER);
        buttonComponent.setAction(actionType);
        return buttonComponent;
    }

    public ButtonWidget createAddButton() {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("Add");
        buttonComponent.setButtonStyle(ButtonStyle.SUCCESS);
        buttonComponent.setAlignment(Alignment.RIGHT);
        buttonComponent.setAction(ButtonAction.ADD_TO_COLLECTION);
        buttonComponent.setDimension(10);
        return buttonComponent;
    }

    public ButtonWidget createRemoveButton() {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("Remove");
        buttonComponent.setButtonStyle(ButtonStyle.DANGER);
        buttonComponent.setAction(ButtonAction.REMOVE_FROM_COLLECTION);
        buttonComponent.setCollectionPosition("Last");
        buttonComponent.setDimension(2);
        return buttonComponent;
    }

}
