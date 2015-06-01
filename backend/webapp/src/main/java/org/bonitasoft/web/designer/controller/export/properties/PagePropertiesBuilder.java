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
package org.bonitasoft.web.designer.controller.export.properties;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.filterValues;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;

@Named
public class PagePropertiesBuilder {

    private static final String TEMPLATE_NAME = "page.hbs.properties";
    private static final String BONITA_RESOURCE_REGEX = ".+/API/([^ /]*)/([^ /?]*)[^ /]*";   // matches ..... /API/{}/{}?...
    private TemplateEngine template;
    private ComponentVisitor componentVisitor;

    @Inject
    public PagePropertiesBuilder(ComponentVisitor componentVisitor) {
        this.componentVisitor = componentVisitor;
        template = new TemplateEngine(TEMPLATE_NAME);
    }

    public byte[] build(Page page) throws GenerationException, IOException {

        List<String> resources = newArrayList(transform(
                filterValues(page.getData(), new BonitaResourcePredicate(BONITA_RESOURCE_REGEX)).values(),
                new BonitaResourceTransformer(BONITA_RESOURCE_REGEX)));

        Iterable<Component> components = componentVisitor.visit(page);

        if (any(components, new ConstantPropertyValuePredicate("Start process"))) {
            resources.add("POST|bpm/process");
        }

        if (any(components, new ConstantPropertyValuePredicate("Submit task"))) {
            resources.add("POST|bpm/userTask");
        }

        Map<String, String> context = new HashMap<>();
        context.put("name", page.getName());
        context.put("resources", resources.toString());
        context.put("type", String.valueOf(page.getType()).toLowerCase());

        // Java properties files must be encoded using ISO_8859_1
        return template.build(context).getBytes(StandardCharsets.ISO_8859_1);
    }

}
