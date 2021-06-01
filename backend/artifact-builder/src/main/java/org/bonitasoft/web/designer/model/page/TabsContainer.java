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

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("tabsContainer")
public class TabsContainer extends Component {

    @Deprecated
    private List<Tab> tabs;

    private List<TabContainer> tabList = new ArrayList<>();

    // We override this id to make import of old pages enable. We do this to fix faster.
    // It would be better to create a migration step for each page or widget
    // This migration step will be migrate all element before 1.4.21 to add id for each type tabsContainer.
    @Override
    public String getId() {
        return "pbTabsContainer";
    }

    @Deprecated
    @JsonView({JsonViewPersistence.class})
    public List<Tab> getTabs() {
        return tabs;
    }

    @Deprecated
    public void setTabs(List<Tab> tabs) {
        tabs.forEach(tab -> {
            var newTabContainer = new TabContainer();
            this.tabList.add(newTabContainer.convert(tab));
        });
    }

    @JsonView({JsonViewPersistence.class})
    public List<TabContainer> getTabList() {
        return tabList;
    }

    public void setTabList(List<TabContainer> tabList) {
        this.tabList = tabList;
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
