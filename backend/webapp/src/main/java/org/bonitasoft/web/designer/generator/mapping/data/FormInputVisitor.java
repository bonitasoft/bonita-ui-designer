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
import org.bonitasoft.web.designer.model.contract.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.EMPTY_LIST;

public class FormInputVisitor implements ContractInputVisitor {

    private static final Map<String, Object> defaultValues = new HashMap<>();

    static {
        defaultValues.put(String.class.getName(), "");
        defaultValues.put(Boolean.class.getName(), false);
        defaultValues.put(Integer.class.getName(), 0);
        defaultValues.put(Double.class.getName(), 0);
        defaultValues.put(Float.class.getName(), 0);
        defaultValues.put(Long.class.getName(), 0);
        defaultValues.put(Date.class.getName(), null);
        defaultValues.put(LocalDate.class.getName(), null);
        defaultValues.put(LocalDateTime.class.getName(), null);
        defaultValues.put(OffsetDateTime.class.getName(), null);
        defaultValues.put(File.class.getName(), null);
    }

    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final JsonHandler jsonHandler;

    public FormInputVisitor(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Override
    public void visit(NodeContractInput contractInput) {
        if (contractInput.getMode() == EditMode.CREATE
                || contractInput.getDataReference() == null) {
            if (contractInput.isMultiple()) {
                properties.put(contractInput.getName(), EMPTY_LIST);
                return;
            }
            var visitor = new FormInputVisitor(jsonHandler);
            for (ContractInput input : contractInput.getInput()) {
                input.accept(visitor);
            }
            properties.put(contractInput.getName(), visitor.properties);
        }
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        if (contractInput.getMode() == EditMode.EDIT && contractInput.getDataReference() != null) {
            return;
        }
        if (contractInput.isMultiple()) {
            properties.put(contractInput.getName(), EMPTY_LIST);
            return;
        }
        properties.put(contractInput.getName(), defaultValues.get(contractInput.getType()));
    }

    public String toJson() throws IOException {
        return jsonHandler.prettyPrint(properties);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }
}
