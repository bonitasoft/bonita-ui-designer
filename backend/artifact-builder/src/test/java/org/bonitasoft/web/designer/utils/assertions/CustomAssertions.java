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

import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Element;

/**
 * Custom assertions entry point
 *
 * @author Colin PUY
 */
public class CustomAssertions extends Assertions {

    public static Element toBody(String html) {
        return HtmlAssert.toBody(html);
    }

    public static Element toHead(String html) {
        return HtmlAssert.toHead(html);
    }

    public static HtmlAssert assertThatHtmlBody(String html) {
        return new HtmlAssert(toBody(html));
    }

    public static HtmlAssert assertThatHtmlHead(String html) {
        return new HtmlAssert(toHead(html));
    }

    public static HtmlAssert assertThat(Element element) {
        return new HtmlAssert(element);
    }

    public static JsAssert assertThatJs(String actual) {
        return new JsAssert(actual);
    }
}
