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

import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.DataType;

import static org.bonitasoft.web.designer.model.data.DataType.*;

public class DataBuilder {

    private DataType type;
    private String value;
    private boolean exposed;

    public DataBuilder(DataType type) {
        this.type = type;
    }

    public static DataBuilder aConstantData() {
        return new DataBuilder(CONSTANT);
    }

    public static DataBuilder aJSONData() {
        return new DataBuilder(JSON);
    }

    public static DataBuilder anURLData() {
        return new DataBuilder(URL);
    }

    public static DataBuilder aUrlParameterData() {
        return new DataBuilder(DataType.URLPARAMETER);
    }

    public static DataBuilder anExpressionData() {
        return new DataBuilder(EXPRESSION);
    }

    public DataBuilder value(String value) {
        this.value = value;
        return this;
    }

    public DataBuilder exposed(boolean exposed) {
        this.exposed = exposed;
        return this;
    }

    public Data build() {
        Data data = new Data(type, value);
        data.setExposed(exposed);
        return data;
    }
}
