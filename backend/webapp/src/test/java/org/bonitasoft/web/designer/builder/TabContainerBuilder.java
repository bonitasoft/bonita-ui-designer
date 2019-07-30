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

import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.TabContainer;

public class TabContainerBuilder extends ElementBuilder<TabContainer>{

    private TabContainer tabContainer = new TabContainer();

    private TabContainerBuilder() {
    }

    public static TabContainerBuilder aTabContainer() {
        return new TabContainerBuilder();
    }

    public TabContainer build() {
        return tabContainer;
    }

    public TabContainerBuilder withId(String id) {
        tabContainer.setId(id);
        return this;
    }

    public TabContainerBuilder with(ElementBuilder<Container> builder) {
        tabContainer.setContainer(builder.build());
        return this;
    }

    public TabContainerBuilder with(Container container) {
        tabContainer.setContainer(container);
        return this;
    }

    @Override
    protected TabContainer getElement() {
        return tabContainer;
    }
}
