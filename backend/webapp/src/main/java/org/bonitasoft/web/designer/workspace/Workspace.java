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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.config.WebMvcConfiguration.WIDGETS_RESOURCES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

@Named
public class Workspace {

    protected static final Logger logger = LoggerFactory.getLogger(Workspace.class);

    @Value("${designer.version}")
    private String currentDesignerVersion;
    private WorkspacePathResolver workspacePathResolver;
    private WidgetRepository widgetRepository;
    private WidgetFileBasedLoader widgetLoader;
    private WidgetDirectiveBuilder widgetDirectiveBuilder;
    private ResourceLoader resourceLoader;
    private AssetImporter<Widget> widgetAssetImporter;

    @Inject
    public Workspace(WorkspacePathResolver workspacePathResolver, WidgetRepository widgetRepository, WidgetFileBasedLoader widgetLoader,
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

    /**
     * Clean page Workspace:
     * * generated files
     * * Folder which don't have page descriptor
     * Theses file could be stay here when user make any action directly on filesystem
     */
    public void cleanPageWorkspace() {
        Path pageWorkspace = workspacePathResolver.getPagesRepositoryPath();
        File file = new File(pageWorkspace.toString());
        Arrays.stream(file.list()).forEach(pageFolder -> {
            try {
                if (isPageExist(pageWorkspace, pageFolder)) {
                    // Clean Js folder, this folder can be exist in version before this fix
                    FileUtils.deleteDirectory(pageWorkspace.resolve(pageFolder).resolve("js").toFile());
                } else {
                    File f = pageWorkspace.resolve(pageFolder).toFile();
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(pageWorkspace.resolve(pageFolder).toFile());
                        logger.debug(String.format("Deleting folder [%s] with success", pageWorkspace.resolve(pageFolder).toString()));
                    }
                }
            } catch (IOException e) {
                String error = String.format("Technical error when deleting file [%s]", pageWorkspace.resolve(pageFolder).resolve("js").toString());
                logger.error(error, e);
            }
        });
    }

    private boolean isPageExist(Path pageWorkspace, String pageFolder) {
        return pageWorkspace.resolve(pageFolder).resolve(pageFolder + ".json").toFile().exists();
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
                // Split version before '_' to avoid patch tagged version compatible
                if (currentDesignerVersion != null) {
                    String[] currentVersion = currentDesignerVersion.split("_");
                    currentDesignerVersion = currentVersion[0];
                }
                if (isBlank(repoWidget.getDesignerVersion()) || new Version(currentDesignerVersion).isGreaterThan(repoWidget.getDesignerVersion())) {
                    FileUtils.deleteDirectory(widgetRepository.resolvePath(widget.getId()).toFile());
                    createWidget(widgetRepositorySourcePath, widget);
                }
            }
        }
        widgetDirectiveBuilder.start(workspacePathResolver.getWidgetsRepositoryPath());
    }

    private void createWidget(Path widgetRepositorySourcePath, Widget widget) throws IOException {
        Path widgetRepositoryPath = createDirectories(widgetRepository.resolvePath(widget.getId()));
        widgetRepository.updateLastUpdateAndSave(widget);

        //Widget help is copied
        File sourceHelpFile = new File(widgetRepositorySourcePath.toString() + File.separator + widget.getId() + File.separator + "help.html");
        if (widget.hasHelp() && sourceHelpFile.exists()) {
            FileUtils.copyFile(sourceHelpFile, new File(widgetRepositoryPath.toString() + File.separator + "help.html"));
        }

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

