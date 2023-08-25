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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DimensionFactoryTest {

    @Test
    public void should_create_dimension_map() throws Exception {
        DimensionFactory dimensionFactory = new DimensionFactory();

        Map<String, Integer> dimensions = dimensionFactory.create(12);

        assertThat(dimensions).containsOnlyKeys("xs", "sm", "md", "lg");
        assertThat(dimensions.get("xs")).isEqualTo(12);
        assertThat(dimensions.get("sm")).isEqualTo(12);
        assertThat(dimensions.get("md")).isEqualTo(12);
        assertThat(dimensions.get("lg")).isEqualTo(12);
    }
}
