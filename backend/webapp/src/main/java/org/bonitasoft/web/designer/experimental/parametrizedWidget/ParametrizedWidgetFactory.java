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

import static com.google.common.base.Joiner.on;

import java.util.Objects;

import org.bonitasoft.web.designer.experimental.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessQueryData;
import org.bonitasoft.web.designer.experimental.widgets.PbDatePicker;
import org.bonitasoft.web.designer.experimental.widgets.PbDateTimePicker;
import org.bonitasoft.web.designer.experimental.widgets.PbInput;
import org.bonitasoft.web.designer.experimental.widgets.PbUpload;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import com.google.common.base.CaseFormat;

public class ParametrizedWidgetFactory {

    public static final String ITEM_ITERATOR = "$item";

    private ContractInputTypeResolver contractInputTypeResolver;

    public ParametrizedWidgetFactory() {
        contractInputTypeResolver = new ContractInputTypeResolver();
    }

    public AbstractParametrizedWidget createParametrizedWidget(ContractInput input) {
        switch (contractInputTypeResolver.getContractInputType(input)) {
            case TEXT:
                return createWidgetForTextInput(input);
            case NUMERIC:
                return input.isReadOnly()
                        ? createTextWidget(input)
                        : createInputWidget(input, InputType.NUMBER);
            case LOCAL_DATE:
                return input.isReadOnly()
                        ? createTextWidget(input)
                        : createDatePicker(input);
            case LOCAL_DATE_TIME:
                return input.isReadOnly()
                        ? createTextWidget(input)
                        : createDateTimePicker(input, false);
            case OFFSET_DATE_TIME:
                return input.isReadOnly()
                        ? createTextWidget(input)
                        : createDateTimePicker(input, true);
            case BOOLEAN:
                return createCheckBox(input);
            case FILE:
                return createFileUploadWidget(input);
            default:
                throw new IllegalStateException(
                        String.format("Unable to create a widget for contract input %s.", input.getName()));
        }
    }

    private AbstractParametrizedWidget createWidgetForTextInput(ContractInput input) {
        if (Objects.equals(ContractInputDataHandler.PERSISTENCEID_INPUT_NAME, input.getName())
                && ContractInputDataHandler.hasAggregatedParentRef(input)) {
            return input.isReadOnly()
                    ? createTextWidget(input.getParent())
                    : createSelectWidget((NodeContractInput) input.getParent());
        } else if (input.isReadOnly()) {
            return createTextWidget(input);
        } else {
            InputWidget inputWidget = createInputWidget(input);
            inputWidget.setType(InputType.TEXT);
            return inputWidget;
        }
    }

    private TextWidget createTextWidget(ContractInput input) {
        TextWidget textWidget = new TextWidget();
        textWidget.setLabel(inputDisplayLabel(input));
        textWidget.setLabelHidden(false);
        addTextValue(input, textWidget);
        return textWidget;
    }

    private FileUploadWidget createFileUploadWidget(ContractInput input) {
        FileUploadWidget fileUploadWidget = new FileUploadWidget();
        fileUploadWidget.setLabel(inputDisplayLabel(input));
        fileUploadWidget.setLabelPosition(LabelPosition.TOP);
        fileUploadWidget.setPlaceholder(new PbUpload().getPlaceholder());

        String value;
        if (input.isMultiple()) {
            value = input.getMode() == EditMode.EDIT
                    ? ITEM_ITERATOR + ".newValue"
                    : ITEM_ITERATOR;
        } else {
            value = input.getMode() == EditMode.EDIT
                    ? String.format("context.%s_ref.newValue", ((LeafContractInput) input).getDataReference().getName())
                    : isParentMultiple(input)
                            ? multipleInputValue(input)
                            : new ContractInputDataHandler(input).inputValue();
        }
        fileUploadWidget.setValue(value);
        fileUploadWidget.setLabelHidden(input.isMultiple() || input.getMode() == EditMode.EDIT);
        fileUploadWidget.setLabelWidth(4);
        fileUploadWidget.setPlaceholder(input.getMode() == EditMode.EDIT
                ? "Browse to update the file..."
                : "Browse to upload a new file...");
        fileUploadWidget.setRequired(input.isMandatory());
        return fileUploadWidget;
    }

    public boolean isSupported(ContractInput input) {
        return contractInputTypeResolver.isSupported(input);
    }

    private CheckboxWidget createCheckBox(ContractInput input) {
        CheckboxWidget checkbox = new CheckboxWidget();
        checkbox.setLabel(inputDisplayLabel(input));
        checkbox.setReadOnly(input.isReadOnly());
        setValuableWidgetValue(input, checkbox);
        return checkbox;
    }

    protected DatePickerWidget createDatePicker(ContractInput input) {
        PbDatePicker reference = new PbDatePicker();
        DatePickerWidget datePickerComponent = inputDefaultWidgetParameters(input, new DatePickerWidget());

        datePickerComponent.setDateFormat(reference.getDateFormat());
        datePickerComponent.setPlaceholder(reference.getPlaceholder());
        datePickerComponent.setShowToday(reference.getShowToday());
        datePickerComponent.setTodayLabel(reference.getTodayLabel());
        setValuableWidgetValue(input, datePickerComponent);

        return datePickerComponent;
    }

    protected DateTimePickerWidget createDateTimePicker(ContractInput input, boolean withTimeZone) {
        PbDateTimePicker reference = new PbDateTimePicker();
        DateTimePickerWidget dateTimePickerComponent = inputDefaultWidgetParameters(input, new DateTimePickerWidget());

        dateTimePickerComponent.setPlaceholder(reference.getPlaceholder());
        dateTimePickerComponent.setTimePlaceholder(reference.getTimePlaceholder());
        dateTimePickerComponent.setDateFormat(reference.getDateFormat());
        dateTimePickerComponent.setTimeFormat(reference.getTimeFormat());
        dateTimePickerComponent.setWithTimeZone(withTimeZone);
        dateTimePickerComponent.setTodayLabel(reference.getTodayLabel());
        dateTimePickerComponent.setNowLabel(reference.getNowLabel());
        dateTimePickerComponent.setShowNow(reference.getShowNow());
        dateTimePickerComponent.setShowToday(reference.getShowToday());
        dateTimePickerComponent.setInlineInput(reference.getInlineInput());
        setValuableWidgetValue(input, dateTimePickerComponent);

        return dateTimePickerComponent;
    }

    public WidgetContainer createWidgetContainer(ContractInput contractInput) {
        WidgetContainer container = new WidgetContainer();
        if (contractInput.isMultiple()) {
            container.setRepeatedCollection(isParentMultiple(contractInput)
                    ? multipleInputValue(contractInput)
                    : singleInputValue(contractInput));
        }
        return container;
    }

    public WidgetContainer createWidgetContainer() {
        return new WidgetContainer();
    }

    protected InputWidget createInputWidget(ContractInput input) {
        InputWidget inputWidget = inputDefaultWidgetParameters(input, new InputWidget());
        inputWidget.setRequired(input.isMandatory());
        inputWidget.setPlaceholder(new PbInput().getPlaceholder());
        setValuableWidgetValue(input, inputWidget);
        return inputWidget;
    }

    private void setValuableWidgetValue(ContractInput input, AbstractParametrizedWidget widget) {
        if (widget instanceof Valuable) {
            String value = getValue(input);
            ((Valuable) widget).setValue(value);
            if (input.isReadOnly()) {
                widget.setHidden(String.format("!%s", value));
            }
        }
    }

    protected InputWidget createInputWidget(ContractInput input, InputType type) {
        InputWidget inputWidget = createInputWidget(input);
        inputWidget.setType(type);
        return inputWidget;
    }

    private <T extends InputWidget> T inputDefaultWidgetParameters(ContractInput input, T inputComponent) {
        inputComponent.setLabel(inputDisplayLabel(input));
        inputComponent.setLabelPosition(LabelPosition.TOP);
        inputComponent.setReadOnly(input.isReadOnly());
        return inputComponent;
    }

    private String inputDisplayLabel(ContractInput contractInput) {
        ContractInputDataHandler dataHandler = new ContractInputDataHandler(contractInput);
        return CaseFormat.LOWER_CAMEL
                .to(CaseFormat.UPPER_CAMEL,
                        dataHandler.getRefName() != null ? dataHandler.getRefName() : dataHandler.getInputName())
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

    public ButtonWidget createSubmitButton(ButtonAction actionType) {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("Submit");
        buttonComponent.setButtonStyle(ButtonStyle.PRIMARY);
        buttonComponent.setAlignment(Alignment.CENTER);
        buttonComponent.setAction(actionType);
        return buttonComponent;
    }

    public ButtonWidget createAddButton(ContractInput contractInput) {
        ContractInputDataHandler dataHandler = new ContractInputDataHandler(contractInput);
        String buttonText = dataHandler.isDocument()
                ? "File"
                : dataHandler.getRefType() != null
                        ? toSimpleName(dataHandler.getRefType())
                        : null;
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel(buttonText == null || buttonText.isEmpty()
                ? "<span class=\"glyphicon glyphicon-plus\"></span>"
                : "<span class=\"glyphicon glyphicon-plus\"></span> Add " + buttonText);
        buttonComponent.setButtonStyle(ButtonStyle.PRIMARY);
        buttonComponent.setAlignment(Alignment.LEFT);
        buttonComponent.setAction(ButtonAction.ADD_TO_COLLECTION);
        buttonComponent.setDimension(12);
        String collectionToModify = isParentMultiple(contractInput)
                ? multipleInputValue(contractInput)
                : dataHandler.isDocumentEdition()
                        ? String.format("context.%s_ref", dataHandler.getRefName())
                        : dataHandler.inputValue();
        buttonComponent.setCollectionToModify(collectionToModify);
        return buttonComponent;
    }

    private String toSimpleName(String refType) {
        String[] parts = refType.split("\\.");
        return parts[parts.length - 1];
    }

    public ButtonWidget createRemoveButton() {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("<span class=\"glyphicon glyphicon-remove\"></span>");
        buttonComponent.setButtonStyle(ButtonStyle.DANGER);
        buttonComponent.setAction(ButtonAction.REMOVE_FROM_COLLECTION);
        buttonComponent.setCollectionPosition("Item");
        buttonComponent.setAlignment(Alignment.RIGHT);
        buttonComponent.setRemoveItem(ITEM_ITERATOR);
        buttonComponent.setCollectionToModify("$collection");
        buttonComponent.setDimension(12);
        return buttonComponent;
    }

    public LinkWidget createLink(String text, String url, ButtonStyle style) {
        LinkWidget linkComponent = new LinkWidget();
        linkComponent.setText(text);
        linkComponent.setTargetUrl(url);
        linkComponent.setButtonStyle(style);
        return linkComponent;
    }

    protected SelectWidget createSelectWidget(NodeContractInput input) {
        SelectWidget selectWidget = new SelectWidget();
        String label = inputDisplayLabel(input);
        selectWidget.setLabel(label);
        selectWidget.setLabelPosition(LabelPosition.TOP);
        selectWidget.setPlaceholder(String.format("Select a %s", label));
        selectWidget.setRequired(input.isMandatory());
        selectWidget.setAvailableValues(toBusinessQueryDataName(input.getDataReference()));
        if (input.isMultiple()) {
            selectWidget.setValue(ITEM_ITERATOR);
        } else {
            setValuableWidgetValue(input, selectWidget);
        }
        return selectWidget;
    }

    private String toBusinessQueryDataName(BusinessDataReference dataReference) {
        return new BusinessQueryData(dataReference).name();
    }

    private String getValue(ContractInput contractInput) {
        return isParentMultiple(contractInput)
                ? multipleInputValue(contractInput)
                : new ContractInputDataHandler(contractInput).inputValue();
    }

    private boolean isParentMultiple(ContractInput contractInput) {
        return contractInput.getParent() != null && contractInput.getParent().isMultiple();
    }

    private String multipleInputValue(ContractInput contractInput) {
        return contractInput.getParent() != null
                && !(Objects.equals(ContractInputDataHandler.PERSISTENCEID_INPUT_NAME, contractInput.getName())
                        && ContractInputDataHandler.hasAggregatedParentRef(contractInput))
                                ? on(".").join(ITEM_ITERATOR, contractInput.getName()) : ITEM_ITERATOR;
    }

    private String singleInputValue(ContractInput contractInput) {
        ContractInputDataHandler contractInputDataHandler = new ContractInputDataHandler(contractInput);
        return contractInputDataHandler.isDocumentEdition()
                ? String.format("context.%s_ref", contractInputDataHandler.getRefName())
                : contractInputDataHandler.inputValue();
    }

    private void addTextValue(ContractInput contractInput, TextWidget widget) {
        String value = getValue(contractInput);
        widget.setHidden(String.format("!%s", value));
        if (contractInputTypeResolver.isDateInput(contractInput)) {
            value = String.format("%s|uiDate", value);
        }
        widget.setText(String.format("{{%s}}", value));
    }

}
