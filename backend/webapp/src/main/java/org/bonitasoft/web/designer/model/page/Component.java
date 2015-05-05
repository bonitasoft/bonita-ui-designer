/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.model.page;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

@JsonTypeName("component")
public class Component extends Element {

    private String widgetId;

    @JsonView({JsonViewPersistence.class})
    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Component) {
            final Component other = (Component) obj;
            return new EqualsBuilder()
                    .append(widgetId, other.widgetId)
                    .append(getPropertyValues(), other.getPropertyValues())
                    .append(getDimension(), other.getDimension())
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(widgetId)
                .append(getPropertyValues())
                .append(getDimension())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("widgetId", widgetId)
                .append("propertyValues", getPropertyValues())
                .append("dimension", getDimension())
                .toString();
    }
}
