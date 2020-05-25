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

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.uncapitalize;
import static org.bonitasoft.web.designer.model.data.DataType.URL;

import java.util.Objects;

import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.data.Data;


public class BusinessQueryData implements PageData {

    private BusinessDataReference businessDataReference;

    public BusinessQueryData(BusinessDataReference businessDataReference) {
        this.businessDataReference = businessDataReference;
    }

    @Override
    public String name() {
        return toSimpleName(businessDataReference.getType()) + "_query";
    }

    private String toSimpleName(String type) {
        return uncapitalize(substringAfterLast(type, "."));
    }

    @Override
    public Data create() {
        return new Data(URL, format("../API/bdm/businessData/%s?q=find&p=0&c=99", businessDataReference.getType()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BusinessQueryData other = (BusinessQueryData) obj;
        return Objects.equals(name(),other.name());
    }
}
