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
package org.bonitasoft.web.designer.generator.mapping.dataManagement;

public enum WidgetDescription {
    ROOT_AUTOGENERATE_CONTAINER("Auto generate ui for business object \"%s\""),
    DETAILS_CONTAINER("Display \"%s\" details when a line is selected in the table above"),
    BUSINESS_OBJECT_CONTAINER("\"%s\" object from \"%s\" variable"),
    ATTRIBUTE_MULTIPLE("List all attributes \"%s\" for Business Object \"%s\"");

    private String value;

    WidgetDescription(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String displayValue(String... values){
        return String.format(value,values);
    }

}
