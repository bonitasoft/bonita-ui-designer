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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.generator.mapping.data.FormInputVisitor;
import org.bonitasoft.web.designer.generator.mapping.data.FormOutputData;
import org.bonitasoft.web.designer.generator.mapping.data.SubmitErrorsListData;
import org.bonitasoft.web.designer.generator.parametrizedWidget.AbstractParametrizedWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.Alignment;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ButtonWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.FileUploadWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.FileViewerWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.Labeled;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterType;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParametrizedWidgetFactory;
import org.bonitasoft.web.designer.generator.parametrizedWidget.TextWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.TitleWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.Valuable;
import org.bonitasoft.web.designer.generator.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
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

    protected ParametrizedWidgetFactory parametrizedWidgetFactory;
    protected DimensionFactory dimensionFactory;
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
        WidgetContainer documentContainer = parametrizedWidgetFactory.createWidgetContainer(contractInput);
        Container container = documentContainer.toContainer(dimensionFactory);
        TitleWidget docuementNameTitle = parametrizedWidgetFactory.createTitle(contractInput);
        docuementNameTitle.setLevel("Level 4");

        if (contractInput.getMode() == EditMode.EDIT) {
            container.addNewRow(docuementNameTitle.toComponent(dimensionFactory));
            FileViewerWidget fileViewerWidget = new FileViewerWidget();
            fileViewerWidget.setShowPreview(false);
            fileViewerWidget.setDocument(String.format("context.%s_ref", contractInput.getDataReference().getName()));
            container.addNewRow(fileViewerWidget.toComponent(dimensionFactory));
        }
        FileUploadWidget fileUploadWidget = (FileUploadWidget) parametrizedWidgetFactory
                .createParametrizedWidget(contractInput);
        fileUploadWidget.setLabel(docuementNameTitle.getText());
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

        if (contractInput.getMode() == EditMode.EDIT) {
            FileViewerWidget fileViewerWidget = new FileViewerWidget();
            fileViewerWidget.setShowPreview(false);
            fileViewerWidget.setDocument("$item");
            container.addNewRow(fileViewerWidget.toComponent(dimensionFactory));
        }

        FileUploadWidget fileUploadWidget = (FileUploadWidget) parametrizedWidgetFactory
                .createParametrizedWidget(contractInput);
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
            ((Labeled) component).setLabel("");
            ((Labeled) component).setLabelWidth(0);
        }
        if (component instanceof Valuable) {
            ((Valuable) component).setValue(ParametrizedWidgetFactory.ITEM_ITERATOR);
        }
        List<Element> row = new ArrayList<>();
        row.add(component.toComponent(dimensionFactory));
        if (!contractInput.isReadOnly()) {
            row.add(createRemoveButton());
        }
        container.getRows().add(row);
        return container;
    }

    private Container toMultipleContainer(ContractInput contractInput) {
        WidgetContainer multipleContainer = parametrizedWidgetFactory.createWidgetContainer(contractInput);
        return multipleContainer.toContainer(dimensionFactory);
    }

    private Container toSimpleContainer(ContractInput contractInput) {
        return parametrizedWidgetFactory.createWidgetContainer(contractInput).toContainer(dimensionFactory);
    }

    private Component toSimpleComponent(ContractInput contractInput) {
        AbstractParametrizedWidget widget = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        return widget.toComponent(dimensionFactory);
    }

    public Container toContainer(NodeContractInput nodeContractInput, List<List<Element>> rows) {
        rows.add(Collections.<Element> singletonList(
                parametrizedWidgetFactory.createTitle(nodeContractInput).toComponent(dimensionFactory)));
        return nodeContractInput.isMultiple() ? toMultipleContainer(nodeContractInput)
                : toSimpleContainer(nodeContractInput);
    }

    public Component createSubmitButton(ButtonAction actionType) {
        ButtonWidget submitButton = parametrizedWidgetFactory.createSubmitButton(actionType);
        submitButton.setDataToSend(FormOutputData.NAME);
        submitButton.setPropertyValue("dataFromError", ParameterType.VARIABLE, SubmitErrorsListData.SUBMIT_ERROR_DATA);
        submitButton.setTargetUrlOnSuccess("/bonita");
        submitButton.setPropertyValue("disabled", ParameterType.EXPRESSION, "$form.$invalid");
        return submitButton.toComponent(dimensionFactory);
    }

    public Component createSubmitErrorAlert() {
        TextWidget widget = new TextWidget();
        widget.setCssClasses("alert alert-danger col-lg-6 col-lg-offset-3");
        widget.setPropertyValue(ParameterConstants.HIDDEN_PARAMETER, ParameterType.EXPRESSION,
                String.format("!%s.message", SubmitErrorsListData.SUBMIT_ERROR_DATA));
        widget.setPropertyValue("allowHTML", ParameterType.CONSTANT, true);
        StringBuffer sb = new StringBuffer();
        sb.append("<strong>Debug message</strong>");
        sb.append("\n");
        sb.append("<br/>");
        sb.append("\n");
        sb.append(String.format("{{%s.message}}", SubmitErrorsListData.SUBMIT_ERROR_DATA));
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
        ButtonWidget addButton = parametrizedWidgetFactory.createAddButton(contractInput);
        if (contractHasInput(contractInput)) {
            addButton.setValueToAdd(getValueToAddFromContract(contractInput));
        }
        return addButton.toComponent(dimensionFactory);
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
