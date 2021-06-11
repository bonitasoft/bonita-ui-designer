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

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;

public class BonitaResourceTransformerTest {

    private BonitaResourceTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new BonitaResourceTransformer("(\\w*)/(\\w*)");
    }

    @Test
    public void should_transform_mathing_two_groups_into_an_authorisation_resource_token() throws Exception {

        String url = transformer.apply(aConstantVariable()
                .value("group1/group2")
                .build());

        assertThat(url).isEqualTo("GET|group1/group2");
    }

    @Test
    public void should_return_an_empty_string_the_url_value_do_not_matches() throws Exception {

        String url = transformer.apply(aConstantVariable()
                .value("/path/to/whatever/resource")
                .build());

        assertThat(url).isEmpty();
    }
}
