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
package org.bonitasoft.web.designer.generator.mapping.data;

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.page.PageData;
import org.bonitasoft.web.designer.model.contract.Contract;

import java.io.IOException;


public class FormInputData implements PageData {

    public static final String INPUT_NAME = "formInput";

    private final JsonHandler jsonHandler;
    private final Contract contract;

    public FormInputData(JsonHandler jsonHandler, Contract contract) {
        this.jsonHandler = jsonHandler;
        this.contract = contract;
    }

    @Override
    public String name() {
        return INPUT_NAME;
    }

    @Override
    public Data create() {
        var formInputVisitor = new FormInputVisitor(jsonHandler);
        contract.accept(formInputVisitor);
        try {
            return new Data(DataType.JSON, formInputVisitor.toJson());
        } catch (IOException e) {
            return new Data(DataType.JSON, "{}");
        }
    }
}
