/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
import static org.bonitasoft.web.designer.model.data.DataType.BUSINESSDATA;
import static org.bonitasoft.web.designer.model.data.DataType.URL;

public class BonitaBusinessDataResourceResourcePredicateTest {

    private Variable variable;
    private BonitaBusinessDataResourcePredicate predicate;

    @Before
    public void setUp() throws Exception {
        variable = new Variable(BUSINESSDATA, "");
        predicate = new BonitaBusinessDataResourcePredicate();
    }

    @Test
    public void should_return_true_when_variable_is_a_businessData() throws Exception {
        assertThat(predicate.test(variable)).isTrue();
    }

    @Test
    public void should_return_false_when_variable_is_not_a_businessData() throws Exception {
        variable.setType(URL);

        assertThat(predicate.test(variable)).isFalse();
    }

    @Test
    public void should_return_false_when_variable_value_is_empty() throws Exception {
        variable.setValue(null);
        assertThat(predicate.test(variable)).isFalse();
    }
}
