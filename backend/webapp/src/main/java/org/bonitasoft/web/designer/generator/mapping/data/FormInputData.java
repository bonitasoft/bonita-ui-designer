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

import static org.bonitasoft.web.designer.model.data.DataType.JSON;

import java.io.IOException;

import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.data.Data;

public class FormInputData implements PageData {

    public static final String NAME = "formInput";

    private JacksonObjectMapper mapper;
    private Contract contract;

    public FormInputData(JacksonObjectMapper mapper, Contract contract) {
        this.mapper = mapper;
        this.contract = contract;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Data create() {
        FormInputVisitor formInputVisitor = new FormInputVisitor(mapper);
        contract.accept(formInputVisitor);
        try {
            return new Data(JSON, formInputVisitor.toJson());
        } catch (IOException e) {
            return new Data(JSON, "{}");
        }
    }
}
