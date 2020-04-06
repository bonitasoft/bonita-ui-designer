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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObject;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObjectAttribute;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObjectAttributeType;

public class BusinessObjectBuilder {

    private String name;
    private String variableName;
    private List<BusinessObjectAttribute> attributes = new ArrayList<>();


    private BusinessObjectBuilder() {
    }

    public static BusinessObjectBuilder aBusinessObject() {
        return new BusinessObjectBuilder();
    }

    public static BusinessObjectBuilder aBusinessObject(String name) {
        BusinessObjectBuilder bo = new BusinessObjectBuilder();
        bo.withName(name);
        return bo;
    }

    public BusinessObjectBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public BusinessObjectBuilder withVariableName(String variableName) {
        this.variableName = variableName;
        return this;
    }

    public BusinessObjectBuilder withAttributes(BusinessObjectAttribute... attributes) {
        for (BusinessObjectAttribute boa : attributes) {
            this.attributes.add(boa);
        }
        return this;
    }

    public BusinessObjectBuilder withAttributes(String name, String type, String scalar){
        BusinessObjectAttribute boa = new BusinessObjectAttribute(name, type);
        this.attributes.add(boa);
        return this;
    }

    public BusinessObject build() {
        BusinessObject bo = new BusinessObject();
        bo.setName(name);
        bo.setVariableName(variableName);
        bo.setAttributes(attributes);
        return bo;
    }


}

