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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import java.util.HashMap;
import java.util.Map;

@JsonTypeName("fragment")
public class FragmentElement extends Element {

    private String id;
    private String description;

    /**
     * Optional fragment data binding to page data.
     * Associates a fragment's data name (map's key), to a page data name (map's value).
     */
    private Map<String, String> binding = new HashMap<>();

    @JsonView({JsonViewPersistence.class})
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonView({JsonViewPersistence.class})
    public Map<String, String> getBinding() {
        return binding;
    }

    public void setBinding(Map<String, String> binding) {
        this.binding = binding;
    }

    @JsonView({JsonViewPersistence.class})
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        if (visitor != null) {
            return visitor.visit(this);
        }
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FragmentElement) {
            final FragmentElement other = (FragmentElement) obj;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(getDimension(), other.getDimension())
                    .append(binding, other.binding)
                    .append(description, other.description)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(getDimension())
                .append(binding)
                .append(description)
                .toHashCode();
    }
}
