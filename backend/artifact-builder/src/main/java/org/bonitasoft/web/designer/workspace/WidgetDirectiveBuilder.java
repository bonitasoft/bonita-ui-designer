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
package org.bonitasoft.web.designer.workspace;

import org.bonitasoft.web.designer.livebuild.AbstractLiveFileBuilder;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;

public class WidgetDirectiveBuilder extends AbstractLiveFileBuilder {

    private final WidgetFileBasedLoader widgetLoader;
    private final HtmlSanitizer htmlSanitizer;
    private final TemplateEngine htmlBuilder = new TemplateEngine("widgetDirectiveTemplate.hbs.js");
    private static final String JSON_EXTENSION = ".json";

    public WidgetDirectiveBuilder(Watcher watcher, WidgetFileBasedLoader widgetLoader, HtmlSanitizer htmlSanitizer) {
        super(watcher);
        this.widgetLoader = widgetLoader;
        this.htmlSanitizer = htmlSanitizer;
    }

    /**
     * Build directive corresponding to the widget descriptive json file which has changed.
     * Resulting js file is created in the same directory than the json file overriding previous build.
     *
     * @param jsonPath is the path to the widget file to build.
     * @throws IOException
     */
    @Override
    public void build(Path jsonPath) throws IOException {
        var widget = widgetLoader.get(jsonPath);
        write(
                get(valueOf(jsonPath).replace(JSON_EXTENSION, ".js")),
                htmlBuilder
                        .with("escapedTemplate", htmlSanitizer.escapeSingleQuotesAndNewLines(widget.getTemplate()))
                        .build(widget).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isBuildable(String path) {
        return path.endsWith(JSON_EXTENSION) && !path.contains(".metadata") && isAngularJsWidget(Paths.get(path));
    }

    private boolean isAngularJsWidget(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.startsWith(WidgetRepository.V3_WIDGET_STANDARD_PREFIX)) {
            return false;
        }
        if (fileName.startsWith(WidgetRepository.ANGULARJS_STANDARD_PREFIX)
                || fileName.startsWith(WidgetRepository.ANGULARJS_CUSTOM_PREFIX)) {
            return true;
        }
        // "Custom widget V3" or "custom widget AngularJS without 'custom' prefix"
        return !isV3CustomWidget(path);
    }

    private boolean isV3CustomWidget(Path path) {
        var widget = widgetLoader.get(path);
        String version = getArtifactVersion(widget);
        return Version.isV3Version(version);
    }

    public String getArtifactVersion(Widget widget) {
        // Use model version if it is present
        if (widget.getModelVersion() != null) {
            return widget.getModelVersion();
        } else {
            return widget.getDesignerVersion();
        }
    }

}
