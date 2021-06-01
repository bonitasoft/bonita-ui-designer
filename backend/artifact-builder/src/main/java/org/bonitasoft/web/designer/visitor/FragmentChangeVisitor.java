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

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * An element visitor which traverses the tree of elements recursively to collect rename reference of fragment
 *
 * @author Benjamin Parisel
 */
public class FragmentChangeVisitor implements ElementVisitor<Set<String>> {

    private String newFragmentId;

    private String fragmentToReplace;

    @Override
    public Set<String> visit(Container container) {
        visitRows(container.getRows());
        return emptySet();
    }

    @Override
    public Set<String> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(TabsContainer tabsContainer) {
        for (var tabContainer : tabsContainer.getTabList()) {
            tabContainer.accept(this);
        }
        return emptySet();
    }

    @Override
    public Set<String> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Set<String> visit(Component component) {
        return emptySet();
    }

    @Override
    public <P extends Previewable & Identifiable> Set<String> visit(P previewable) {
        return emptySet();
    }

    @Override
    public Set<String> visit(FragmentElement fragmentElement) {
        if (fragmentToReplace.equals(fragmentElement.getId())) {
            fragmentElement.setId(newFragmentId);
        }
        return emptySet();
    }

    public void setNewFragmentId(String newFragmentId) {
        this.newFragmentId = newFragmentId;
    }

    public void setFragmentToReplace(String fragmentToReplace) {
        this.fragmentToReplace = fragmentToReplace;
    }

    public void visitRows(List<List<Element>> rows) {
        rows.forEach(row -> {
            row.stream().filter(Container.class::isInstance)
                    .forEach(compo -> {
                        Container component = (Container) compo;
                        visit(component);
                    });
            row.stream().filter(FormContainer.class::isInstance)
                    .forEach(compo -> {
                        var component = (FormContainer) compo;
                        visit(component);
                    });
            row.stream().filter(TabsContainer.class::isInstance)
                    .forEach(compo -> {
                        var component = (TabsContainer) compo;
                        visit(component);
                    });
            row.stream().filter(ModalContainer.class::isInstance)
                    .forEach(compo -> {
                        var component = (ModalContainer) compo;
                        visit(component);
                    });
            row.stream().filter(FragmentElement.class::isInstance)
                    .forEach(compo -> {
                        var component = (FragmentElement) compo;
                        visit(component);
                    });
        });
    }

}
