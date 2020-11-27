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
import static org.bonitasoft.web.designer.SpringWebApplicationInitializer.UID_EXPERIMENTAL;
import static org.bonitasoft.web.designer.config.WebMvcConfiguration.WIDGETS_RESOURCES;
import static org.bonitasoft.web.designer.config.WebMvcConfiguration.WIDGETS_WC_RESOURCES;

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
import org.bonitasoft.web.designer.rendering.WidgetFileHelper;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

@Named
public class Workspace {

    protected static final Logger logger = LoggerFactory.getLogger(Workspace.class);

    @Value("${designer.modelVersion}")
    protected String modelVersion;
    private WorkspacePathResolver workspacePathResolver;
    private WidgetRepository widgetRepository;
    private WidgetFileBasedLoader widgetLoader;
    private WidgetDirectiveBuilder widgetDirectiveBuilder;
    private FragmentDirectiveBuilder fragmentDirectiveBuilder;
    private ResourceLoader resourceLoader;
    private AssetImporter<Widget> widgetAssetImporter;

    @Inject
    public Workspace(WorkspacePathResolver workspacePathResolver, WidgetRepository widgetRepository, WidgetFileBasedLoader widgetLoader,
                     WidgetDirectiveBuilder widgetDirectiveBuilder, FragmentDirectiveBuilder fragmentDirectiveBuilder,
                     ResourceLoader resourceLoader, AssetImporter<Widget> widgetAssetImporter) {
        this.workspacePathResolver = workspacePathResolver;
        this.widgetRepository = widgetRepository;
        this.widgetLoader = widgetLoader;
        this.resourceLoader = resourceLoader;
        this.widgetDirectiveBuilder = widgetDirectiveBuilder;
        this.fragmentDirectiveBuilder = fragmentDirectiveBuilder;
        this.widgetAssetImporter = widgetAssetImporter;
    }

    public void initialize() throws IOException {
        ensurePageRepositoryPresent();
        ensureWidgetRepositoryPresent();
        ensureWidgetRepositoryFilled();
        if(Boolean.getBoolean(UID_EXPERIMENTAL)){
            ensureWidgetWcRepositoryPresent();
            ensureWidgetRepositoryFilledWC();
        }
        ensureFragmentRepositoryPresent();
        cleanFragmentWorkspace();
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
            if (".metadata".equals(pageFolder)) {
                cleanMetadataFolder(pageWorkspace, pageFolder);
                return;
            }
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

    /**
     * remove  metadata file without a artifact in workspace
     * @param workspace
     * @param folder
     */
    protected void cleanMetadataFolder(Path workspace, String folder) {
        File metadataFolder = new File(workspace.resolve(folder).toString());
        Arrays.stream(metadataFolder.listFiles()).forEach(file -> {
            if (!workspace.resolve(removeExtension(file.getName())).resolve(file.getName()).toFile().exists()) {
                if (!workspace.resolve(folder).resolve(file.getName()).toFile().delete()) {
                    String error = String.format("Technical error when deleting file [%s]", workspace.resolve(folder).resolve("js").toString());
                    logger.error(error);
                }
            }
        });
    }

    private String removeExtension(String fileName) {
        if (fileName.indexOf(".") > 0) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }

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

    private void ensureWidgetWcRepositoryPresent() throws IOException {
        createDirectories(workspacePathResolver.getWidgetsWcRepositoryPath());
    }

    private void ensureWidgetRepositoryFilledWC() throws IOException {
        Path widgetRepositorySourcePath = Paths.get(resourceLoader.getResource(WIDGETS_WC_RESOURCES).getURI());
        FileUtils.copyDirectory(FileUtils.getFile(widgetRepositorySourcePath.toString()),FileUtils.getFile(workspacePathResolver.getWidgetsWcRepositoryPath().toString()));
    }

    private void ensureWidgetRepositoryFilled() throws IOException {
        Path widgetRepositorySourcePath = Paths.get(resourceLoader.getResource(WIDGETS_RESOURCES).getURI());
        List<Widget> widgets = widgetLoader.getAll(widgetRepositorySourcePath);

        for (Widget widget : widgets) {
            if (!widgetRepository.exists(widget.getId())) {
                createWidget(widgetRepositorySourcePath, widget);
            } else {
                Widget repoWidget = widgetRepository.get(widget.getId());
                if (isBlank(repoWidget.getArtifactVersion()) || new Version(modelVersion).isGreaterThan(repoWidget.getArtifactVersion())) {
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

    /**
     * Clean fragment Workspace:
     * * generated files
     * * Folder which don't have fragment descriptor
     * Theses file could be stay here when user make any action directly on filesystem
     */
    private void cleanFragmentWorkspace() {
        Path fragWorkspace = workspacePathResolver.getFragmentsRepositoryPath();
        Arrays.stream(new File(fragWorkspace.toString()).list()).forEach(fragment -> {
            if (".metadata".equals(fragment)) {
                cleanMetadataFolder(fragWorkspace, fragment);
                return;
            }
            try {
                if (isFragmentDescriptorExist(fragWorkspace, fragment)) {
                    //Remove min.js file, this file can be here for oldest fragment than this fix
                    WidgetFileHelper.deleteConcatenateFile(fragWorkspace.resolve(fragment));
                } else {
                    File f = fragWorkspace.resolve(fragment).toFile();
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(fragWorkspace.resolve(fragment).toFile());
                    }
                    logger.debug(String.format("Deleted fragment folder [%s] with success", fragWorkspace.resolve(fragment).toString()));
                }
            } catch (IOException e) {
                String error = String.format("Error while filter file in folder " + fragWorkspace.resolve(fragment).resolve(fragment).toString());
                logger.error(error, e);
            }
        });
    }

    private void ensureFragmentRepositoryPresent() throws IOException {
        createDirectories(workspacePathResolver.getFragmentsRepositoryPath());
        fragmentDirectiveBuilder.start(workspacePathResolver.getFragmentsRepositoryPath());
    }

    private boolean isFragmentDescriptorExist(Path fragWorkspace, String fragment) {
        return fragWorkspace.resolve(fragment).resolve(fragment + ".json").toFile().exists();
    }

}

