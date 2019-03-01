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
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.experimental.mapping.data.FormInputData;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;

public class ContractInputDataHandler {

    private ContractInput input;

    public ContractInputDataHandler(ContractInput input) {
        this.input = input;
    }

    public boolean hasLazyDataRef() {
        return hasDataReference()
                && ((NodeContractInput) input).getDataReference().getLoadingType() == LoadingType.LAZY
                && doesNotHaveAMultipleParent(input);
    }

    public boolean hasDataReference() {
        return input instanceof NodeContractInput
                && ((NodeContractInput) input).getDataReference() != null;
    }

    public String inputValue() {
        return hasLazyDataRef() ? inputName()
                : buildPathForInputValue();
    }

    private String inputName() {
        return hasDataReference() ? nameFromDataRef()
                : input.getName();
    }

    private boolean doesNotHaveAMultipleParent(ContractInput contractInput) {
        ContractInput current = contractInput.getParent();
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
        return on("_").join(reverse(dataNames));
    }

    private String nameFromDataRef() {
        return hasLazyDataRef() ? lazyDataRef() : getRefName();
    }

    private String buildPathForInputValue() {
        List<String> pathNames = newArrayList();
        pathNames.add(inputName());
        boolean parentHasDataRef = hasDataReference();
        boolean parentHasLazyDataRef = false;
        if (parentHasDataRef) {
            parentHasLazyDataRef = hasLazyDataRef();
        }
        ContractInputDataHandler pInput = getParent();
        while (!parentHasLazyDataRef && pInput != null) {
            parentHasDataRef = pInput.hasDataReference();
            if (pInput.isMultiple()) {
                pathNames.add(ContractInputToWidgetMapper.ITEM_ITERATOR);
                break;
            } else {
                parentHasLazyDataRef = pInput.hasLazyDataRef();
                pathNames.add(pInput.inputName());
                pInput = pInput.getParent();
            }
        }
        if (pathNames.isEmpty()) {
            return null;
        } else if (pInput == null) {
            if (!parentHasDataRef) {
                pathNames.add(FormInputData.NAME);
            }
        }
        return on(".").join(reverse(pathNames));
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
        return ((NodeContractInput) input).getDataReference().getName();
    }

    public String getPath() {
        List<String> pathNames = newArrayList();
        pathNames.add(input.getName());
        ContractInput pInput = input.getParent();
        while (pInput != null) {
            if (pInput.isMultiple()) {
                break;
            } else {
                pathNames.add(pInput.getName());
                pInput = pInput.getParent();
            }
        }
        if (pathNames.isEmpty()) {
            return null;
        }
        return on(".").join(reverse(pathNames));
    }
}
