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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonFilter("valueAsArray")
public class Variable {

    private DataType type;
    private String displayValue;
    private boolean exposed;
    private VariableAdvancedOptions advancedOptions;

    @JsonCreator
    public Variable(@JsonProperty("type") DataType type, @JsonProperty("value") List<String> value, @JsonProperty("advancedOptions") VariableAdvancedOptions advancedOptions) {
        this.type = type;
        this.displayValue = getValueAsString(value);
        this.advancedOptions = advancedOptions;
    }

    public Variable(DataType type, String displayValue) {
        this.type = type;
        this.displayValue = displayValue;
    }

    @JsonView({JsonViewPersistence.class})
    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @JsonView({JsonViewPersistence.class})
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<String> getValue() {
        return getValueAsArray(displayValue);
    }

    public void setValue(List<String> value) {
        this.displayValue = getValueAsString(value);
    }

    @JsonView({JsonViewPersistence.class})
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public VariableAdvancedOptions getAdvancedOptions() {
        return this.advancedOptions;
    }

    public void setAdvancedOptions(VariableAdvancedOptions options) {
        this.advancedOptions = options;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    private static List<String> getValueAsArray(String value) {
        return (value != null) ? Arrays.asList(value.split("\\n")) : Collections.emptyList();
    }

    private static String getValueAsString(List<String> value) {
        return (value != null) ? String.join("\n", value) : null;
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
        if (obj instanceof Variable) {
            final Variable other = (Variable) obj;
            return new EqualsBuilder()
                    .append(type, other.type)
                    .append(displayValue, other.displayValue)
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
                .append(displayValue)
                .append(exposed)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("type", type).append("displayValue", displayValue).append("exposed", exposed).
                toString();
    }
}
