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

import java.util.function.Function;
import java.util.regex.Pattern;


public class ResourceURLFunction implements Function<String, String> {

    private final Pattern pattern;
    private final String httpVerb;

    public ResourceURLFunction(String pattern) {
        this(pattern, "GET");
    }

    public ResourceURLFunction(String pattern, String httpVerb) {
        this.httpVerb = httpVerb;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public String apply(String value) {
        var api = pattern.matcher(value);
        return api.matches() ? httpVerb + "|" + api.group(1) + "/" + api.group(2) : "";
    }

    public String applyApi(String value) {
        var api = pattern.matcher(value);
        return api.matches() ? api.group(1) + "/" + api.group(2) : "";
    }

}
