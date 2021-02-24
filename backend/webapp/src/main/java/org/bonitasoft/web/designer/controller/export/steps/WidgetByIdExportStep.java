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

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.controller.export.ExcludeDescriptorFilePredicate;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.controller.export.properties.WidgetPropertiesBuilder;
import org.bonitasoft.web.designer.model.widget.Widget;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_DIRECTORIES;

@Named
public class WidgetByIdExportStep implements ExportStep<Widget> {

    private Path widgetsPath;
    private WidgetPropertiesBuilder widgetPropertiesBuilder;

    @Inject
    public WidgetByIdExportStep(WorkspaceProperties workspaceProperties, WidgetPropertiesBuilder widgetPropertiesBuilder) {
        this.widgetsPath = workspaceProperties.getWidgets().getDir();
        this.widgetPropertiesBuilder = widgetPropertiesBuilder;
    }

    @Override
    public void execute(Zipper zipper, Widget widget) throws IOException {
        byte[] widgetProperties = widgetPropertiesBuilder.build(widget);
        zipper.addToZip(widgetProperties, "widget.properties");
        zipper.addDirectoryToZip(
                widgetsPath.resolve(widget.getId()),
                ALL_DIRECTORIES,
                new ExcludeDescriptorFilePredicate(format("%s.json",widget.getId())),
                RESOURCES);
    }
}
