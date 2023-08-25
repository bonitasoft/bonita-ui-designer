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

package org.bonitasoft.web.designer.generator.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.generator.mapping.data.FormInputVisitor;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.*;

public class FormInputVisitorTest {

    JsonHandler jsonHandler = new JacksonJsonHandler(new ObjectMapper());

    FormInputVisitor visitor;

    @BeforeEach
    public void setUp() throws Exception {
        visitor = new FormInputVisitor(jsonHandler);
    }

    @Test
    public void should_build_form_input_from_simple_types() throws Exception {
        Contract contract = aContract().withInput(
                aBooleanContractInput("accepted"),
                anIntegerContractInput("age"),
                aStringContractInput("name")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson()).isEqualTo(jsonHandler.prettyPrint("{\"accepted\":false,\"age\":0,\"name\":\"\"}"));
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
                .isEqualTo(
                        jsonHandler.prettyPrint("{\"person\":{\"name\":\"\",\"details\":{\"age\":0}},\"accepted\":false}"));
    }

    @Test
    public void should_ignore_complex_types_with_dataRef_in_formInput_in_edit_mode() throws Exception {
        Contract contract = aContract().inEditMode().withInput(
                aNodeContractInput("person")
                        .withDataReference(new BusinessDataReference("person", "org.test.Person", RelationType.COMPOSITION,
                                LoadingType.EAGER))
                        .withInput(
                                aStringContractInput("name"),
                                aNodeContractInput("details")
                                        .withInput(anIntegerContractInput("age"))
                                        .build())
                        .build(),
                aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson())
                .isEqualTo(jsonHandler.prettyPrint("{\"accepted\":false}"));
    }

    @Test
    public void should_add_an_empty_list_when_input_is_multiple() throws Exception {
        Contract contract = aContract().withInput(
                aNodeContractInput("persons").mulitple().build(),
                aMultipleStringContractInput("roles")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson())
                .isEqualTo(jsonHandler.prettyPrint("{\"persons\":[],\"roles\":[]}"));
    }

    @Test
    public void should_default_to_null_when_contract_input_is_a_file() throws Exception {
        Contract contract = aContract().withInput(aFileContractInput("myFile")).build();

        contract.accept(visitor);

        assertThat(visitor.toJson()).isEqualTo(jsonHandler.prettyPrint("{\"myFile\":null}"));
    }

    @Test
    public void should_accept_non_mandatory_local_date() throws Exception {
        LeafContractInput aLocalDateContractInput = aLocalDateContractInput("myDate");
        LeafContractInput aLocalDateTimeContractInput = new LeafContractInput("myLocalDateTime", LocalDateTime.class);
        LeafContractInput aOffsetDateTimeContractInput = new LeafContractInput("myOffsetDateTime", OffsetDateTime.class);
        LeafContractInput aLegacyDateContractInput = new LeafContractInput("myLgacyDate", Date.class);

        aLocalDateContractInput.setMandatory(false);
        aLocalDateTimeContractInput.setMandatory(false);
        aOffsetDateTimeContractInput.setMandatory(false);
        aLegacyDateContractInput.setMandatory(false);

        Contract contract = aContract().withInput(aLocalDateContractInput,
                aLocalDateTimeContractInput,
                aOffsetDateTimeContractInput,
                aLegacyDateContractInput)
                .build();

        contract.accept(visitor);

        assertThat(visitor.toJson()).contains("\"myDate\" : null");
        assertThat(visitor.toJson()).contains("\"myLocalDateTime\" : null");
        assertThat(visitor.toJson()).contains("\"myOffsetDateTime\" : null");
        assertThat(visitor.toJson()).contains("\"myLgacyDate\" : null");
    }
}
