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

import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;

import java.util.regex.Matcher;

import com.google.common.base.Function;
import org.bonitasoft.web.designer.model.data.Data;

class BonitaResourceTransformer implements Function<Data, String> {

    private String bonitaResourceRegex;

    public BonitaResourceTransformer(String bonitaResourceRegex) {
        this.bonitaResourceRegex = bonitaResourceRegex;
    }

    @Override
    public String apply(Data data) {
        Matcher api = compile(bonitaResourceRegex).matcher(valueOf(data.getValue()));
        return api.matches() ? "GET|" + api.group(1) + "/" + api.group(2) : "";
    }
}
