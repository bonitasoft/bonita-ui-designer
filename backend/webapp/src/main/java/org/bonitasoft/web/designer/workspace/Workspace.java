/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.workspace;

import static java.nio.file.Files.createDirectories;
import static org.bonitasoft.web.designer.config.WebMvcConfiguration.WIDGETS_RESOURCES;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.springframework.core.io.ResourceLoader;

@Named
public class Workspace {

    private WorkspacePathResolver workspacePathResolver;
    private WidgetRepository widgetRepository;
    private WidgetLoader widgetLoader;
    private WidgetDirectiveBuilder widgetDirectiveBuilder;
    private ResourceLoader resourceLoader;

    @Inject
    public Workspace(WorkspacePathResolver workspacePathResolver, WidgetRepository widgetRepository, WidgetLoader widgetLoader,
                     WidgetDirectiveBuilder widgetDirectiveBuilder, ResourceLoader resourceLoader) {
        this.workspacePathResolver = workspacePathResolver;
        this.widgetRepository = widgetRepository;
        this.widgetLoader = widgetLoader;
        this.resourceLoader = resourceLoader;
        this.widgetDirectiveBuilder = widgetDirectiveBuilder;
    }

    public void initialize() throws IOException {
        ensurePageRepositoryPresent();
        ensureWidgetRepositoryPresent();
        ensureWidgetRepositoryFilled();
    }

    private void ensurePageRepositoryPresent() throws IOException {
        createDirectories(workspacePathResolver.getPagesRepositoryPath());
    }

    private void ensureWidgetRepositoryPresent() throws IOException {
        createDirectories(workspacePathResolver.getWidgetsRepositoryPath());
    }

    private void ensureWidgetRepositoryFilled() throws IOException {
        List<Widget> widgets = widgetLoader.getAll(Paths.get(resourceLoader.getResource(WIDGETS_RESOURCES).getURI()));

        for (Widget widget : widgets) {
            if (!widgetRepository.exists(widget.getId())) {
                createDirectories(widgetRepository.resolvePath(widget.getId()));
                widgetRepository.save(widget);
            }
        }
        widgetDirectiveBuilder.start(workspacePathResolver.getWidgetsRepositoryPath());
    }

}
