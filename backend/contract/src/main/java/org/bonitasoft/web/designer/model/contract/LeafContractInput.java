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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LeafContractInput extends AbstractContractInput {

    private String type;

    public LeafContractInput(String name, Class<?> type) {
        super(name);
        this.type = type.getName();
    }

    @Override
    public String getType() {
        return type;
    }

    @JsonIgnore
    @Override
    public void accept(ContractInputVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public List<ContractInput> getInput() {
        return Collections.emptyList();
    }

    @Override
    public void addInput(ContractInput childInput) {
        throw new IllegalStateException("Cannot add a child input to a leaf input.");
    }

    @Override
    public void setType(String classname) {
        type = classname;
    }

}
