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
package org.bonitasoft.web.designer.model.contract;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractContractInput implements ContractInput {

    private ContractInput parentInput;
    private boolean mandatory;
    private boolean multiple;
    private String description;
    private String name;
    private DataReference dataReference;
    private EditMode mode = EditMode.CREATE;
    private boolean readOnly;

    public AbstractContractInput(String name) {
        this.name = name;
    }

    public void setDataReference(DataReference dataReference) {
        this.dataReference = dataReference;
    }

    public DataReference getDataReference() {
        return dataReference;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public void setMode(EditMode mode) {
        this.mode = mode;
    }

    @Override
    public EditMode getMode() {
        return this.mode;
    }

    @JsonIgnore
    @Override
    public ContractInput getParent() {
        return parentInput;
    }

    @Override
    @JsonIgnore
    public void setParent(ContractInput parentInput) {
        this.parentInput = parentInput;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadonly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
