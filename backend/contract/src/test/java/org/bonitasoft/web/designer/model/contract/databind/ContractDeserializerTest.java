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

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleTaskContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;

import java.util.Date;

import org.bonitasoft.web.designer.model.contract.ContractType;
import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.builders.ContractBuilder;
import org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder;
import org.junit.Test;

import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ContractDeserializerTest {

    @Test
    public void deserialize_a_serialized_contract_json_document_into_a_contract_object() throws Exception {
        Contract aSimpleContract = aSimpleTaskContract();
        byte[] serializedContract = new ObjectMapper().writeValueAsBytes(aSimpleContract);
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        Contract contract = contractDeserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(serializedContract), null);

        assertThat(contract.getInput()).extracting("name", "type").containsExactly(
                tuple("name", String.class.getName()),
                tuple("isValid", Boolean.class.getName()),
                tuple("ticket", NodeContractInput.class.getName()));
        assertThat(find(contract.getInput(), instanceOf(NodeContractInput.class)).getInput()).extracting("name", "type").containsExactly(
                tuple("title", String.class.getName()),
                tuple("creationDate", Date.class.getName()),
                tuple("updateTime", Long.class.getName()));

        assertThat(find(contract.getInput(), instanceOf(NodeContractInput.class)).getInput().get(0).path()).isEqualTo("ticket.title");
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
                .contains(tuple("", String.class.getName(), null, false, false, newArrayList()));
    }

    @Test
    public void should_dezerialize_contract_type() throws Exception {
        Contract anEmptyContract = aContract().withType(ContractType.PROCESS).build();
        byte[] serializedContract = new ObjectMapper().writeValueAsBytes(anEmptyContract);
        ContractDeserializer contractDeserializer = new ContractDeserializer();

        Contract contract = contractDeserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(serializedContract), null);

        assertThat(contract.getContractType()).isEqualTo(ContractType.PROCESS);

    }
}
