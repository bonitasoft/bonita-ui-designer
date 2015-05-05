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
package org.bonitasoft.web.designer.model.contract.builders;

import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aBooleanContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLongContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aMultipleStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aNodeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.ContractType;

public class ContractBuilder {

    private Contract contract;

    private ContractBuilder(Contract contract) {
        this.contract = contract;
    }

    public static ContractBuilder aContract() {
        return new ContractBuilder(new Contract());
    }

    public ContractBuilder withType(ContractType type) {
        contract.setContractType(type);
        return this;
    }

    public ContractBuilder withInput(ContractInput... contractInput) {
        for (ContractInput input : contractInput) {
            contract.addInput(input);
        }
        return this;
    }

    public Contract build() {
        return contract;
    }

    public static Contract aSimpleTaskContract() {
        return aContract().withInput(aContractInput("name").withDescription("employee name").build(),
                aBooleanContractInput("isValid"),
                aNodeContractInput("ticket").withInput(
                        aStringContractInput("title"),
                        aDateContractInput("creationDate"),
                        aLongContractInput("updateTime")).build()).build();
    }

    public static Contract aSimpleProcessContract() {
        return aContract().withType(ContractType.PROCESS).withInput(aStringContractInput("name"),
                aBooleanContractInput("isValid"),
                aNodeContractInput("ticket").withInput(
                        aStringContractInput("title"),
                        aDateContractInput("creationDate"),
                        aLongContractInput("updateTime")).build()).build();
    }

    public static Contract aContractWithMultipleInput() {
        return aContract().withInput(aMultipleStringContractInput("names")).build();
    }
}
