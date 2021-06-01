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
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.page.PropertyValue;

public class PropertyValueBuilder {

    private PropertyValue propertyValue;

    public static PropertyValueBuilder aPropertyValue() {
        return new PropertyValueBuilder(new PropertyValue());
    }

    public static PropertyValue aConstantPropertyValue(Object value) {
        return new PropertyValueBuilder(new PropertyValue()).withType(ParameterType.CONSTANT).withValue(value).build();
    }

    public static PropertyValue aDataPropertyValue(Object value) {
        return new PropertyValueBuilder(new PropertyValue()).withType(ParameterType.VARIABLE).withValue(value).build();
    }

    public static PropertyValue anExpressionPropertyValue(Object value) {
        return new PropertyValueBuilder(new PropertyValue()).withType(ParameterType.EXPRESSION).withValue(value).build();
    }

    public static PropertyValue aInterpolationPropertyValue(Object value) {
        return new PropertyValueBuilder(new PropertyValue()).withType(ParameterType.INTERPOLATION).withValue(value).build();
    }

    public PropertyValueBuilder withType(ParameterType type) {
        propertyValue.setType(type.getValue());
        return this;
    }

    public PropertyValueBuilder withValue(Object value) {
        this.propertyValue.setValue(value);
        return this;
    }

    private PropertyValueBuilder(PropertyValue value) {
        this.propertyValue = value;
    }

    public PropertyValue build() {
        return propertyValue;
    }

}
