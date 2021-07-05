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

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import static org.jsoup.internal.StringUtil.normaliseWhitespace;

/**
 * Custom assertj assert to compare html strings using jsoup
 * Also add helper method {@link #toBody(String)} to transform html string into jsoup element
 *
 * @author Colin PUY
 */
public class HtmlAssert extends AbstractAssert<HtmlAssert, Element> {

    private Objects objects = Objects.instance();

    protected HtmlAssert(Element actual) {
        super(actual, HtmlAssert.class);
    }

    protected static Element toHead(String html) {
        return Jsoup.parse(html).head();
    }

    protected static Element toBody(String html) {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, true)); // tag, attribute preserve case
        return parser.parseInput(html, "").body().child(0);
    }

    public HtmlAssert isEqualToBody(String html) {
        objects.assertEqual(info, normalize(actual), normalize(toBody(html)));
        return this;
    }

    public HtmlAssert isEqualToHead(String html) {
        objects.assertEqual(info, normalize(actual), normalize(toHead(html)));
        return this;
    }

    public HtmlAssert hasClass(String... cssClasses) {
        for (String className : cssClasses) {
            if (!actual.hasClass(className)) {
                failWithMessage("Expected element to has class [ %s ] but has [ %s ]", className, actual.classNames());
            }
        }
        return this;
    }

    public HtmlAssert hasClassEqualTo(String cssClasse) {
        if (!actual.className().equals(cssClasse)) {
            failWithMessage("Expected class is [ %s ] but is [ %s ]", cssClasse, actual.className());
        }
        return this;
    }

    public HtmlAssert hasElement(String cssSelector) {
        Elements select = actual.select(cssSelector);
        if (select.size() == 0) {
            failWithMessage("Expected html to have component %s but was not found", cssSelector);
        }
        return this;
    }

    public HtmlAssert element(String cssSelector) {
        return new HtmlAssert(actual.select(cssSelector).first());
    }

    public HtmlAssert hasAttributeValue(String attributeName, String attributeValue) {
        String attr = actual.attr(attributeName);
        if (attr == null) {
            failWithMessage("Expected to have attribute [ %s ] but wasn't found", attributeName);
        } else if (!attr.equals(attributeValue)) {
            failWithMessage("Expected attribute [ %s ] to have value [ %s ] but was [ %s ]", attributeName, attributeValue, attr);
        }
        return this;
    }

    public HtmlAssert hasChild(String html) {
        Elements children = actual.children();
        for (Element element : children) {
            if (normalize(element).equals(normalize(toBody(html)))) {
                return this;
            }
        }
        failWithMessage("Expected element to have child [ %s ]", html);
        return this;
    }

    public HtmlAssert hasTagName(String tagName) {
        if (!actual.tagName().equals(tagName)) {
            failWithMessage("Expected element to have tagName [ %s ] but was [ %s ]", tagName, actual.tagName());
        }
        return this;
    }

    private String normalize(Element element) {
        String html = normaliseWhitespace(element.outerHtml());
        // remove all whitespace between html tags
        return html.replace(" <", "<");
    }

}
