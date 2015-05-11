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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.data.DataType.*;

import org.bonitasoft.web.designer.model.data.Data;
import org.junit.Before;
import org.junit.Test;

public class BonitaResourcePredicateTest {

    private Data data;
    private BonitaResourcePredicate predicate;

    @Before
    public void setUp() throws Exception {
        data = new Data();
        predicate = new BonitaResourcePredicate("matches");
    }

    @Test
    public void should_return_true_if_match_regex() throws Exception {
        data.setType(URL);
        data.setValue("matches");

        assertThat(predicate.apply(data)).isTrue();
    }

    @Test
    public void should_not_accept_url_not_accessing_bonita_api() throws Exception {
        data.setType(URL);
        data.setValue("do not match");

        assertThat(predicate.apply(data)).isFalse();
    }

    @Test
    public void should_not_accept_a_constant_data() throws Exception {
        data.setType(CONSTANT);
        data.setValue("matches");

        assertThat(predicate.apply(data)).isFalse();
    }

    @Test
    public void should_not_accept_an_expression_data() throws Exception {
        data.setType(EXPRESSION);
        data.setValue("matches");

        assertThat(predicate.apply(data)).isFalse();
    }

    @Test
    public void should_not_accept_a_JSON_data() throws Exception {
        data.setType(JSON);
        data.setValue("matches");

        assertThat(predicate.apply(data)).isFalse();
    }
}
