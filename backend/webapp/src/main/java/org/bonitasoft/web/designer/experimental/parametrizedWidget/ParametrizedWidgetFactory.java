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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.bonitasoft.web.designer.experimental.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.experimental.mapping.ContractInputToWidgetMapper;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessQueryData;
import org.bonitasoft.web.designer.experimental.widgets.PbDatePicker;
import org.bonitasoft.web.designer.experimental.widgets.PbDateTimePicker;
import org.bonitasoft.web.designer.experimental.widgets.PbInput;
import org.bonitasoft.web.designer.experimental.widgets.PbUpload;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import com.google.common.base.CaseFormat;

public class ParametrizedWidgetFactory {

    public AbstractParametrizedWidget createParametrizedWidget(ContractInput input) {
        if (aTextInput(input)) {
            if(Objects.equals(ContractInputDataHandler.PERSISTENCEID_INPUT_NAME, input.getName()) 
                    && ContractInputDataHandler.hasAggregatedParentRef(input)) {
                return createSelectWidget((NodeContractInput) input.getParent());
            }else {
                InputWidget inputWidget = createInputWidget(input);
                inputWidget.setType(InputType.TEXT);
                return inputWidget;
            }
        }
        if (aNumericInput(input)) {
            InputWidget inputWidget = createInputWidget(input);
            inputWidget.setType(InputType.NUMBER);
            return inputWidget;
        }
        if (aLocalDateInput(input) || aDateInput(input)) {
            return createDatePicker(input);
        }
        if (aLocalDateTimeInput(input)) {
            return createDateTimePicker(input, false);
        }
        if (aOffsetDateTimeInput(input)) {
            return createDateTimePicker(input, true);
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
        fileUploadWidget.setPlaceholder(new PbUpload().getPlaceholder());
        return fileUploadWidget;
    }

    public boolean isSupported(ContractInput input) {
        return !ContractInputDataHandler.shouldGenerateWidgetForInput(input)
                && ( aTextInput(input) 
                || aNumericInput(input) 
                || aDateInput(input) 
                || aLocalDateInput(input)
                || aLocalDateTimeInput(input) 
                || aOffsetDateTimeInput(input) 
                || aBooleanInput(input)
                || aFileInput(input));
    }

    // contract sent by studio contain things like that "java.lang.Boolean" for type
    private boolean aBooleanInput(ContractInput input) {
        return input.getType() != null && input.getType().toLowerCase(Locale.ENGLISH).endsWith("boolean");
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
        PbDatePicker reference = new PbDatePicker();
        DatePickerWidget datePickerComponent = inputDefaultWidgetParameters(input, new DatePickerWidget());

        datePickerComponent.setReadonly(reference.getReadOnly());
        datePickerComponent.setDateFormat(reference.getDateFormat());
        datePickerComponent.setPlaceholder(reference.getPlaceholder());
        datePickerComponent.setShowToday(reference.getShowToday());
        datePickerComponent.setTodayLabel(reference.getTodayLabel());

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
        dateTimePickerComponent.setReadonly(reference.getReadOnly());
        dateTimePickerComponent.setTodayLabel(reference.getTodayLabel());
        dateTimePickerComponent.setNowLabel(reference.getNowLabel());
        dateTimePickerComponent.setShowNow(reference.getShowNow());
        dateTimePickerComponent.setShowToday(reference.getShowToday());
        dateTimePickerComponent.setInlineInput(reference.getInlineInput());

        return dateTimePickerComponent;
    }

    public WidgetContainer createWidgetContainer() {
        return new WidgetContainer();
    }

    public WidgetContainer createWidgetContainer(String collection) {
        WidgetContainer container = createWidgetContainer();
        container.setRepeatedCollection(collection);
        return container;
    }

    protected InputWidget createInputWidget(ContractInput input) {
        InputWidget inputWidget = inputDefaultWidgetParameters(input, new InputWidget());
        inputWidget.setPlaceholder(new PbInput().getPlaceholder());
        return inputWidget;
    }

    private <T extends InputWidget> T inputDefaultWidgetParameters(ContractInput input, T inputComponent) {
        inputComponent.setLabel(inputDisplayLabel(input));
        inputComponent.setLabelPosition(LabelPosition.TOP);
        return inputComponent;
    }

    /**
     * @deprecated Type Date is deprecated in studio, prefer use type LocalDate.
     */
    @Deprecated
    private boolean aDateInput(ContractInput input) {
        return Date.class.getName().equals(input.getType());
    }

    private boolean aLocalDateInput(ContractInput input) {
        return LocalDate.class.getName().equals(input.getType());
    }

    private boolean aLocalDateTimeInput(ContractInput input) {
        return LocalDateTime.class.getName().equals(input.getType());
    }

    private boolean aOffsetDateTimeInput(ContractInput input) {
        return OffsetDateTime.class.getName().equals(input.getType());
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
        buttonComponent.setDimension(12);
        return buttonComponent;
    }

    public ButtonWidget createRemoveButton() {
        ButtonWidget buttonComponent = new ButtonWidget();
        buttonComponent.setLabel("Remove");
        buttonComponent.setButtonStyle(ButtonStyle.DANGER);
        buttonComponent.setAction(ButtonAction.REMOVE_FROM_COLLECTION);
        buttonComponent.setCollectionPosition("Item");
        buttonComponent.setAlignment(Alignment.RIGHT);
        buttonComponent.setRemoveItem(ContractInputToWidgetMapper.ITEM_ITERATOR);
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
        selectWidget.setPlaceholder(String.format("Select a %s",label));
        selectWidget.setAvailableValues(toBusinessQueryDataName(input.getDataReference()));
        return selectWidget;
    }

    private String toBusinessQueryDataName(BusinessDataReference dataReference) {
        return new BusinessQueryData(dataReference).name();
    }

}
