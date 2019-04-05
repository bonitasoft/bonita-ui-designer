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
package org.bonitasoft.web.designer.experimental.parametrizedWidget;

import java.util.Map.Entry;
import java.util.Objects;

public class CheckboxWidget extends AbstractParametrizedWidget implements Valuable {

    private static final String READ_ONLY_PROPERTY_NAME = "disabled";

    private Integer dimension = 12;
    private String label;
    private String value;

    @Override
    public String getWidgetId() {
        return "pbCheckbox";
    }

    @Override
    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    @Override
    public Integer getDimension() {
        return dimension;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected String getEntryKey(Entry<String, Object> entry) {
        String originalKey = entry.getKey();
        if (Objects.equals(originalKey, ParameterConstants.READONLY_PARAMETER)) {
            return READ_ONLY_PROPERTY_NAME;
        }
        return originalKey;
    }

}
