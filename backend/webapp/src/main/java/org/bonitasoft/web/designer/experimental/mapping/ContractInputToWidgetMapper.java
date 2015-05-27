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

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.bonitasoft.web.designer.experimental.parametrizedWidget.AbstractParametrizedWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Labeled;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParametrizedWidgetFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Valuable;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;

@Named
public class ContractInputToWidgetMapper {

    private static final String ITEM_ITERATOR = "$item";
    static final String FORM_INPUT_DATA = "formInput";
    static final String FORM_OUTPUT_DATA = "formOutput";
    private ParametrizedWidgetFactory parametrizedWidgetFactory;

    public ContractInputToWidgetMapper() {
        parametrizedWidgetFactory = new ParametrizedWidgetFactory();
    }

    public Element toElement(ContractInput contractInput, List<List<Element>> rows) {
        return contractInput.isMultiple() ? toMultipleComponent(contractInput, rows) : toSimpleComponent(contractInput);
    }

    private Container toMultipleComponent(ContractInput contractInput, List<List<Element>> rows) {
        Container container = toMultipleContainer(contractInput);
        rows.add(Collections.<Element> singletonList(parametrizedWidgetFactory.createTitle(contractInput).getAdapter(Component.class)));
        AbstractParametrizedWidget component = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (component instanceof Labeled) {
            ((Labeled) component).setLabel("");
            ((Labeled) component).setLabelWidth(0);
        }
        if (component instanceof Valuable) {
            ((Valuable) component).setValue(ITEM_ITERATOR);
        }
        container.getRows().add(Collections.<Element> singletonList(component.getAdapter(Component.class)));
        return container;
    }

    private String multipleInputValue(ContractInput contractInput) {
        return contractInput.getParent() != null ? on(".").join(ITEM_ITERATOR, contractInput.getName()) : ITEM_ITERATOR;
    }

    private Container toMultipleContainer(ContractInput contractInput) {
        WidgetContainer multipleContainer = parametrizedWidgetFactory.createWidgetContainer();
        multipleContainer.setRepeatedCollection(isParentMultiple(contractInput) ? multipleInputValue(contractInput) : inputValue(contractInput));
        return multipleContainer.getAdapter(Container.class);
    }

    private boolean isParentMultiple(ContractInput contractInput) {
        return contractInput.getParent() != null && contractInput.getParent().isMultiple();
    }

    private Container toSimpleContainer(NodeContractInput nodeContractInput) {
        return parametrizedWidgetFactory.createWidgetContainer().getAdapter(Container.class);
    }

    private Component toSimpleComponent(ContractInput contractInput) {
        AbstractParametrizedWidget widget = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (widget instanceof Valuable) {
            ((Valuable) widget).setValue(contractInput.getParent() != null && contractInput.getParent().isMultiple()
                    ? multipleInputValue(contractInput) : inputValue(contractInput));
        }
        return widget.getAdapter(Component.class);
    }

    public Container toContainer(NodeContractInput nodeContractInput, List<List<Element>> rows) {
        rows.add(Collections.<Element> singletonList(parametrizedWidgetFactory.createTitle(nodeContractInput).getAdapter(Component.class)));
        return nodeContractInput.isMultiple() ? toMultipleContainer(nodeContractInput) : toSimpleContainer(nodeContractInput);
    }

    public Component createSubmitButton(Contract contract, ButtonAction actionType) {
        ButtonWidget submitButton = parametrizedWidgetFactory.createSubmitButton(contract, actionType);
        submitButton.setDataToSend(FORM_OUTPUT_DATA);
        return submitButton.getAdapter(Component.class);
    }

    private String inputValue(ContractInput contractInput) {
        return on(".").join(FORM_INPUT_DATA, contractInput.path());
    }

    public boolean canCreateComponent(ContractInput contractInput) {
        return parametrizedWidgetFactory.isSupported(contractInput);
    }

    public Component createAddButton(ContractInput contractInput) {
        ButtonWidget addButton = parametrizedWidgetFactory.createAddButton();
        addButton.setCollectionToModify(inputValue(contractInput));
        return addButton.getAdapter(Component.class);
    }

    public Component createRemoveButton(ContractInput contractInput) {
        ButtonWidget removeButton = parametrizedWidgetFactory.createRemoveButton();
        removeButton.setCollectionToModify(inputValue(contractInput));
        return removeButton.getAdapter(Component.class);
    }

}
