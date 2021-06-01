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
package org.bonitasoft.web.designer.model.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static java.lang.String.format;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
public abstract class Element {

    private Map<String, Integer> dimension = new TreeMap<>();
    private Map<String, PropertyValue> propertyValues = new HashMap<>();
    private String reference;

    private boolean hasValidationError = false;

    protected Element() {
        this.dimension.put("xs", 12);
    }

    @JsonView({JsonViewPersistence.class})
    public String getReference() {
        if (reference == null) {
            // Reference generated for the life time of the object.
            reference = UUID.randomUUID().toString();
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, Integer> getDimension() {
        return dimension;
    }

    public void setDimension(Map<String, Integer> dimension) {
        this.dimension = dimension;
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(Map<String, PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }

    /**
     * Accepts the visit of the given visitor
     */
    public abstract <T> T accept(ElementVisitor<T> visitor);

    @JsonIgnore
    public String getDimensionAsCssClasses() {
        var classes = new StringBuilder();
        for (Map.Entry<String, Integer> entry : dimension.entrySet()) {
            classes.append(stringifyColumn(entry.getKey(), entry.getValue())).append(" ");
        }
        return classes.toString().trim();
    }

    private String stringifyColumn(String prefix, Integer size) {
        return size == null ? "" : format("col-%s-%d ", prefix, size);
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public boolean getHasValidationError() {
        return hasValidationError;
    }

    public void setHasValidationError(boolean hasValidationError) {
        this.hasValidationError = hasValidationError;
    }
}
