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
import org.bonitasoft.web.designer.model.page.Tab;

public class TabBuilder {

    private Tab tab = new Tab();

    private TabBuilder() {
    }

    public static TabBuilder aTab() {
        return new TabBuilder();
    }

    public Tab build() {
        return tab;
    }

    public TabBuilder withId(String id) {
        tab.setId(id);
        return this;
    }

    public TabBuilder withTitle(String title) {
        tab.setTitle(title);
        return this;
    }

    public TabBuilder with(ElementBuilder<Container> builder) {
        tab.setContainer(builder.build());
        return this;
    }

    public TabBuilder with(Container container) {
        tab.setContainer(container);
        return this;
    }
}
