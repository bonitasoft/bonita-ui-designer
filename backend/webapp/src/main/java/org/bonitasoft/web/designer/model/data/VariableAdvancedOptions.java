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
package org.bonitasoft.web.designer.model.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

@JsonFilter("valueAsArray")
public class VariableAdvancedOptions {

    //Bind to a UI-Designer to store information at runtime
    private String headers;
    private String statusCode;

    @JsonCreator
    public VariableAdvancedOptions(@JsonProperty("headers") String  headers, @JsonProperty("statusCode") String statusCode) {
        this.headers = headers;
        this.statusCode = statusCode;
    }

    @JsonView({JsonViewPersistence.class})
    public String getHeaders() {
        return headers;
    }

    public void setType(String headers) {
        this.headers = headers;
    }

    @JsonView({JsonViewPersistence.class})
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("headers", headers).append("statusCode", statusCode).toString();
    }
}
