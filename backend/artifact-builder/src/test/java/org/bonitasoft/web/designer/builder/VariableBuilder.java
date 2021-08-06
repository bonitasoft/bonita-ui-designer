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

import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;

import static org.bonitasoft.web.designer.model.data.DataType.*;

public class VariableBuilder {

    private DataType type;
    private String value;
    private boolean exposed;

    public VariableBuilder(DataType type) {
        this.type = type;
    }

    public static VariableBuilder aConstantVariable() {
        return new VariableBuilder(CONSTANT);
    }

    public static VariableBuilder aJSONVariable() {
        return new VariableBuilder(JSON);
    }

    public static VariableBuilder anURLVariable() {
        return new VariableBuilder(URL);
    }

    public static VariableBuilder aUrlParameterVariable() {
        return new VariableBuilder(DataType.URLPARAMETER);
    }

    public static VariableBuilder anExpressionVariable() {
        return new VariableBuilder(EXPRESSION);
    }
    public static VariableBuilder aBusinessDataVariable() {
        return new VariableBuilder(BUSINESSDATA);
    }

    public VariableBuilder value(String value) {
        this.value = value;
        return this;
    }

    public VariableBuilder exposed(boolean exposed) {
        this.exposed = exposed;
        return this;
    }

    public Variable build() {
        Variable variable = new Variable(type, value);
        variable.setExposed(exposed);
        return variable;
    }
}
