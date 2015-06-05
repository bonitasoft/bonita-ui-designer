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
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aNodeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.anIntegerContractInput;

import org.bonitasoft.web.designer.model.contract.Contract;
import org.junit.Test;

public class FormOutputVisitorTest {

    FormOutputVisitor visitor = new FormOutputVisitor();

    @Test
    public void should_build_form_input_from_simple_types() throws Exception {
        Contract contract = aContract().withInput(aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJavascriptExpression()).isEqualTo("return {\n" +
                "\t'accepted': $data.formInput.accepted\n" +
                "};");
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

        assertThat(visitor.toJavascriptExpression())
                .isEqualTo("return {\n" +
                        "\t'person': $data.formInput.person,\n" +
                        "\t'accepted': $data.formInput.accepted\n" +
                        "};");
    }
}
