/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.model.contract;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractContractInput implements ContractInput {

    private ContractInput parentInput;
    private boolean mandatory;
    private boolean multiple;
    private String description;
    private String name;

    public AbstractContractInput(String name) {
        this.name = name;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @JsonIgnore
    @Override
    public ContractInput getParent() {
        return parentInput;
    }

    @JsonIgnore
    public void setParent(ContractInput parentInput) {
        this.parentInput = parentInput;
    }

    @Override
    public String path() {
        List<String> pathNames = newArrayList();
        pathNames.add(getName());
        ContractInput pInput = getParent();
        while (pInput != null) {
            pathNames.add(pInput.getName());
            pInput = pInput.getParent();
        }
        return pathNames.isEmpty() ? null : on(".").join(reverse(pathNames));
    }
}
