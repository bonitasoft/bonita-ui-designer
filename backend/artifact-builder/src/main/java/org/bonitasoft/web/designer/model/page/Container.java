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
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

@JsonTypeName("container")
public class Container extends Component implements ElementContainer {

    private List<List<Element>> rows = new ArrayList<>();

    // We override this id to make import of old pages enable. We do this to fix faster.
    // It would be better to create a migration step for each page or widget
    // This migration step will be migrate all element before 1.4.21 to add id for each type container.
    @Override
    public String getId() {
        return "pbContainer";
    }

    @JsonView({JsonViewPersistence.class})
    public List<List<Element>> getRows() {
        return rows;
    }

    public void setRows(List<List<Element>> rows) {
        this.rows = rows;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @JsonIgnore
    public boolean isRepeated() {
        return getPropertyValues().containsKey("repeatedCollection")
                && !StringUtils.isEmpty((String) getPropertyValues().get("repeatedCollection").getValue());
    }

    public Container addNewRow(Element element) {
        this.getRows().add(Collections.<Element>singletonList(element));
        return this;
    }

    public Container addNewRow(List<Element> elements) {
        this.getRows().add(elements);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Container) {
            final Container other = (Container) obj;
            return new EqualsBuilder()
                    .append(rows, other.rows)
                    .append(getDimension(), other.getDimension())
                    .append(getPropertyValues(), other.getPropertyValues())
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(rows)
                .append(getDimension())
                .append(getPropertyValues())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("rows", rows)
                .append("dimension", getDimension())
                .append("propertyValues", getPropertyValues())
                .toString();
    }
}
