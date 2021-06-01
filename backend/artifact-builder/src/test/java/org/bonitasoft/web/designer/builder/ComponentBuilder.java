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

import org.bonitasoft.web.designer.model.page.Component;

public class ComponentBuilder extends ElementBuilder<Component> {

    private final Component component;

    private ComponentBuilder() {
        component = new Component();
    }

    public static ComponentBuilder aComponent() {
        return new ComponentBuilder();
    }

    public static ComponentBuilder aParagraph() throws Exception {
        return aComponent("paragraph");
    }

    public static ComponentBuilder anInput() throws Exception {
        return aComponent("input");
    }

    public static ComponentBuilder aComponent(String id) throws Exception {
        ComponentBuilder builder = new ComponentBuilder();
        return builder.withWidgetId(id);
    }

    public ComponentBuilder withWidgetId(String id) {
        component.setId(id);
        return this;
    }

    public ComponentBuilder withDescription(String description) {
        component.setDescription(description);
        return this;
    }

    @Override
    protected Component getElement() {
        return component;
    }

    @Override
    public Component build() {
        return component;
    }
}
