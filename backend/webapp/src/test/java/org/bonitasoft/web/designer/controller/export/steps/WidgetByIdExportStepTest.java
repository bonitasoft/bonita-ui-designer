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

import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetByIdExportStepTest {

    @Mock
    private WorkspacePathResolver pathResolver;

    @InjectMocks
    private WidgetByIdExportStep step;

    @Mock
    private Zipper zipper;

    @Test
    public void should_add_a_widget_by_id_to_zip() throws Exception {
        when(pathResolver.getWidgetsRepositoryPath()).thenReturn(Paths.get("widgets"));

        step.execute(zipper, aWidget().id("anId").build());

        verify(zipper).addDirectoryToZip(Paths.get("widgets").resolve("anId"), "resources/anId");
    }
}
