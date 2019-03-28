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

import static com.google.common.base.Joiner.on;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.experimental.mapping.data.FormInputVisitor;
import org.bonitasoft.web.designer.experimental.mapping.data.FormOutputData;
import org.bonitasoft.web.designer.experimental.mapping.data.SubmitErrorsListData;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.AbstractParametrizedWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Alignment;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.FileUploadWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.FileViewerWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Labeled;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterType;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParametrizedWidgetFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Requirable;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TextWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TitleWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Valuable;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class ContractInputToWidgetMapper {

    private static final Logger logger = LoggerFactory.getLogger(ParametrizedWidgetFactory.class);

    public static final String ITEM_ITERATOR = "$item";

    private ParametrizedWidgetFactory parametrizedWidgetFactory;
    private DimensionFactory dimensionFactory;
    private JacksonObjectMapper objectMapperWrapper;

    @Inject
    public ContractInputToWidgetMapper(DimensionFactory dimensionFactory, JacksonObjectMapper objectMapperWrapper) {
        this.dimensionFactory = dimensionFactory;
        this.objectMapperWrapper = objectMapperWrapper;
        parametrizedWidgetFactory = new ParametrizedWidgetFactory();
    }

    public Element toElement(ContractInput contractInput, List<List<Element>> rows) {
        return contractInput.isMultiple() ? toMultipleComponent(contractInput, rows) : toSimpleComponent(contractInput);
    }

    public Element toDocument(LeafContractInput contractInput) {
        return contractInput.isMultiple()
                ? toMultipleDocument(contractInput)
                : toSingleDocument(contractInput);
    }

    private Element toSingleDocument(LeafContractInput contractInput) {
        WidgetContainer documentContainer = parametrizedWidgetFactory.createWidgetContainer();
        Container container = documentContainer.toContainer(dimensionFactory);
        TitleWidget docuementNameTitle = parametrizedWidgetFactory.createTitle(contractInput);
        docuementNameTitle.setLevel("Level 4");
        if(contractInput.getMode() == EditMode.EDIT) {
            container.addNewRow(docuementNameTitle.toComponent(dimensionFactory));
        }
        
        if(contractInput.getMode() == EditMode.EDIT) {
            FileViewerWidget fileViewerWidget = new FileViewerWidget();
            fileViewerWidget.setShowPreview(false);
            fileViewerWidget.setDocument(String.format("context.%s_ref", contractInput.getDataReference().getName()));
            container.addNewRow(fileViewerWidget.toComponent(dimensionFactory));
        }
        
        FileUploadWidget fileUploadWidget = (FileUploadWidget) parametrizedWidgetFactory
                .createParametrizedWidget(contractInput);
        String value = contractInput.getMode() == EditMode.EDIT ?
                String.format("context.%s_ref.newValue", contractInput.getDataReference().getName())
                : isParentMultiple(contractInput) ? multipleInputValue(contractInput)
                            : new ContractInputDataHandler(contractInput).inputValue();
        fileUploadWidget.setValue(value);
        fileUploadWidget.setLabelHidden(contractInput.getMode() == EditMode.EDIT);
        fileUploadWidget.setLabelWidth(4);
        fileUploadWidget.setLabel(docuementNameTitle.getText());
        fileUploadWidget.setPlaceholder(contractInput.getMode() == EditMode.EDIT ? "Browse to update the file..." : "Browse to upload a new file..." );
        fileUploadWidget.setRequired(contractInput.isMandatory());
        container.addNewRow(fileUploadWidget.toComponent(dimensionFactory));
        
        return container;
    }

    private Element toMultipleDocument(LeafContractInput contractInput) {
        WidgetContainer rootWidgetContainer = parametrizedWidgetFactory.createWidgetContainer();
        Container rootContainer = rootWidgetContainer.toContainer(dimensionFactory);
        TitleWidget documentNameTitle = parametrizedWidgetFactory.createTitle(contractInput);
        documentNameTitle.setLevel("Level 4");
        rootContainer.addNewRow(documentNameTitle.toComponent(dimensionFactory));

        Container container = toMultipleContainer(contractInput);

        if(contractInput.getMode() == EditMode.EDIT) {
            FileViewerWidget fileViewerWidget = new FileViewerWidget();
            fileViewerWidget.setShowPreview(false);
            fileViewerWidget.setDocument("$item");
            container.addNewRow(fileViewerWidget.toComponent(dimensionFactory));
        }

        FileUploadWidget fileUploadWidget = (FileUploadWidget) parametrizedWidgetFactory
                .createParametrizedWidget(contractInput);
        fileUploadWidget.setValue(contractInput.getMode() == EditMode.EDIT ? ITEM_ITERATOR + ".newValue" : ITEM_ITERATOR);
        fileUploadWidget.setLabelHidden(true);
        fileUploadWidget.setLabelWidth(4);
        fileUploadWidget.setRequired(false);
        fileUploadWidget.setPlaceholder(contractInput.getMode() == EditMode.EDIT ? "Browse to update the file..." : "Browse to upload a new file..." );
        fileUploadWidget.setDimension(11);

        List<Element> row = new ArrayList<>();
        row.add(fileUploadWidget.toComponent(dimensionFactory));

        ButtonWidget removeButton = parametrizedWidgetFactory.createRemoveButton();
        removeButton.setDimension(1);
        removeButton.setAlignment(Alignment.LEFT);

        row.add(removeButton.toComponent(dimensionFactory));
        container.getRows().add(row);
        rootContainer.addNewRow(container);
        return rootContainer;
    }

    private Container toMultipleComponent(ContractInput contractInput, List<List<Element>> rows) {
        rows.add(Collections.<Element> singletonList(
                parametrizedWidgetFactory.createTitle(contractInput).toComponent(dimensionFactory)));
        return toMultipleComponent(contractInput);
    }

    private Container toMultipleComponent(ContractInput contractInput) {
        Container container = toMultipleContainer(contractInput);
        AbstractParametrizedWidget component = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (component instanceof Labeled) {
            component.setLabel("");
            ((Labeled) component).setLabelWidth(0);
        }
        if (component instanceof Valuable) {
            ((Valuable) component).setValue(ITEM_ITERATOR);
        }
        if (component instanceof Requirable) {
            ((Requirable) component).setRequired(false);
        }
        List<Element> row = new ArrayList<>();
        row.add(component.toComponent(dimensionFactory));
        row.add(createRemoveButton());
        container.getRows().add(row);
        return container;
    }

    private String multipleInputValue(ContractInput contractInput) {
        return contractInput.getParent() != null
                && !(Objects.equals(ContractInputDataHandler.PERSISTENCEID_INPUT_NAME, contractInput.getName())
                        && ContractInputDataHandler.hasAggregatedParentRef(contractInput))
                                ? on(".").join(ITEM_ITERATOR, contractInput.getName()) : ITEM_ITERATOR;
    }

    private Container toMultipleContainer(ContractInput contractInput) {
        WidgetContainer multipleContainer = parametrizedWidgetFactory.createWidgetContainer();
        multipleContainer.setRepeatedCollection(
                isParentMultiple(contractInput) ? multipleInputValue(contractInput) : singleInputValue(contractInput));
        return multipleContainer.toContainer(dimensionFactory);
    }

    private String singleInputValue(ContractInput contractInput) {
        ContractInputDataHandler contractInputDataHandler = new ContractInputDataHandler(contractInput);
        return contractInputDataHandler.isDocumentEdition()
                ? String.format("context.%s_ref", contractInputDataHandler.getRefName())
                : contractInputDataHandler.inputValue();
    }

    private boolean isParentMultiple(ContractInput contractInput) {
        return contractInput.getParent() != null && contractInput.getParent().isMultiple();
    }

    private Container toSimpleContainer(NodeContractInput nodeContractInput) {
        return parametrizedWidgetFactory.createWidgetContainer().toContainer(dimensionFactory);
    }

    private Component toSimpleComponent(ContractInput contractInput) {
        AbstractParametrizedWidget widget = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (widget instanceof Valuable) {
            ((Valuable) widget).setValue(isParentMultiple(contractInput) ? multipleInputValue(contractInput)
                    : new ContractInputDataHandler(contractInput).inputValue());
        }
        return widget.toComponent(dimensionFactory);
    }

    public Container toContainer(NodeContractInput nodeContractInput, List<List<Element>> rows) {
        rows.add(Collections.<Element> singletonList(
                parametrizedWidgetFactory.createTitle(nodeContractInput).toComponent(dimensionFactory)));
        return nodeContractInput.isMultiple() ? toMultipleContainer(nodeContractInput)
                : toSimpleContainer(nodeContractInput);
    }

    public Component createSubmitButton(Contract contract, ButtonAction actionType) {
        ButtonWidget submitButton = parametrizedWidgetFactory.createSubmitButton(contract, actionType);
        submitButton.setDataToSend(FormOutputData.NAME);
        submitButton.setPropertyValue("dataFromError", ParameterType.VARIABLE, SubmitErrorsListData.SUBMIT_ERROR_DATA);
        submitButton.setTargetUrlOnSuccess("/bonita");
        submitButton.setPropertyValue("disabled", ParameterType.EXPRESSION, "$form.$invalid");
        return submitButton.toComponent(dimensionFactory);
    }

    public Component createSubmitErrorAlert() {
        TextWidget widget = new TextWidget();
        widget.setCssClasses("alert alert-danger col-lg-6 col-lg-offset-3");
        widget.setPropertyValue("hidden", ParameterType.EXPRESSION,
                String.format("!%s.message", SubmitErrorsListData.SUBMIT_ERROR_DATA));
        widget.setPropertyValue("allowHTML", ParameterType.CONSTANT, true);
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("<strong>{{%s.message}}</strong>", SubmitErrorsListData.SUBMIT_ERROR_DATA));
        sb.append("\n");
        sb.append(String.format("{{%s}}", SubmitErrorsListData.NAME));
        widget.setPropertyValue("text", ParameterType.INTERPOLATION, sb.toString());
        return widget.toComponent(dimensionFactory);
    }

    public boolean canCreateComponent(ContractInput contractInput) {
        return parametrizedWidgetFactory.isSupported(contractInput);
    }

    public Component createRemoveButton() {
        ButtonWidget removeButton = parametrizedWidgetFactory.createRemoveButton();
        return removeButton.toComponent(dimensionFactory);
    }

    public Component createAddButton(ContractInput contractInput) {
        ContractInputDataHandler dataHandler = new ContractInputDataHandler(contractInput);
        ButtonWidget addButton = parametrizedWidgetFactory
                .createAddButton(dataHandler.isDocument() ? "File" : dataHandler.getRefType() != null ? toSimpleName(dataHandler.getRefType()) : null);

        addButton.setCollectionToModify(
                isParentMultiple(contractInput) ? multipleInputValue(contractInput) : dataHandler.isDocumentEdition()
                        ? String.format("context.%s_ref", dataHandler.getRefName()) : dataHandler.inputValue());
        if (contractHasInput(contractInput)) {
            addButton.setValueToAdd(getValueToAddFromContract(contractInput));
        }
        return addButton.toComponent(dimensionFactory);
    }

    private String toSimpleName(String refType) {
        String[] parts = refType.split("\\.");
        return parts[parts.length - 1];
    }

    private boolean contractHasInput(ContractInput contractInput) {
        return contractInput instanceof NodeContractInput
                && contractInput.getInput() != null;
    }

    private String getValueToAddFromContract(ContractInput contractInput) {
        try {
            FormInputVisitor visitor = new FormInputVisitor(objectMapperWrapper);
            for (ContractInput input : contractInput.getInput()) {
                input.accept(visitor);
            }
            return visitor.toJson();
        } catch (IOException e) {
            logger.warn("Impossible to set valueToAdd from ContractInput", e);
        }
        return null;
    }


}
