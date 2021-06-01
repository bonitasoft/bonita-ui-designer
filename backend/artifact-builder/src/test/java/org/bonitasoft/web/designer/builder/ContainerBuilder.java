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
import org.bonitasoft.web.designer.model.page.Element;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class ContainerBuilder extends ElementBuilder<Container> {

    private final Container container;

    private ContainerBuilder() {
        container = new Container();
        container.setId("pbContainer");
        container.setReference("container-reference");
    }

    public static ContainerBuilder aContainer() {
        return new ContainerBuilder();
    }

    public ContainerBuilder with(Element... elements) {
        return with(asList(elements));
    }

    public ContainerBuilder with(List<Element> elements) {
        List<List<Element>> rows = new ArrayList<>();
        rows.add(elements);
        container.setRows(rows);
        return this;
    }

    public ContainerBuilder with(ElementBuilder<?>... elements) {
        List<Element> elem = new ArrayList<>();
        for (ElementBuilder<?> elementBuilder : elements) {
            elem.add(elementBuilder.build());
        }
        return with(elem);
    }

    public ContainerBuilder with(RowBuilder... rows) {
        List<List<Element>> containerRows = new ArrayList<>();
        for (RowBuilder rowBuilder : rows) {
            containerRows.add(rowBuilder.build());
        }
        container.setRows(containerRows);
        return this;
    }

    @Override
    public Container build() {
        return container;
    }

    @Override
    public Container getElement() {
        return container;
    }
}
