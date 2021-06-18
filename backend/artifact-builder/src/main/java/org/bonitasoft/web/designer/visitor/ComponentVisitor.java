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

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.StreamUtils;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.repository.FragmentRepository;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the components used in a page
 */
@RequiredArgsConstructor
public class ComponentVisitor implements ElementVisitor<Iterable<Component>> {

    private final FragmentRepository fragmentRepository;

    @Override
    public Iterable<Component> visit(FragmentElement fragmentElement) {
        return fragmentRepository.get(fragmentElement.getId()).accept(this);
    }

    @Override
    public Iterable<Component> visit(Container container) {
        // transform a list of list of elements into a list of components by visiting element contents
        return container.getRows().stream().flatMap(Collection::stream)
                .map(element -> element.accept(ComponentVisitor.this))
                .flatMap(StreamUtils::toStream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Component> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Iterable<Component> visit(TabsContainer tabsContainer) {
        return tabsContainer.getTabList().stream()
                .map(tabContainer -> tabContainer.accept(ComponentVisitor.this))
                .flatMap(StreamUtils::toStream)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Component> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Iterable<Component> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Iterable<Component> visit(Component component) {
        return singleton(component);
    }

    @Override
    public Iterable<Component> visit(Previewable previewable) {
        var container = new Container();
        container.setRows(previewable.getRows());
        return container.accept(this);
    }

}
