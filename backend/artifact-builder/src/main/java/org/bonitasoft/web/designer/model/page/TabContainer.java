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
import org.bonitasoft.web.designer.model.widget.BondType;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

@JsonTypeName("tabContainer")
public class TabContainer extends Component {

    private Container container = new Container();

    @Override
    public String getId() {
        return "pbTabContainer";
    }

    @JsonView({JsonViewPersistence.class})
    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TabContainer) {
            var other = (TabContainer) obj;
            return new EqualsBuilder()
                    .append(container, other.container)
                    .isEquals();
        } else {
            return false;
        }
    }

    public TabContainer convert(Tab tab) {
        // Migrate Tab to TabContainer
        this.setContainer(tab.getContainer());

        var propertyValue = new PropertyValue();
        propertyValue.setType(BondType.INTERPOLATION.toJson());
        propertyValue.setValue(tab.getTitle());

        this.getPropertyValues().put("title", propertyValue);

        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(container)
                .toHashCode();
    }
}
