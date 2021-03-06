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

import org.bonitasoft.web.designer.generator.mapping.data.FormInputData;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParametrizedWidgetFactory;
import org.bonitasoft.web.designer.model.contract.AbstractContractInput;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;


public class ContractInputDataHandler {

    public static final String PERSISTENCE_ID_INPUT_NAME = "persistenceId_string";
    public static final String ITERATOR_NAME = "it";
    private final ContractInput input;

    public ContractInputDataHandler(ContractInput input) {
        this.input = input;
    }

    public static boolean hasAggregatedParentRef(ContractInput input) {
        return input.getParent() instanceof NodeContractInput
                && ((NodeContractInput) input.getParent()).getDataReference() != null
                && ((NodeContractInput) input.getParent()).getDataReference()
                .getRelationType() == RelationType.AGGREGATION;
    }

    public static boolean hasMultipleComposedParentRef(ContractInput input) {
        return input.getParent() instanceof NodeContractInput
                && ((NodeContractInput) input.getParent()).getDataReference() != null
                && input.getParent().isMultiple()
                && ((NodeContractInput) input.getParent()).getDataReference()
                .getRelationType() == RelationType.COMPOSITION;
    }

    public static boolean shouldGenerateWidgetForInput(ContractInput input) {
        return Objects.equals(input.getName(), PERSISTENCE_ID_INPUT_NAME) && hasMultipleComposedParentRef(input);
    }

    public boolean hasLazyDataRef() {
        return hasDataReference()
                && input instanceof NodeContractInput
                && ((NodeContractInput) input).getDataReference().getLoadingType() == LoadingType.LAZY
                && doesNotHaveAMultipleParent(input);
    }

    public boolean hasDataReference() {
        return input.getMode() == EditMode.EDIT
                && input instanceof AbstractContractInput
                && ((AbstractContractInput) input).getDataReference() != null;
    }

    public String inputValue() {
        if (Objects.equals(PERSISTENCE_ID_INPUT_NAME, input.getName()) && hasAggregatedParentRef(input)) {
            ContractInputDataHandler parentInput = getParent();
            return parentInput.hasLazyDataRef() ? parentInput.inputName()
                    : parentInput.buildPathForInputValue();
        }
        return hasLazyDataRef() ? inputName()
                : buildPathForInputValue();
    }

    public String inputName() {
        return hasDataReference() && EditMode.EDIT == getMode() ? nameFromDataRef()
                : input.getName();
    }

    private boolean doesNotHaveAMultipleParent(ContractInput contractInput) {
        var current = contractInput.getParent();
        while (current != null && !current.isMultiple()) {
            current = current.getParent();
        }
        return current == null;
    }

    private String lazyDataRef() {
        List<String> dataNames = new ArrayList<>();
        NodeContractInput current = (NodeContractInput) input;
        while (current != null) {
            dataNames.add(current.getDataReference().getName());
            current = (NodeContractInput) current.getParent();
        }

        var reversed = new LinkedList<>(dataNames);
        Collections.reverse(reversed);
        return join("_", reversed);
    }

    private String nameFromDataRef() {
        return hasLazyDataRef() ? lazyDataRef() : getRefName();
    }

    private String buildPathForInputValue() {
        List<String> pathNames = new ArrayList<>();
        pathNames.add(inputName());
        var parentHasDataRef = hasDataReference();
        var parentHasLazyDataRef = false;
        if (parentHasDataRef) {
            parentHasLazyDataRef = hasLazyDataRef();
        }
        var pInput = getParent();
        while (!parentHasLazyDataRef && pInput != null) {
            parentHasDataRef = pInput.hasDataReference();
            if (pInput.isMultiple()) {
                pathNames.add(ParametrizedWidgetFactory.ITEM_ITERATOR);
                break;
            }
            parentHasLazyDataRef = pInput.hasLazyDataRef();
            pathNames.add(pInput.inputName());
            pInput = pInput.getParent();
        }
        if (pathNames.isEmpty()) {
            return null;
        } else if (pInput == null && !parentHasDataRef) {
            pathNames.add(FormInputData.INPUT_NAME);
        }

        var reversed = new LinkedList<>(pathNames);
        Collections.reverse(reversed);
        return join(".", reversed);
    }

    public ContractInputDataHandler getParent() {
        return input.getParent() != null ? new ContractInputDataHandler(input.getParent()) : null;
    }

    public boolean isMultiple() {
        return input.isMultiple();
    }

    public String getInputName() {
        return input.getName();
    }

    public String getRefName() {
        return input instanceof AbstractContractInput
                && ((AbstractContractInput) input).getDataReference() != null
                ? ((AbstractContractInput) input).getDataReference().getName()
                : null;
    }

    public String getRefType() {
        return input instanceof AbstractContractInput
                && ((AbstractContractInput) input).getDataReference() != null
                ? ((AbstractContractInput) input).getDataReference().getType()
                : null;
    }

    public String getDataPath() {
        List<String> pathNames = new ArrayList<>();
        pathNames.add(getRefName() == null ? getInputName() : getRefName());
        var parent = getParent();
        while (parent != null) {
            if (parent.isMultiple()) {
                pathNames.add(ITERATOR_NAME);
                break;
            }
            pathNames.add(parent.getRefName() == null ? parent.getInputName() : parent.getRefName());
            parent = parent.getParent();
        }
        if (pathNames.isEmpty()) {
            return null;
        }
        if (parent == null) {
            pathNames.add("$data");
        }

        var reversed = new LinkedList<>(pathNames);
        Collections.reverse(reversed);
        return join(".", reversed);
    }

    public EditMode getMode() {
        return input.getMode();
    }

    public boolean isDocumentEdition() {
        return isDocument() && input.getMode() == EditMode.EDIT;
    }

    public boolean isDocument() {
        return Objects.equals(File.class.getName(), input.getType());
    }

    public List<ContractInputDataHandler> getNonReadOnlyChildren() {
        return input.getInput().stream()
                .filter(i -> !i.isReadOnly())
                .map(ContractInputDataHandler::new)
                .collect(toList());
    }
}
