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

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlGeneratorTest {

    @Mock
    private HtmlBuilderVisitor htmlBuilderVisitor;

    @InjectMocks
    private HtmlGenerator generator;

    @Test
    public void should_generate_formatted_html_with_given_widgets() throws Exception {
        Page page = aPage().build();
        when(htmlBuilderVisitor.build(page, "mycontext/")).thenReturn("foobar");

        String generateHtml = generator.generateHtml(page, "mycontext/");

        assertThat(generateHtml).isEqualTo(format("foobar"));
    }

    @Test
    public void should_generate_formatted_html_with_no_context() throws Exception {
        Page page = aPage().build();
        when(htmlBuilderVisitor.build(page, "")).thenReturn("foobar");

        String generateHtml = generator.generateHtml(page);

        assertThat(generateHtml).isEqualTo(format("foobar"));
    }

    @Test
    public void should_generate_formatted_html_for_fragment_with_given_widgets() throws Exception {
        Fragment fragment = aFragment().build();
        when(htmlBuilderVisitor.build(fragment, "mycontext/")).thenReturn("foobar");

        String generateHtml = generator.generateHtml(fragment, "mycontext/");

        assertThat(generateHtml).isEqualTo(format("foobar"));
    }

    private String format(String html) {
        return Jsoup.parse(html).toString();
    }
}
