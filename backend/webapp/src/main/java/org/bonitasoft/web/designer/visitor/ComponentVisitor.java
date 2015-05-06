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

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.singleton;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the components used in a page
 */
public class ComponentVisitor implements ElementVisitor<Iterable<Component>> {

    @Override
    public Iterable<Component> visit(Container container) {
        // transform a list of list of elements into a list of components by visiting element contents
        return flatten(transform(flatten(container.getRows()), new Function<Element, Iterable<Component>>() {

            @Override
            public Iterable<Component> apply(Element element) {
                return element.accept(ComponentVisitor.this);
            }
        }));
    }

    @Override
    public Iterable<Component> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Iterable<Component> visit(TabsContainer tabsContainer) {
        return flatten(transform(tabsContainer.getTabs(), new Function<Tab, Iterable<Component>>() {

            @Override
            public Iterable<Component> apply(Tab tab) {
                return tab.getContainer().accept(ComponentVisitor.this);
            }
        }));
    }

    @Override
    public Iterable<Component> visit(Component component) {
        return singleton(component);
    }

    @Override
    public Iterable<Component> visit(Previewable previewable) {
        Container container = new Container();
        container.setRows(previewable.getRows());
        return container.accept(this);
    }

    private <T> Iterable<T> flatten(Iterable<? extends Iterable<? extends T>> iterables) {
        return concat(iterables);
    }
}
