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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.databind.BusinessObjectDeserializer;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.ContractInputContainer;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;

@JsonDeserialize(using = BusinessObjectDeserializer.class)
public class BusinessObject implements ContractInputContainer {

    private List<ContractInput> contractInput = new ArrayList<>();

    public void setContractInput(List<ContractInput> contractInput) {
        this.contractInput = contractInput;
    }


    @JsonIgnore
    public void accept(ContractInputVisitor contractInputVisitor) {
        for (ContractInput input : contractInput) {
            input.accept(contractInputVisitor);
        }
    }

    @Override
    public List<ContractInput> getInput() {
        return contractInput;
    }

    @Override
    public void addInput(ContractInput childInput) {
        contractInput.add(childInput);
    }
}
