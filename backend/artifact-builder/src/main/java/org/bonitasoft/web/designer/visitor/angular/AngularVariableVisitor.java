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
package org.bonitasoft.web.designer.visitor.angular;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.visitor.angularJS.VariableModelVisitor;

import java.nio.file.Paths;
import java.util.TreeMap;

/**
 * An element visitor which traverses the tree of elements recursively to collect property values in a page
 */
public class AngularVariableVisitor extends VariableModelVisitor {

    public AngularVariableVisitor(FragmentRepository fragmentRepository) {
        super(fragmentRepository);
    }

    public <P extends Previewable & Identifiable> String generate(P previewable) {
        var resources = this.visit(previewable);
        return new TemplateEngine(Paths.get("angular/src/assets/resources.hbs.ts").toString())
                .with("resourceName","propertiesValues")
                .with("resources", resources == null ? null : new TreeMap<>(resources))
                .build(this);
    }
}
