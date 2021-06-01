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
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import java.util.List;

/**
 * An element visitor which traverses the tree of elements recursively to find out if a page still contains elements that have a validation error
 */
public class PageHasValidationErrorVisitor {

    public boolean visit(Container container) {
        if (container.getHasValidationError()) {
            return true;
        }
        return traverseRow(container.getRows());
    }

    public boolean visit(FormContainer formContainer) {
        return visit(formContainer.getContainer());
    }

    public boolean visit(ModalContainer modalContainer) {
        return visit(modalContainer.getContainer());
    }

    public boolean visit(TabsContainer tabsContainer) {
        var hasValidationError = false;
        for (var tabContainer : tabsContainer.getTabList()) {
            hasValidationError = hasValidationError || visit(tabContainer.getContainer());
        }
        return hasValidationError;
    }

    public boolean visit(Component component) {
        return component.getHasValidationError();
    }

    public <P extends Previewable & Identifiable> boolean visit(P previewable) {
        return false;
    }

    private boolean traverseRow(List<List<Element>> rows) {
        var hasValidationError = false;
        for (var row : rows) {
            for (var element : row) {
                hasValidationError = hasValidationError || element.getHasValidationError();
            }
        }
        return hasValidationError;
    }
}
