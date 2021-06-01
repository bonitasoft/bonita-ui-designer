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
package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.visitor.FixBondsTypesVisitor;

import java.util.List;

public class BondsTypesFixer<T extends Identifiable & Previewable> {

    private final Repository<T> repository;

    public BondsTypesFixer(Repository<T> repository) {
        this.repository = repository;
    }

    public void fixBondsTypes(String widgetId, List<Property> properties) {
        List<T> artifactsUsingWidget = repository.findByObjectId(widgetId);
        var fixBondsTypesVisitor = new FixBondsTypesVisitor(properties);
        for (var artifactUsingWidget : artifactsUsingWidget) {
            fixBondsTypesVisitor.visit(artifactUsingWidget);
            repository.save(artifactUsingWidget);
        }
    }
}
