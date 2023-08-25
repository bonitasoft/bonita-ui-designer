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
package org.bonitasoft.web.designer.model.contract.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.*;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;

public class ContractDeserializerTest {

    @Test
    public void deserialize_a_serialized_contract_json_document_into_a_contract_object() throws Exception {
        Contract aSimpleContract = aSimpleContract();
        byte[] serializedContract = new ObjectMapper().writeValueAsBytes(aSimpleContract);
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        Contract contract = contractDeserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(serializedContract), null);

        assertThat(contract.getInput()).extracting("name", "type").containsExactly(
                tuple("name", String.class.getName()),
                tuple("isValid", Boolean.class.getName()),
                tuple("ticket", NodeContractInput.class.getName()));
        var contractInput = contract.getInput().stream().filter(aContractInput -> aContractInput instanceof NodeContractInput).findFirst();
        assertThat(contractInput).isPresent();
        assertThat(contractInput.get().getInput())
                .extracting("name", "type").containsExactly(
                tuple("title", String.class.getName()),
                tuple("creationDate", Date.class.getName()),
                tuple("creationLocalDate", LocalDate.class.getName()),
                tuple("creationLocalDateTime", LocalDateTime.class.getName()),
                tuple("creationOffsetDateTime", OffsetDateTime.class.getName()),
                tuple("updateTime", Long.class.getName()));
    }

    @Test
    public void should_handle_serialization_of_a_contract_with_data_references() throws Exception {
        Contract aSimpleContractWithDataRef = aSimpleContractWithDataRef(EditMode.EDIT);
        byte[] serializedContract = new ObjectMapper().writeValueAsBytes(aSimpleContractWithDataRef);
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        Contract contract = contractDeserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(serializedContract), null);

        assertThat(contract.getInput()).extracting("name", "type", "dataReference").containsExactly(
                tuple("employeeInput", NodeContractInput.class.getName(), new BusinessDataReference("employee", "org.test.Employee", RelationType.COMPOSITION, LoadingType.EAGER)));

        var contractInput = contract.getInput().stream().filter(aContractInput -> aContractInput instanceof NodeContractInput).findFirst();
        assertThat(contractInput).isPresent();
        assertThat(contractInput.get().getInput()).extracting("name", "type").containsExactly(
                tuple("firstName", String.class.getName()),
                tuple("lastName", String.class.getName()),
                tuple("birthDate", LocalDate.class.getName()),
                tuple("manager", NodeContractInput.class.getName()),
                tuple("addresses", NodeContractInput.class.getName()));
    }


    @Test
    public void handle_contract_type() throws Exception {
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        assertThat(contractDeserializer.handledType()).isEqualTo(Contract.class);
    }

    @Test
    public void deserialize_an_contact_with_an_input_without_name() throws Exception {
        Contract anEmptyContract = aContract()
                .withInput(aContractInput(null).withType(null).withDescription(null).build()).build();
        byte[] serializedContract = new ObjectMapper().writeValueAsBytes(anEmptyContract);
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        Contract contract = contractDeserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(serializedContract), null);

        assertThat(contract.getInput()).extracting("name", "type", "description", "mandatory", "multiple", "input")
                .contains(tuple("", String.class.getName(), null, false, false, new ArrayList<>()));
    }

}
