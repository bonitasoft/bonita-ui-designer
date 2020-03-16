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
package org.bonitasoft.web.designer.experimental.mapping.dataManagement;

import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class NodeBusinessObjectInput extends NodeContractInput {

    static final String BUSINESS_OBJECT_SELECTED = "_selected";
    /**
     * UID variable name
     */
    private String dataName;

    public NodeBusinessObjectInput(String name) {
        super(name);
    }

    public NodeBusinessObjectInput(String name, String dataName) {
        super(name);
        this.dataName = dataName;
    }

    public boolean hasInput(){
        return !this.getInput().isEmpty();
    }


    public String getDataName() {
        return dataName;
    }

    public String getDataNameSelected() {
        return new StringBuilder(dataName).append(BUSINESS_OBJECT_SELECTED).toString();
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }
}
