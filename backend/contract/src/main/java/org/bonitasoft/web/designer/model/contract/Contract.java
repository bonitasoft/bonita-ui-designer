/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.model.contract;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.model.contract.databind.ContractDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ContractDeserializer.class)
public class Contract implements ContractInputContainer {

    private List<ContractInput> contractInput = new ArrayList<>();
    private ContractType contractType = ContractType.TASK;

    public void setContractInput(List<ContractInput> contractInput) {
        this.contractInput = contractInput;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
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
