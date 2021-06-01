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
package org.bonitasoft.web.designer.generator.mapping.data;

import java.util.stream.Stream;

public class StringUtil {

    private StringUtil() {
        // Utility class
    }

    public static String indent(String value, int size) {
        var sb = new StringBuilder();
        boolean appendNewLine = value.endsWith("\n");
        String[] lines = value.split("\n");
        Stream.of(lines).forEach(line -> {
            var lineBuilder = new StringBuilder(line);
            for (var i = 0; i < size; i++) {
                lineBuilder.insert(0, "\t");
            }
            line = lineBuilder.toString();
            sb.append(line);
            sb.append("\n");
        });
        var indentedValue = sb.toString();
        return !appendNewLine ? indentedValue.substring(0, indentedValue.length() - 1) : indentedValue;
    }

}
