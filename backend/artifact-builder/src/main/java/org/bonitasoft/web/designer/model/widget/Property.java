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
package org.bonitasoft.web.designer.model.widget;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class Property {

    @NotBlank(message = "Property label should not be blank")
    private String label;
    @NotBlank(message = "Property name should not be blank")
    @Pattern(regexp = "[a-zA-Z0-9]*$", message = "Property name should contains only alphanumeric characters with no space")
    private String name;
    private String caption;
    private String help;
    private String showFor;
    private String patternValidation;
    private PropertyType type;
    private Object defaultValue;
    private List<Object> choiceValues;
    private BondType bond = BondType.EXPRESSION;
    private Map<String, Object> constraints;

    @JsonView({JsonViewPersistence.class})
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonView({JsonViewPersistence.class})
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView({JsonViewPersistence.class})
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @JsonView({JsonViewPersistence.class})
    public String getShowFor() {
        return showFor;
    }

    public void setShowFor(String showFor) {
        this.showFor = showFor;
    }

    @JsonView({JsonViewPersistence.class})
    public String getPatternValidation() {
        return patternValidation;
    }

    public void setPatternValidation(String patternValidation) {
        this.patternValidation = patternValidation;
    }

    @JsonView({JsonViewPersistence.class})
    public PropertyType getType() {
        return type;
    }

    public void setType(PropertyType type) {
        this.type = type;
    }

    @JsonView({JsonViewPersistence.class})
    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @JsonView({JsonViewPersistence.class})
    public List<Object> getChoiceValues() {
        return choiceValues;
    }

    public void setChoiceValues(List<Object> choiceValues) {
        this.choiceValues = choiceValues;
    }

    @JsonView({JsonViewPersistence.class})
    public BondType getBond() {
        return bond;
    }

    public void setBond(BondType bond) {
        this.bond = bond;
    }

    @JsonView({JsonViewPersistence.class})
    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Property) {
            var other = (Property) obj;
            return new EqualsBuilder()
                    .append(label, other.label)
                    .append(name, other.name)
                    .append(caption, other.caption)
                    .append(showFor, other.showFor)
                    .append(patternValidation, other.patternValidation)
                    .append(type, other.type)
                    .append(defaultValue, other.defaultValue)
                    .append(choiceValues, other.choiceValues)
                    .append(bond, other.bond)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(label)
                .append(name)
                .append(caption)
                .append(showFor)
                .append(patternValidation)
                .append(type)
                .append(defaultValue)
                .append(choiceValues)
                .append(bond)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("label", label)
                .append("name", name)
                .append("caption", caption)
                .append("showFor", showFor)
                .append("patternValidation", patternValidation)
                .append("type", type)
                .append("defaultValue", defaultValue)
                .append("choiceValues", choiceValues)
                .append("bond", bond)
                .toString();
    }
}
