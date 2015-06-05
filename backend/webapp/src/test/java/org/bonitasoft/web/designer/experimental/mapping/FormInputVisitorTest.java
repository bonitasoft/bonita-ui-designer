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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aBooleanContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aMultipleStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aNodeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.anIntegerContractInput;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.junit.Before;
import org.junit.Test;

public class FormInputVisitorTest {

    JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());

    FormInputVisitor visitor;

    @Before
    public void setUp() throws Exception {
        visitor = new FormInputVisitor(objectMapper);
    }

    @Test
    public void should_build_form_input_from_simple_types() throws Exception {
        Contract contract = aContract().withInput(
                aBooleanContractInput("accepted"),
                anIntegerContractInput("age"),
                aStringContractInput("name")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson()).isEqualTo(objectMapper.prettyPrint("{\"accepted\":false,\"age\":0,\"name\":\"\"}"));
    }

    @Test
    public void should_build_form_input_from_complex_types() throws Exception {
        Contract contract = aContract().withInput(
                aNodeContractInput("person").withInput(
                        aStringContractInput("name"),
                        aNodeContractInput("details")
                                .withInput(anIntegerContractInput("age"))
                                .build())
                        .build(),
                aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson())
                .isEqualTo(objectMapper.prettyPrint("{\"person\":{\"name\":\"\",\"details\":{\"age\":0}},\"accepted\":false}"));
    }

    @Test
    public void should_add_an_empty_list_when_input_is_multiple() throws Exception {
        Contract contract = aContract().withInput(
                aNodeContractInput("persons").mulitple().build(),
                aMultipleStringContractInput("roles")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson())
                .isEqualTo(objectMapper.prettyPrint("{\"persons\":[],\"roles\":[]}"));
    }
}
