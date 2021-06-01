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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

@JsonTypeName("modalContainer")
public class ModalContainer extends Component {


    private String controller;
    private Container container = new Container();

    // We override this id to make import of old pages enable. We do this to fix faster.
    // It would be better to create a migration step for each page or widget
    // This migration step will be migrate all element before 1.4.21 to add id for each type container.
    @Override
    public String getId() {
        return "pbModalContainer";
    }

    @JsonView({JsonViewPersistence.class})
    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ModalContainer) {
            var other = (ModalContainer) obj;
            return new EqualsBuilder()
                    .append(container, other.container)
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
                .append(container)
                .append(getDimension())
                .append(getPropertyValues())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("container", container)
                .append("dimension", getDimension())
                .append("propertyValues", getPropertyValues())
                .toString();
    }

    @JsonView({JsonViewPersistence.class})
    public Container getContainer() {
        return container;
    }

    public ModalContainer setContainer(Container container) {
        this.container = container;
        return this;
    }
}
