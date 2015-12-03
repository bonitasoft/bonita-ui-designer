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

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;

public class WidgetImportMock {

    private static final String WIDGETS_FOLDER = "widgets";

    private Path unzippedPath;
    private WidgetLoader widgetLoader;
    private WidgetRepository widgetRepository;
    private List<Widget> widgets = new ArrayList<>();

    public WidgetImportMock(Path unzippedPath, WidgetLoader widgetLoader, WidgetRepository widgetRepository) throws IOException {
        this.unzippedPath = unzippedPath;
        this.widgetLoader = widgetLoader;
        this.widgetRepository = widgetRepository;
    }

    public List<Widget> mockWidgetsAsAddedDependencies() throws IOException {
        return mockWidgetsAsAddedDependencies(
                aWidget().id("aWidget").custom(),
                aWidget().id("anotherWidget").custom());
    }

    public List<Widget> mockWidgetsAsAddedDependencies(WidgetBuilder... widgetBuilders) throws IOException {
        List<Widget> widgets = transform(asList(widgetBuilders), new Function<WidgetBuilder, Widget>() {

            @Override
            public Widget apply(WidgetBuilder builder) {
                return builder.build();
            }
        });
        Files.createDirectories(unzippedPath.resolve(WIDGETS_FOLDER));
        this.widgets.addAll(widgets);
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenReturn(this.widgets);
        return widgets;
    }

    public List<Widget> mockWidgetsAsOverridenDependencies() throws IOException {
        Files.createDirectories(unzippedPath.resolve(WIDGETS_FOLDER));
        List<Widget> widgets = asList(
                aWidget().id("alreadyThereWidget").custom().build(),
                aWidget().id("anotherExistingWidget").custom().build());
        this.widgets.addAll(widgets);
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(widgetLoader.getAllCustom(unzippedPath.resolve(WIDGETS_FOLDER))).thenReturn(this.widgets);
        when(widgetRepository.exists("alreadyThereWidget")).thenReturn(true);
        when(widgetRepository.exists("anotherExistingWidget")).thenReturn(true);
        return widgets;
    }

}
