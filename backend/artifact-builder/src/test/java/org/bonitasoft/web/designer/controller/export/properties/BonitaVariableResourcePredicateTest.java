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
package org.bonitasoft.web.designer.controller.export.properties;

import org.bonitasoft.web.designer.model.data.Variable;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.data.DataType.*;

public class BonitaVariableResourcePredicateTest {

    private Variable variable;
    private BonitaVariableResourcePredicate predicate;

    @Before
    public void setUp() throws Exception {
        variable = new Variable(URL, "");
        predicate = new BonitaVariableResourcePredicate("matches");
    }

    @Test
    public void should_return_true_if_match_regex() throws Exception {
        variable.setType(URL);
        variable.setDisplayValue("matches");

        assertThat(predicate.test(variable)).isTrue();
    }

    @Test
    public void should_not_accept_url_not_accessing_bonita_api() throws Exception {
        variable.setType(URL);
        variable.setDisplayValue("do not match");

        assertThat(predicate.test(variable)).isFalse();
    }

    @Test
    public void should_not_accept_a_constant_data() throws Exception {
        variable.setType(CONSTANT);
        variable.setDisplayValue("matches");

        assertThat(predicate.test(variable)).isFalse();
    }

    @Test
    public void should_not_accept_an_expression_data() throws Exception {
        variable.setType(EXPRESSION);
        variable.setDisplayValue("matches");

        assertThat(predicate.test(variable)).isFalse();
    }

    @Test
    public void should_not_accept_a_JSON_data() throws Exception {
        variable.setType(JSON);
        variable.setDisplayValue("matches");

        assertThat(predicate.test(variable)).isFalse();
    }
}
