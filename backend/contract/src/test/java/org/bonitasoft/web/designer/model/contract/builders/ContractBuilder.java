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
package org.bonitasoft.web.designer.model.contract.builders;

import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aBooleanContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLongContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aMultipleStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aNodeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aOffsetDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

import java.time.LocalDate;
import java.util.List;

import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.EditMode;

public class ContractBuilder {

    private Contract contract;
    private EditMode editMode = EditMode.CREATE ;

    private ContractBuilder(Contract contract) {
        this.contract = contract;
    }

    public static ContractBuilder aContract() {
        return new ContractBuilder(new Contract());
    }

    public ContractBuilder withInput(ContractInput... contractInput) {
        for (ContractInput input : contractInput) {
            contract.addInput(input);
        }
        return this;
    }
    
    public ContractBuilder inEditMode() {
        editMode = EditMode.EDIT;
        return this;
    }

    public Contract build() {
        updateEditMode(contract.getInput());
        return contract;
    }

    private void updateEditMode(List<ContractInput> input) {
        input.stream().forEach(in -> {
            in.setMode(editMode);
            updateEditMode(in.getInput());
        });
    }

    public static Contract aSimpleContract() {
        return aContract().withInput(aContractInput("name").withDescription("employee name").build(),
                aBooleanContractInput("isValid"),
                aNodeContractInput("ticket").withInput(
                        aStringContractInput("title"),
                        aDateContractInput("creationDate"),
                        aLocalDateContractInput("creationLocalDate"),
                        aLocalDateTimeContractInput("creationLocalDateTime"),
                        aOffsetDateTimeContractInput("creationOffsetDateTime"),
                        aLongContractInput("updateTime")).build())
                .build();

    }

    public static Contract aSimpleContractWithDataRef(EditMode mode) {
        ContractBuilder contractBuilder = aContract();
        if(mode == EditMode.EDIT) {
            contractBuilder =  contractBuilder.inEditMode();
        }
        return contractBuilder.withInput(
                aNodeContractInput("employeeInput")
                        .withDataReference(new BusinessDataReference("employee", "org.test.Employee",
                                RelationType.COMPOSITION, LoadingType.EAGER))
                        .withInput(
                                aContractInput("firstName").build(),
                                aContractInput("lastName").build(),
                                aContractInput("birthDate").withType(LocalDate.class.getName()).build(),
                                aNodeContractInput("manager")
                                        .withDataReference(new BusinessDataReference("manager", "org.test.Employee",
                                                RelationType.COMPOSITION, LoadingType.LAZY))
                                        .withInput(aContractInput("firstName").build(),
                                                aNodeContractInput("addresses")
                                                        .mulitple()
                                                        .withDataReference(new BusinessDataReference("addresses",
                                                                "org.test.Address", RelationType.COMPOSITION,
                                                                LoadingType.LAZY))
                                                        .build())
                                        .build(),
                                aNodeContractInput("addresses")
                                        .withDataReference(new BusinessDataReference("addresses", "org.test.Address",
                                                RelationType.COMPOSITION, LoadingType.LAZY))
                                        .mulitple()
                                        .withInput(aContractInput("street").build(),
                                                aContractInput("zipcode").build(),
                                                aContractInput("city").build(),
                                                aNodeContractInput("country")
                                                        .withDataReference(
                                                                new BusinessDataReference("country", "org.test.Country",
                                                                        RelationType.COMPOSITION, LoadingType.LAZY))
                                                        .build())
                                        .build())
                        .build())
                .build();

    }

    public static Contract aContractWithDataRefAndAggregation(EditMode mode) {
        ContractBuilder contractBuilder = aContract();
        if(mode == EditMode.EDIT) {
            contractBuilder =  contractBuilder.inEditMode();
        }
        return contractBuilder.withInput(
                aNodeContractInput("employeeInput")
                        .withDataReference(new BusinessDataReference("employee", "org.test.Employee",
                                RelationType.COMPOSITION, LoadingType.EAGER))
                        .withInput(
                                aContractInput("firstName").build(),
                                aContractInput("lastName").build(),
                                aContractInput("birthDate").withType(LocalDate.class.getName()).build(),
                                aNodeContractInput("manager")
                                        .withDataReference(new BusinessDataReference("manager", "org.test.Employee",
                                                RelationType.AGGREGATION, LoadingType.EAGER))
                                        .withInput(aContractInput("persistenceId_string").build())
                                        .build(),
                                aNodeContractInput("addresses")
                                        .withDataReference(new BusinessDataReference("addresses", "org.test.Address",
                                                RelationType.COMPOSITION, LoadingType.LAZY))
                                        .mulitple()
                                        .withInput(aContractInput("persistenceId_string").build(),
                                                aContractInput("street").build(),
                                                aContractInput("zipcode").build(),
                                                aContractInput("city").build())
                                        .build())
                        .build())
                .build();

    }

    public static Contract aContractWithMultipleInput() {
        return aContract().withInput(aMultipleStringContractInput("names")).build();
    }

   
}
