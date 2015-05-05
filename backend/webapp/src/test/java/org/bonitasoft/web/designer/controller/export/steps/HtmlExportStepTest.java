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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import javax.servlet.ServletContext;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@RunWith(MockitoJUnitRunner.class)
public class HtmlExportStepTest {

    @Mock
    private HtmlGenerator htmlGenerator;

    @Mock
    private ServletContext servletContext;

    @InjectMocks
    private HtmlExportStep step;

    @Mock
    private Zipper zipper;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Resource resource;

    @Before
    public void setUp() throws Exception {
        when(htmlGenerator.generateHtml(any(Page.class))).thenReturn("");
        when(resource.getURI()).thenReturn(new File("src/test/resources/generator").toURI());
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
    }

    @Test
    public void should_export_webapp_generator_folder() throws Exception {

        step.execute(zipper, aPage().build());

        verify(zipper).addDirectoryToZip(Paths.get(new File("src/test/resources/generator").toURI()), "resources");
    }

    @Test
    public void should_export_generated_html() throws Exception {
        when(htmlGenerator.generateHtml(any(Page.class))).thenReturn("foobar");

        step.execute(zipper, aPage().build());

        verify(zipper).addToZip("foobar".getBytes(), "resources/index.html");
    }
}
