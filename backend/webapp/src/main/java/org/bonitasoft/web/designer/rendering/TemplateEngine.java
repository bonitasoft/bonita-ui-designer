/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.rendering;

import java.io.IOException;
import java.util.HashMap;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;

public class TemplateEngine {

    private Handlebars handlebars;
    private String location;
    private HashMap<String, Object> model = new HashMap<>();

    public TemplateEngine(String template) {
        handlebars = new Handlebars(new ClassPathTemplateLoader("/", ""));
        handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
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
