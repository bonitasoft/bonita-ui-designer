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
package org.bonitasoft.web.designer.utils.assertions;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Objects;

public class JsAssert extends AbstractAssert<JsAssert, String> {

    protected JsAssert(String actual) {
        super(actual, JsAssert.class);
    }

    public JsAssert isEqualTo(String js) {
        Objects.instance().assertEqual(info, trimEachLines(actual), trimEachLines(js));
        return this;
    }

    /**
     * For each string lines we trim them to avoid whitespace issue when comparing strings
     */
    private String trimEachLines(String string) {
        String[] lines = string.split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(StringUtils.trim(line)).append(System.lineSeparator());
        }
        return StringUtils.chop(sb.toString());
    }
}
