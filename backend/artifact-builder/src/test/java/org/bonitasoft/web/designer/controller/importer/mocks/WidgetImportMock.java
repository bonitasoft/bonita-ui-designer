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

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetDependencyImporter;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

public class WidgetImportMock {

    private static final String WIDGETS_FOLDER = "widgets";

    private final Path unzippedPath;
    private final WidgetRepository widgetRepository;
    private final List<Widget> widgets = new ArrayList<>();
    private JsonHandler jsonHandlerMock;

    public WidgetImportMock(Path unzippedPath, WidgetRepository widgetRepository, JsonHandler jsonHandlerMock) throws IOException {
        this.unzippedPath = unzippedPath;
        this.widgetRepository = widgetRepository;
        this.jsonHandlerMock = jsonHandlerMock;
    }

    public List<Widget> mockWidgetsAsAddedDependencies() throws IOException {
        return mockWidgetsAsAddedDependencies(
                aWidget().withId("aWidget").custom(),
                aWidget().withId("anotherWidget").custom());
    }

    public List<Widget> mockWidgetsAsAddedDependencies(WidgetBuilder... widgetBuilders) throws IOException {
        List<Widget> widgets = stream(widgetBuilders).map(WidgetBuilder::build).collect(Collectors.toList());
        Files.createDirectories(unzippedPath.resolve(WIDGETS_FOLDER));
        this.widgets.addAll(widgets);
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetRepository.loadAll(unzippedPath.resolve(WIDGETS_FOLDER),
                WidgetDependencyImporter.CUSTOM_WIDGET_FILTER)).thenReturn(this.widgets);
        return widgets;
    }

    public List<Widget> mockWidgetsAsOverridenDependencies() throws IOException {
        Files.createDirectories(unzippedPath.resolve(WIDGETS_FOLDER));
        Widget alreadyThereWidget = aWidget().withId("alreadyThereWidget").custom().build();
        Widget anotherExistingWidget = aWidget().withId("anotherExistingWidget").custom().build();
        List<Widget> widgets = asList(alreadyThereWidget, anotherExistingWidget);
        this.widgets.addAll(widgets);
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetRepository.loadAll(unzippedPath.resolve(WIDGETS_FOLDER),
                WidgetDependencyImporter.CUSTOM_WIDGET_FILTER)).thenReturn(this.widgets);
        when(widgetRepository.exists("alreadyThereWidget")).thenReturn(true);
        when(widgetRepository.exists("anotherExistingWidget")).thenReturn(true);
        when(widgetRepository.get("alreadyThereWidget")).thenReturn(alreadyThereWidget);
        when(widgetRepository.get("anotherExistingWidget")).thenReturn(anotherExistingWidget);
        return widgets;
    }

}
