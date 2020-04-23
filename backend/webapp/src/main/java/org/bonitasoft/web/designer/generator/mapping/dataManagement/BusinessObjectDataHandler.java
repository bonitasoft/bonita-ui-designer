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

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.List;
import java.util.Objects;

import org.bonitasoft.web.designer.generator.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.ContractInput;

public class BusinessObjectDataHandler extends ContractInputDataHandler {

    private ContractInput input;

    public BusinessObjectDataHandler(ContractInput input) {
        super(input);
        this.input = input;
    }

    public String getParentValue() {
        if (this.input.getParent() == null) {
            return null;
        }

        List<String> pathNames = newArrayList();
        pathNames.add(inputName());

        //multiple
        NodeBusinessObjectInput name = ((NodeBusinessObjectInput) this.input.getParent());
        BusinessObjectDataHandler pInput = getParent();
        String prefix = pInput.isMultiple() ? name.getPageDataNameSelected() : name.getPageDataName();

        //Eager
        if(name.getDataReference() != null && BusinessDataReference.LoadingType.EAGER.equals(name.getDataReference().getLoadingType())){
            NodeBusinessObjectInput eagerParent = ((NodeBusinessObjectInput) name.getParent());
            prefix = eagerParent.isMultiple() ? eagerParent.getPageDataNameSelected() : eagerParent.getPageDataName();
            pathNames.add(name.getBusinessObjectAttributeName());
        }
        pathNames.add(prefix);

        return on(".").join(reverse(pathNames));
    }

    @Override
    public String inputValue() {
        if (Objects.equals(PERSISTENCEID_INPUT_NAME, input.getName()) && hasAggregatedParentRef(input)) {
            BusinessObjectDataHandler parentInput = getParent();
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
