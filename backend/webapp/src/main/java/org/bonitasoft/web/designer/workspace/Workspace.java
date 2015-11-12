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

import static java.nio.file.Files.createDirectories;
import static org.bonitasoft.web.designer.config.WebMvcConfiguration.WIDGETS_RESOURCES;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

@Named
public class Workspace {
    protected static final Logger logger = LoggerFactory.getLogger(Workspace.class);
    private WorkspacePathResolver workspacePathResolver;
    private WidgetRepository widgetRepository;
    private WidgetLoader widgetLoader;
    private WidgetDirectiveBuilder widgetDirectiveBuilder;
    private ResourceLoader resourceLoader;
    private AssetImporter<Widget> widgetAssetImporter;

    @Inject
    public Workspace(WorkspacePathResolver workspacePathResolver, WidgetRepository widgetRepository, WidgetLoader widgetLoader,
                     WidgetDirectiveBuilder widgetDirectiveBuilder, ResourceLoader resourceLoader, AssetImporter<Widget> widgetAssetImporter) {
        this.workspacePathResolver = workspacePathResolver;
        this.widgetRepository = widgetRepository;
        this.widgetLoader = widgetLoader;
        this.resourceLoader = resourceLoader;
        this.widgetDirectiveBuilder = widgetDirectiveBuilder;
        this.widgetAssetImporter = widgetAssetImporter;
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
        Path widgetRepositorySourcePath = Paths.get(resourceLoader.getResource(WIDGETS_RESOURCES).getURI());
        List<Widget> widgets = widgetLoader.getAll(widgetRepositorySourcePath);

        for (Widget widget : widgets) {
            if (!widgetRepository.exists(widget.getId())) {
                createWidget(widgetRepositorySourcePath, widget);
            } else {
                Widget repoWidget = widgetRepository.get(widget.getId());
                if(!StringUtils.equals(repoWidget.getDesignerVersion(), widget.getDesignerVersion())){
                    FileUtils.deleteDirectory(widgetRepository.resolvePath(widget.getId()).toFile());
                    createWidget(widgetRepositorySourcePath, widget);
                }
            }
        }
        widgetDirectiveBuilder.start(workspacePathResolver.getWidgetsRepositoryPath());
    }

    private void createWidget(Path widgetRepositorySourcePath, Widget widget) throws IOException {
        createDirectories(widgetRepository.resolvePath(widget.getId()));
        widgetRepository.updateLastUpdateAndSave(widget);
        //Widget assets are copied if they exist
        try {
            List<Asset> assets = widgetAssetImporter.load(widget, widgetRepositorySourcePath);
            widgetAssetImporter.save(assets, widgetRepositorySourcePath);
        } catch (IOException e) {
            String error = String.format("Technical error when importing widget asset [%s]", widget.getId());
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

}
