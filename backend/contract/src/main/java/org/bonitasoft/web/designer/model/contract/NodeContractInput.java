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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NodeContractInput extends AbstractContractInput {

    private List<ContractInput> children = new ArrayList<>();

    public NodeContractInput(String name) {
        super(name);
    }

    @Override
    public BusinessDataReference getDataReference() {
        return (BusinessDataReference) super.getDataReference();
    }

    @Override
    public String getType() {
        return NodeContractInput.class.getName();
    }

    @Override
    public List<ContractInput> getInput() {
        return children;
    }

    @Override
    @JsonIgnore
    public void addInput(ContractInput input) {
        input.setParent(this);
        children.add(input);
    }

    @JsonIgnore
    @Override
    public void accept(ContractInputVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setType(String classname) {
        throw new UnsupportedOperationException();
    }

}
