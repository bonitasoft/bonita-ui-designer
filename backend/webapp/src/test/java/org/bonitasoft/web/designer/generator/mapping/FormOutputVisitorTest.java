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

import org.bonitasoft.web.designer.generator.mapping.data.FormOutputVisitor;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContract;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.*;

public class FormOutputVisitorTest {

    FormOutputVisitor visitor = new FormOutputVisitor();

    @Test
    public void should_build_form_output_from_simple_types() throws Exception {
        Contract contract = aContract().withInput(aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJavascriptExpression()).isEqualTo("return {\n" +
                "\taccepted: $data.formInput.accepted\n" +
                "}");
    }

    @Test
    public void should_build_form_output_from_complex_types() throws Exception {
        Contract contract = aContract().inEditMode().withInput(
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
                        "\tperson: $data.formInput.person,\n" +
                        "\taccepted: $data.formInput.accepted\n" +
                        "}");

    }

    @Test
    public void should_build_form_output_from_complex_types_with_data_reference() throws Exception {
        Contract contract = aContract().inEditMode().withInput(
                aNodeContractInput("person")
                        .withDataReference(new BusinessDataReference("person", "org.test.Person",
                                RelationType.COMPOSITION, LoadingType.EAGER))
                        .withInput(
                                aStringContractInput("name"),
                                aNodeContractInput("details")
                                .mulitple()
                                .withDataReference(new BusinessDataReference("details", "org.test.Detail",
                                        RelationType.COMPOSITION, LoadingType.EAGER))
                                        .withInput(anIntegerContractInput("age"))
                                        .build())
                        .build(),
                aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJavascriptExpression())
                .isEqualTo("if( $data.person ){\n" +
                        "\treturn {\n" +
                        "\t\t//map person variable to expected task contract input\n" +
                        "\t\tperson: {\n" +
                        "\t\t\tname: $data.person.name !== undefined ? $data.person.name : null,\n" +
                        "\t\t\tdetails: $data.person.details.map( it => ({\n" +
                        "\t\t\t\tage: it.age !== undefined ? it.age : null\n" +
                        "\t\t\t}))\n" +
                        "\t\t},\n" +
                        "\t\taccepted: $data.formInput.accepted\n" +
                        "\t}\n" +
                        "}");
    }

    @Test
    public void should_build_form_output_from_complex_types_with_lazy_data_reference() throws Exception {
        Contract contract = aContract().inEditMode().withInput(
                aNodeContractInput("personInput")
                        .withDataReference(new BusinessDataReference("person", "org.test.Person",
                                RelationType.COMPOSITION, LoadingType.EAGER))
                        .withInput(
                                aStringContractInput("name"),
                                aNodeContractInput("detail")
                                        .withDataReference(new BusinessDataReference("detail", "org.test.Detail",
                                                RelationType.COMPOSITION, LoadingType.LAZY))
                                        .withInput(anIntegerContractInput("age"),aStringContractInput("carnation"))
                                        .build())
                        .build(),
                aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJavascriptExpression())
                .isEqualTo("if( $data.person && $data.person_detail ){\n" +
                		"\t//attach lazy references variables to parent variables\n"+
                		"\t$data.person.detail = $data.person_detail;\n"+
                        "\treturn {\n" +
                		"\t\t//map person variable to expected task contract input\n"+
                        "\t\tpersonInput: {\n" +
                        "\t\t\tname: $data.person.name !== undefined ? $data.person.name : null,\n" +
                        "\t\t\tdetail: $data.person.detail ? {\n" +
                        "\t\t\t\tage: $data.person.detail.age !== undefined ? $data.person.detail.age : null,\n" +
                        "\t\t\t\tcarnation: $data.person.detail.carnation !== undefined ? $data.person.detail.carnation : null\n" +
                        "\t\t\t} : null\n" +
                        "\t\t},\n" +
                        "\t\taccepted: $data.formInput.accepted\n" +
                        "\t}\n" +
                        "}");
    }

    @Test
    public void should_build_form_output_from_multiple_complex_types_with_data_reference() throws Exception {
        Contract contract = aContract().inEditMode().withInput(
                aNodeContractInput("persons")
                        .mulitple()
                        .withDataReference(new BusinessDataReference("persons", "org.test.Person",
                                RelationType.COMPOSITION, LoadingType.EAGER))
                        .withInput(
                                aStringContractInput("name"),
                                aNodeContractInput("details")
                                .mulitple()
                                .withDataReference(new BusinessDataReference("details", "org.test.Detail",
                                        RelationType.COMPOSITION, LoadingType.EAGER))
                                        .withInput(anIntegerContractInput("age"))
                                        .build())
                        .build(),
                aBooleanContractInput("accepted")).build();

        contract.accept(visitor);

        assertThat(visitor.toJavascriptExpression())
                .isEqualTo("if( $data.persons ){\n" +
                        "\treturn {\n" +
                        "\t\t//map persons variable to expected task contract input\n" +
                        "\t\tpersons: $data.persons.map( it => ({\n" +
                        "\t\t\tname: it.name !== undefined ? it.name : null,\n" +
                        "\t\t\tdetails: it.details.map( it => ({\n" +
                        "\t\t\t\tage: it.age !== undefined ? it.age : null\n" +
                        "\t\t\t}))\n" +
                        "\t\t})),\n" +
                        "\t\taccepted: $data.formInput.accepted\n" +
                        "\t}\n" +
                        "}");
    }
}
