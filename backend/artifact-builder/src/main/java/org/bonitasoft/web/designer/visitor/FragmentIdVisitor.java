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
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.repository.FragmentRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * An element visitor which traverses the tree of elements recursively to collect all the fragment IDs used in a page
 *
 * @author Colin Puy
 */
@RequiredArgsConstructor
public class FragmentIdVisitor implements ElementVisitor<Set<String>> {

    private final FragmentRepository fragmentRepository;

    @Override
    public Set<String> visit(Container container) {
        return getFragmentIdsFrom(container.getRows());
    }

    @Override
    public Set<String> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(TabsContainer tabsContainer) {
        Set<String> fragmentIds = new HashSet<>();
        for (var tabContainer : tabsContainer.getTabList()) {
            fragmentIds.addAll(tabContainer.accept(this));
        }
        return fragmentIds;
    }

    @Override
    public Set<String> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(FragmentElement fragmentElement) {
        var fragmentIds = getFragmentIdsFrom(fragmentRepository.get(fragmentElement.getId()).getRows());
        fragmentIds.add(fragmentElement.getId());
        return fragmentIds;
    }

    @Override
    public Set<String> visit(Component component) {
        return emptySet();
    }

    @Override
    public Set<String> visit(Previewable previewable) {
        return getFragmentIdsFrom(previewable.getRows());
    }

    private Set<String> getFragmentIdsFrom(List<List<Element>> rows) {
        var fragmentIds = new HashSet<String>();
        for (var row : rows) {
            for (var element : row) {
                fragmentIds.addAll(element.accept(this));
            }
        }
        return fragmentIds;
    }
}
