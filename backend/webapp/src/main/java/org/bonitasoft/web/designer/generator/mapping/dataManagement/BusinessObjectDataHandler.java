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
package org.bonitasoft.web.designer.generator.mapping.dataManagement;

import org.bonitasoft.web.designer.generator.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.ContractInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.join;


public class BusinessObjectDataHandler extends ContractInputDataHandler {

    private final ContractInput input;

    public BusinessObjectDataHandler(ContractInput input) {
        super(input);
        this.input = input;
    }

    public String getParentValue() {
        if (this.input.getParent() == null) {
            return null;
        }

        List<String> pathNames = new ArrayList<>();
        pathNames.add(inputName());

        //multiple
        var inputParent = ((NodeBusinessObjectInput) this.input.getParent());
        BusinessObjectDataHandler pInput = getParent();
        var prefix = inputParent.isMultiple() ? inputParent.getPageDataNameSelected() : inputParent.getPageDataName();

        //Eager
        var hasEagerRef = hasEagerRef(inputParent);
        while (pInput != null && hasEagerRef) {
            var nodeInput = (NodeBusinessObjectInput) pInput.input;
            if (nodeInput.isMultiple()) {
                prefix = nodeInput.getPageDataNameSelected();
                break;
            } else {
                pathNames.add(nodeInput.getBusinessObjectAttributeName());
            }
            hasEagerRef = hasEagerRef(nodeInput);
            pInput = pInput.getParent();
        }

        pathNames.add(prefix);

        var reversed = new LinkedList<>(pathNames);
        Collections.reverse(reversed);
        return join(".", reversed);
    }

    private boolean hasEagerRef(NodeBusinessObjectInput inputParent) {
        return inputParent.getDataReference() != null && BusinessDataReference.LoadingType.EAGER.equals(inputParent.getDataReference().getLoadingType());
    }

    @Override
    public String inputValue() {
        if (Objects.equals(PERSISTENCE_ID_INPUT_NAME, input.getName()) && hasAggregatedParentRef(input)) {
            var parentInput = getParent();
            return parentInput.hasLazyDataRef() ? parentInput.inputName()
                    : parentInput.getParentValue();
        }
        return hasLazyDataRef() ? inputName()
                : getParentValue();
    }

    @Override
    public BusinessObjectDataHandler getParent() {
        return input.getParent() != null ? new BusinessObjectDataHandler(input.getParent()) : null;
    }
}
