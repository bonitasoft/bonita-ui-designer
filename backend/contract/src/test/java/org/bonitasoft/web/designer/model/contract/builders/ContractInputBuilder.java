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

import java.util.Date;

import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class ContractInputBuilder {

    private ContractInput contractInput;

    private ContractInputBuilder(ContractInput contractInput) {
        this.contractInput = contractInput;
    }

    public ContractInputBuilder withType(String classname) {
        contractInput.setType(classname);
        return this;
    }

    public ContractInputBuilder withDescription(String description) {
        contractInput.setDescription(description);
        return this;
    }

    public ContractInputBuilder mulitple() {
        contractInput.setMultiple(true);
        return this;
    }

    public ContractInputBuilder simple() {
        contractInput.setMultiple(false);
        return this;
    }

    public ContractInputBuilder mandatory() {
        contractInput.setMandatory(true);
        return this;
    }

    public ContractInputBuilder optional() {
        contractInput.setMandatory(false);
        return this;
    }

    public ContractInputBuilder withInput(ContractInput... contractInputs) {
        for (ContractInput input : contractInputs) {
            contractInput.addInput(input);
        }
        return this;
    }

    public ContractInput build() {
        return contractInput;
    }

    public static ContractInputBuilder aContractInput(String name) {
        return new ContractInputBuilder(new LeafContractInput(name, String.class));
    }

    public static LeafContractInput aBooleanContractInput(String name) {
        return new LeafContractInput(name, Boolean.class);
    }

    public static LeafContractInput aStringContractInput(String name) {
        return new LeafContractInput(name, String.class);
    }

    public static LeafContractInput aMultipleStringContractInput(String name) {
        LeafContractInput input = new LeafContractInput(name, String.class);
        input.setMultiple(true);
        return input;
    }

    public static LeafContractInput aLongContractInput(String name) {
        return new LeafContractInput(name, Long.class);
    }

    public static LeafContractInput anIntegerContractInput(String name) {
        return new LeafContractInput(name, Integer.class);
    }

    public static LeafContractInput aDateContractInput(String name) {
        return new LeafContractInput(name, Date.class);
    }

    public static ContractInputBuilder aNodeContractInput(String name) {
        return new ContractInputBuilder(new NodeContractInput(name));
    }
}
