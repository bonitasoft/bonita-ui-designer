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
package org.bonitasoft.web.designer.rendering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateEngine {

    private final Handlebars handlebars;
    private final String location;
    private final Map<String, Object> model = new HashMap<>();

    public TemplateEngine(String template) {
        var simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.setFailOnUnknownId(false);
        var objectMapper = new ObjectMapper();
        objectMapper.setFilters(simpleFilterProvider);

        handlebars = new Handlebars(new ClassPathTemplateLoader("/", ""));
        handlebars.registerHelper("json", new Jackson2Helper(objectMapper));
        handlebars.registerHelper("join", StringHelpers.join);
        handlebars.registerHelper("ifequal", IfEqualHelper.INSTANCE);
        handlebars.prettyPrint(true);

        location = "templates/" + template;
    }

    public TemplateEngine with(String key, Object value) {
        model.put(key, value);
        return this;
    }

    public String build(Object context) throws GenerationException {
        try {
            return handlebars
                    .compile(location)
                    .apply(Context.newBuilder(context).combine(model).build());
        } catch (IOException e) {
            throw new GenerationException("Error applying context to template <" + location + ">", e);
        }
    }
}
