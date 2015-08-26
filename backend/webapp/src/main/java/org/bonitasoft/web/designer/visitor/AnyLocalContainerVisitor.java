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
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.Tab;
import org.bonitasoft.web.designer.model.page.TabsContainer;

/**
 * A visitor
 */
public class AnyLocalContainerVisitor implements ElementVisitor<Iterable<Element>> {

    @Override
    public Iterable<Element> visit(Container container) {
        // transform a list of list of elements into a list of components by visiting element contents
        return concat(singletonList(container), traverse(concat(container.getRows())));
    }

    @Override
    public Iterable<Element> visit(FormContainer formContainer) {
        return concat(singletonList(formContainer), formContainer.getContainer().accept(this));
    }

    @Override
    public Iterable<Element> visit(TabsContainer tabsContainer) {
        return concat(
                singletonList(tabsContainer),
                concat(transform(tabsContainer.getTabs(), new Function<Tab, Iterable<Element>>() {

                    @Override
                    public Iterable<Element> apply(Tab tab) {
                        return tab.getContainer().accept(AnyLocalContainerVisitor.this);
                    }
                })));
    }

    @Override
    public Iterable<Element> visit(Component component) {
        return emptyList();
    }

    @Override
    public Iterable<Element> visit(Previewable previewable) {
        return traverse(concat(previewable.getRows()));
    }

    private Iterable<Element> traverse(Iterable<Element> elements) {
        return concat(transform(elements, new Function<Element, Iterable<Element>>() {

            @Override
            public Iterable<Element> apply(Element element) {
                return element.accept(AnyLocalContainerVisitor.this);
            }
        }));
    }
}
