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
package org.bonitasoft.web.designer.generator.mapping.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubmitErrorsListDataTest {

    @Test
    public void should_generate_a_script_to_create_errors_list() throws Exception {
        SubmitErrorsListData submitErrorsListData = new SubmitErrorsListData();

        assertThat(submitErrorsListData.create().getValue()).isEqualTo("if($data.formOutput && $data.formOutput._submitError && $data.formOutput._submitError.explanations){\n" +
                "\tconst liElements = $data.formOutput._submitError.explanations\n" +
                "\t\t.filter(cause => cause !== null)\n" +
                "\t\t.map(cause => \"<li>\" + cause + \"</li>\")\n" +
                "\t\t.join('');\n" +
                "\tif(liElements){\n" +
                "\t\treturn \"<ul>\" + liElements + \"</ul>\";\n" +
                "\t}\n" +
                "}");
    }
}
