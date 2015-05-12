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
package org.bonitasoft.web.designer.visitor;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DirectivesCollectorTest {

    @Mock
    WidgetRepository widgetRepository;

    @Mock
    WidgetIdVisitor widgetIdVisitor;

    @InjectMocks
    DirectivesCollector directivesCollector;

    @Test
    public void should_collect_directives_from_the_preview() throws Exception {
        Page page = aPage().build();
        HashSet<String> widgetIds = new HashSet<>(asList("input", "paragraph"));
        when(widgetIdVisitor.visit(page)).thenReturn(widgetIds);
        when(widgetRepository.getByIds(widgetIds))
                .thenReturn(asList(
                        aWidget().id("input").build(),
                        aWidget().id("paragraph").build()));


        assertThat(directivesCollector.collect(page)).containsOnly(
                "widgets/input/input.js",
                "widgets/paragraph/paragraph.js");
    }
}
