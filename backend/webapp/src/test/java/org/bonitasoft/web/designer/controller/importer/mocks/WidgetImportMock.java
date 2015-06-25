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
package org.bonitasoft.web.designer.controller.importer.mocks;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;

public class WidgetImportMock {

    private static final String WIDGETS_FOLDER = "widgets";

    private Path unzippedPath;
    private WidgetLoader widgetLoader;
    private WidgetRepository widgetRepository;

    public WidgetImportMock(Path unzippedPath, WidgetLoader widgetLoader, WidgetRepository widgetRepository) {
        this.unzippedPath = unzippedPath;
        this.widgetLoader = widgetLoader;
        this.widgetRepository = widgetRepository;
    }

    public List<Widget> mockWidgetsAsDependencies() throws IOException {
        Files.createDirectory(unzippedPath.resolve(WIDGETS_FOLDER));
        List<Widget> widgets = asList(
                aWidget().id("aWidget").custom().build(),
                aWidget().id("anotherWidget").custom().build());
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenReturn(widgets);
        return widgets;
    }

    public Widget mockWidgetToBeImported() {
        Widget widget = aWidget().id("aWidget").build();
        when(widgetLoader.load(unzippedPath,"widget.json")).thenReturn(widget);
        return widget;
    }

}
