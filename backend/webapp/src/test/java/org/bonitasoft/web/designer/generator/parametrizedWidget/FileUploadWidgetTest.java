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

import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FileUploadWidgetTest {

    @Test
    public void should_set_required_property_value_with_correct_property_type() {
        FileUploadWidget widget = new FileUploadWidget();

        Map<String, PropertyValue> values = widget.toPropertyValues();
        assertThat(values).containsKey(ParameterConstants.REQUIRED_PARAMETER);
        assertThat(values.get(ParameterConstants.REQUIRED_PARAMETER))
                .extracting(PropertyValue::getType, PropertyValue::getValue)
                .containsExactly(ParameterType.CONSTANT.getValue(), true);

        String expectedExpression = String.format("!%s.id", ParametrizedWidgetFactory.ITEM_ITERATOR);
        widget.setRequiredExpression(expectedExpression);
        values = widget.toPropertyValues();
        assertThat(values).containsKey(ParameterConstants.REQUIRED_PARAMETER);
        assertThat(values.get(ParameterConstants.REQUIRED_PARAMETER))
                .extracting(PropertyValue::getType, PropertyValue::getValue)
                .containsExactly(ParameterType.EXPRESSION.getValue(), expectedExpression);
    }

}
