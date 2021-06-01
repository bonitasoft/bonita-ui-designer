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
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import java.util.ArrayList;

public class TabsContainerBuilder extends ElementBuilder<TabsContainer> {

    private TabsContainer tabsContainer;
    private String id = "pbTabsContainer";

    private TabsContainerBuilder() {
        tabsContainer = new TabsContainer();
        tabsContainer.setId(id);
        tabsContainer.setTabList(new ArrayList<TabContainer>());
    }

    public static TabsContainerBuilder aTabsContainer() {
        return new TabsContainerBuilder();
    }

    public TabsContainerBuilder with(TabContainer... tabContainers) {
        for (TabContainer tabContainer : tabContainers) {
            tabsContainer.getTabList().add(tabContainer);
        }
        return this;
    }

    public TabsContainerBuilder with(TabContainerBuilder... builders) {
        for (TabContainerBuilder builder : builders) {
            tabsContainer.getTabList().add(builder.build());
        }
        return this;
    }

    @Override
    protected TabsContainer getElement() {
        return tabsContainer;
    }

    public TabsContainer build() {
        return tabsContainer;
    }
}
