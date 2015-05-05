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
package org.bonitasoft.web.designer.builder;

import java.util.ArrayList;

import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;

public class TabsContainerBuilder extends ElementBuilder<TabsContainer> {

    TabsContainer tabsContainer = new TabsContainer();

    private TabsContainerBuilder() {
        tabsContainer.setTabs(new ArrayList<Tab>());
    }

    public static TabsContainerBuilder aTabsContainer() {
        return new TabsContainerBuilder();
    }

    public TabsContainerBuilder with(TabBuilder... builders) {
        for (TabBuilder builder : builders) {
            tabsContainer.getTabs().add(builder.build());
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
