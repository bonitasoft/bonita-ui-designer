/** 
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.nio.charset.StandardCharsets;

import inconspicuous.jsmin.JSMin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class Minifier {

    public static byte[] minify(byte[] contentToMinify) {
        String jsmin;
        try {
            jsmin = JSMin.minify(new String(contentToMinify, StandardCharsets.UTF_8));
            return jsmin.getBytes(StandardCharsets.UTF_8);
        } catch (JSMin.MinifyException e) {
            log.warn("Something went wrong when minifying JS content. Non minified content will be packaged instead.",
                    e);
            return contentToMinify;
        }
    }
}
