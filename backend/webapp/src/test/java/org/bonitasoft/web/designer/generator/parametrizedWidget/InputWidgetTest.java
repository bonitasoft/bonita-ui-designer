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
package org.bonitasoft.web.designer.generator.parametrizedWidget;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.ParameterType.INTERPOLATION;

public class InputWidgetTest {

    @Test
    public void should_create_a_component_with_an_interpolated_label_property_value() {
        InputWidget input = new InputWidget();

        assertThat(input.toComponent(new DimensionFactory()).getPropertyValues().get("label").getType()).isEqualTo(INTERPOLATION.getValue());
    }
}
