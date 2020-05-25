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
package org.bonitasoft.web.designer.generator.mapping.dataManagement;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Container;

@JsonTypeName("businessObjectContainer")
public class BusinessObjectContainer {

    private Map<String, Variable> businessObjectVariable = new HashMap<>();

    private Container container;

    public BusinessObjectContainer(Container container) {
        this.container = container;
    }

    public BusinessObjectContainer() {
        this.container = new Container();
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, Variable> getBusinessObjectVariable() {
        return businessObjectVariable;
    }

    public void setBusinessObjectVariable(Map<String, Variable> businessObjectVariable) {
        this.businessObjectVariable = businessObjectVariable;
    }

    @JsonView({JsonViewPersistence.class})
    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public void addVariable(String name, Variable value) {
        businessObjectVariable.put(name, value);
    }
}
