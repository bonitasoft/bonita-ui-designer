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
package org.bonitasoft.web.designer.utils.assertions;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Element;

/**
 * Custom assertions entry point
 *
 * @author Colin PUY
 */
public class CustomAssertions extends Assertions {

    public static Element toElement(String html) {
        return HtmlAssert.toElement(html);
    }

    public static HtmlAssert assertThatHtml(String html) {
        return new HtmlAssert(toElement(html));
    }

    public static HtmlAssert assertThat(Element element) {
        return new HtmlAssert(element);
    }

    public static JsAssert assertThatJs(String actual) {
        return new JsAssert(actual);
    }

    public static PathAssert assertThat(Path actual) {
        return new PathAssert(actual);
    }
}
