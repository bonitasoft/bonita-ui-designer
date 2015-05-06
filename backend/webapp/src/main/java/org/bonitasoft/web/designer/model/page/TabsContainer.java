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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

@JsonTypeName("tabsContainer")
public class TabsContainer extends Element {

    private List<Tab> tabs;

    @JsonView({JsonViewPersistence.class})
    public List<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }

    @Override
    public <T> T accept(ElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TabsContainer) {
            final TabsContainer other = (TabsContainer) obj;
            return new EqualsBuilder()
                    .append(tabs, other.tabs)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(tabs)
                .toHashCode();
    }
}
