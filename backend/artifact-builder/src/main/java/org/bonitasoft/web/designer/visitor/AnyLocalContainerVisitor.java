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
package org.bonitasoft.web.designer.visitor;


import org.bonitasoft.web.designer.StreamUtils;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Stream.of;

/**
 * A visitor
 */
public class AnyLocalContainerVisitor implements ElementVisitor<Iterable<Element>> {


    @Override
    public Iterable<Element> visit(FragmentElement fragmentElement) {
        // we want only local containers so we don't want to search in fragment associated to fragmentElement
        return emptyList();
    }

    @Override
    public Iterable<Element> visit(Container container) {
        // transform a list of list of elements into a list of components by visiting element contents
        return Stream.concat(
                of(container),
                traverse(container.getRows().stream().flatMap(Collection::stream))
        ).collect(Collectors.toList());
    }

    @Override
    public Iterable<Element> visit(FormContainer formContainer) {
        return Stream.concat(
                of(formContainer),
                of(formContainer.getContainer())
                        .map(container -> container.accept(this))
                        .flatMap(StreamUtils::toStream)
        ).collect(Collectors.toList());
    }

    @Override
    public Iterable<Element> visit(TabsContainer tabsContainer) {
        return Stream.concat(
                of(tabsContainer),
                tabsContainer.getTabList().stream()
                        .map(tabContainer -> tabContainer.accept(this))
                        .flatMap(StreamUtils::toStream)
        ).collect(Collectors.toList());
    }

    @Override
    public Iterable<Element> visit(TabContainer tabContainer) {
        return Stream.concat(
                of(tabContainer),
                of(tabContainer.getContainer())
                        .map(container -> container.accept(this))
                        .flatMap(StreamUtils::toStream)
        ).collect(Collectors.toList());
    }

    @Override
    public Iterable<Element> visit(ModalContainer modalContainer) {
        return Stream.concat(
                of(modalContainer),
                of(modalContainer.getContainer())
                        .map(container -> container.accept(this))
                        .flatMap(StreamUtils::toStream)
        ).collect(Collectors.toList());
    }

    @Override
    public Iterable<Element> visit(Component component) {
        return emptyList();
    }

    @Override
    public Iterable<Element> visit(Previewable previewable) {
        return traverse(
                previewable.getRows().stream().flatMap(Collection::stream)
        ).collect(Collectors.toList());
    }

    private Stream<Element> traverse(Stream<Element> elements) {
        return elements
                .map(element -> element.accept(this))
                .flatMap(StreamUtils::toStream);
    }
}
