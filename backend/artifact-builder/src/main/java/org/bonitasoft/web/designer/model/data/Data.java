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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

public class Data {

    private DataType type;
    private Object value;
    private boolean exposed;

    @JsonCreator
    public Data(@JsonProperty("type") DataType type, @JsonProperty("value") Object value) {
        this.type = type;
        this.value = value;
    }

    @JsonView({JsonViewPersistence.class})
    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @JsonView({JsonViewPersistence.class})
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @JsonView({JsonViewPersistence.class})
    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Data) {
            final Data other = (Data) obj;
            return new EqualsBuilder()
                    .append(type, other.type)
                    .append(value, other.value)
                    .append(exposed, other.exposed)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(value)
                .append(exposed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("type", type).append("value", value).append("exposed", exposed).
                toString();
    }
}
