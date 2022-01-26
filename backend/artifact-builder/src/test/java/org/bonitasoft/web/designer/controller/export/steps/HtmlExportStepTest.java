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

import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;

import static java.nio.file.Paths.get;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_DIRECTORIES;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class HtmlExportStepTest {

    @Mock
    private HtmlGenerator htmlGenerator;

    @InjectMocks
    private HtmlExportStep step;

    @Mock
    private Zipper zipper;

    @Mock
    private WorkspaceUidProperties workspaceUidProperties;


    @Before
    public void setUp() throws Exception {
        when(workspaceUidProperties.getExportBackendResourcesPath()).thenReturn(Paths.get("src/test/resources/"));
        when(htmlGenerator.generateHtml(any(Page.class))).thenReturn("");
    }

    @Test
    public void should_export_webapp_generator_folder() throws Exception {

        step.execute(zipper, aPage().build());

        verify(zipper).addDirectoryToZip(get(new File("src/test/resources/").toURI()), ALL_DIRECTORIES, ALL_FILES, "resources");
    }

    @Test
    public void should_export_generated_html() throws Exception {
        when(htmlGenerator.generateHtml(any(Page.class))).thenReturn("foobar");

        step.execute(zipper, aPage().build());

        verify(zipper).addToZip("foobar".getBytes(), "resources/index.html");
    }
}
