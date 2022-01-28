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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.WidgetFileHelper;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class Workspace {

    public static final String EXTRACT_BACKEND_RESOURCES = "META-INF/resources";

    public static final String WIDGETS_RESOURCES = "widgets";

    public static final String METADATA_FOLDER_NAME = ".metadata";
    protected static final Logger logger = LoggerFactory.getLogger(Workspace.class);
    private final UiDesignerProperties uiDesignerProperties;

    private final WidgetRepository widgetRepository;

    private final PageRepository pageRepository;

    private final ResourcesCopier resourcesCopier;

    private final WidgetDirectiveBuilder widgetDirectiveBuilder;

    private final FragmentDirectiveBuilder fragmentDirectiveBuilder;

    private final AssetDependencyImporter<Widget> widgetAssetDependencyImporter;

    private final Path extractPath;

    private final List<LiveRepositoryUpdate> migrations;

    private final JsonHandler jsonHandler;

    protected AtomicBoolean initialized = new AtomicBoolean(false);

    public Workspace(UiDesignerProperties uiDesignerProperties, WidgetRepository widgetRepository, PageRepository pageRepository,
                     WidgetDirectiveBuilder widgetDirectiveBuilder, FragmentDirectiveBuilder fragmentDirectiveBuilder,
                     AssetDependencyImporter<Widget> widgetAssetDependencyImporter, ResourcesCopier resourcesCopier,
                     List<LiveRepositoryUpdate> migrations,
                     JsonHandler jsonHandler
    ) {
        this.widgetRepository = widgetRepository;
        this.pageRepository = pageRepository;
        this.resourcesCopier = resourcesCopier;
        this.widgetDirectiveBuilder = widgetDirectiveBuilder;
        this.fragmentDirectiveBuilder = fragmentDirectiveBuilder;
        this.widgetAssetDependencyImporter = widgetAssetDependencyImporter;
        this.uiDesignerProperties = uiDesignerProperties;
        this.extractPath = uiDesignerProperties.getWorkspaceUid().getExtractPath();
        this.migrations = migrations;
        this.jsonHandler = jsonHandler;
    }

    protected void doInitialize() throws IOException {
        // First, clean up the extractPath temp dir
        FileSystemUtils.deleteRecursively(extractPath);
        ensureTemplateRepositoryPresent();
        ensureTemplateRepositoryFilled();
        ensurePageRepositoryPresent();
        ensureWidgetRepositoryPresent();
        ensureWidgetRepositoryFilled();
        ensureFragmentRepositoryPresent();
        cleanFragmentWorkspace();
        extractResourcesForExport();
    }

    public void initialize() {
        if (!initialized.get()) {
            try {
                doInitialize();
                for (LiveRepositoryUpdate<?> migration : migrations) {
                    migration.start();
                }
                cleanPageWorkspace();
                initialized.set(true);
            } catch (IOException e) {
                throw new DesignerInitializerException("Unable to initialize workspace", e);
            }
        }
    }

    public void migrateWorkspace() {
        initialize(); //Ensure that the workspace initialization is ended
        migrations.stream().forEachOrdered(migration -> {
            try {
                migration.migrate();
            } catch (IOException e) {
                throw new DesignerInitializerException("Unable to migrate workspace", e);
            }
        });
    }

    public void indexingArtifacts(List<Page> pages) {
        initialize(); //Ensure that the workspace initialization is ended
        pageRepository.refreshIndexing(pages);
    }

    /**
     * Clean page Workspace:
     * * generated files
     * * Folder which don't have page descriptor
     * Theses file could be stay here when user make any action directly on filesystem
     */
    public void cleanPageWorkspace() {
        var pageWorkspace = uiDesignerProperties.getWorkspace().getPages().getDir();
        var files = requireNonNull(pageWorkspace.toFile().list());
        stream(files).forEach(pageFolder -> {

            // metadata folder is at the same level as page folders
            if (METADATA_FOLDER_NAME.equals(pageFolder)) {
                cleanMetadataFolder(pageWorkspace);
                return;
            }

            // pages
            try {
                if (isPageExist(pageWorkspace, pageFolder)) {
                    // Clean Js folder, this folder can be exist in version before this fix
                    FileUtils.deleteDirectory(pageWorkspace.resolve(pageFolder).resolve("js").toFile());
                } else {
                    var f = pageWorkspace.resolve(pageFolder).toFile();
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(pageWorkspace.resolve(pageFolder).toFile());
                        logger.debug("Deleting folder [{}] with success", pageWorkspace.resolve(pageFolder));
                    }
                }
            } catch (IOException e) {
                logger.error("Technical error when deleting files [{}]", pageWorkspace.resolve(pageFolder).resolve("js"), e);
            }

        });
    }

    /**
     * remove  metadata file without a artifact in workspace
     *
     * @param workspace
     */
    protected void cleanMetadataFolder(Path workspace) {
        var metadataFolder = new File(workspace.resolve(METADATA_FOLDER_NAME).toString());
        stream(requireNonNull(metadataFolder.listFiles())).forEach(page -> {
            var pageFileName = page.getName();
            if (!pageExists(workspace, pageFileName)) {
                deleteMissingPageMetadata(workspace, pageFileName);
            }
        });
    }

    private void deleteMissingPageMetadata(Path workspace, String pageFileName) {
        try {
            Files.delete(workspace.resolve(METADATA_FOLDER_NAME).resolve(pageFileName));
        } catch (IOException e) {
            logger.error("Technical error when deleting file [{}]", workspace.resolve(METADATA_FOLDER_NAME).resolve("js"));
        }
    }

    private boolean pageExists(Path workspace, String fileName) {
        return workspace.resolve(removeExtension(fileName)).resolve(fileName).toFile().exists();
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

    private void ensureTemplateRepositoryPresent() throws IOException {
        createDirectories(extractPath.resolve(WorkspaceUidProperties.TEMPLATES_RESOURCES));
    }

    private void ensureTemplateRepositoryFilled() throws IOException {
        resourcesCopier.copy(extractPath, WorkspaceUidProperties.TEMPLATES_RESOURCES);
    }

    private void ensurePageRepositoryPresent() throws IOException {
        createDirectories(uiDesignerProperties.getWorkspace().getPages().getDir());
    }

    private void ensureWidgetRepositoryPresent() throws IOException {
        createDirectories(uiDesignerProperties.getWorkspace().getWidgets().getDir());
    }

    private void ensureWidgetRepositoryFilled() throws IOException {

        // Extract/Unzip widgets from UID jar file to a temp uid working dir (not the bonita project dir)
        resourcesCopier.copy(extractPath, WIDGETS_RESOURCES);
        var widgetRepositorySourcePath = extractPath.resolve(WIDGETS_RESOURCES);

        // loop on widgets from UID and ensure that they exist in the bonita project dir // == import
        var widgetLoader = new WidgetFileBasedLoader(jsonHandler);
        var widgets = widgetLoader.getAll(widgetRepositorySourcePath);
        for (var widget : widgets) {
            if (!widgetRepository.exists(widget.getId())) {
                createWidget(widgetRepositorySourcePath, widget);
            } else {
                var repoWidget = widgetRepository.get(widget.getId());
                if (isBlank(repoWidget.getArtifactVersion()) || new Version(uiDesignerProperties.getModelVersion()).isGreaterThan(repoWidget.getArtifactVersion())) {
                    FileUtils.deleteDirectory(widgetRepository.resolvePath(widget.getId()).toFile());
                    createWidget(widgetRepositorySourcePath, widget);
                }
            }
        }

        widgetDirectiveBuilder.start(uiDesignerProperties.getWorkspace().getWidgets().getDir());
    }


    private void createWidget(Path widgetRepositorySourcePath, Widget widget) throws IOException {
        var targetWidgetPath = widgetRepository.resolvePath(widget.getId());
        var widgetRepositoryPath = createDirectories(targetWidgetPath);
        widgetRepository.updateLastUpdateAndSave(widget);

        //Widget help is copied
        var sourceHelpFile = new File(widgetRepositorySourcePath.toString() + File.separator + widget.getId() + File.separator + "help.html");
        if (widget.hasHelp() && sourceHelpFile.exists()) {
            FileUtils.copyFile(sourceHelpFile, new File(widgetRepositoryPath.toString() + File.separator + "help.html"));
        }

        //Widget assets are copied if they exist
        try {
            var assets = widgetAssetDependencyImporter.load(widget, widgetRepositorySourcePath);
            widgetAssetDependencyImporter.save(assets, widgetRepositorySourcePath);
        } catch (IOException e) {
            var error = String.format("Technical error when importing widget asset [%s]", widget.getId());
            logger.error(error, e);
            throw new ArtifactBuilderException(error, e);
        }
    }

    /**
     * Clean fragment Workspace:
     * * generated files
     * * Folder which don't have fragment descriptor
     * Theses file could be stay here when user make any action directly on filesystem
     */
    private void cleanFragmentWorkspace() {
        var fragWorkspace = uiDesignerProperties.getWorkspace().getFragments().getDir();
        stream(requireNonNull(fragWorkspace.toFile().list())).forEach(fragment -> {

            if (METADATA_FOLDER_NAME.equals(fragment)) {
                cleanMetadataFolder(fragWorkspace);
                return;
            }

            try {
                if (isFragmentDescriptorExist(fragWorkspace, fragment)) {
                    //Remove min.js file, this file can be here for oldest fragment than this fix
                    WidgetFileHelper.deleteConcatenateFile(fragWorkspace.resolve(fragment));
                } else {
                    var f = fragWorkspace.resolve(fragment).toFile();
                    if (f.isDirectory()) {
                        FileUtils.deleteDirectory(fragWorkspace.resolve(fragment).toFile());
                    }
                    logger.debug("Deleted fragment folder [{}] with success", fragWorkspace.resolve(fragment));
                }
            } catch (IOException e) {
                var error = "Error while filter file in folder " + fragWorkspace.resolve(fragment).resolve(fragment);
                logger.error(error, e);
            }
        });
    }

    private void ensureFragmentRepositoryPresent() throws IOException {
        var fragmentsPath = uiDesignerProperties.getWorkspace().getFragments().getDir();
        createDirectories(fragmentsPath);
        fragmentDirectiveBuilder.start(fragmentsPath);
    }

    private boolean isFragmentDescriptorExist(Path fragWorkspace, String fragment) {
        return fragWorkspace.resolve(fragment).resolve(fragment + ".json").toFile().exists();
    }

    private void extractResourcesForExport() throws IOException {
        resourcesCopier.copy(extractPath, EXTRACT_BACKEND_RESOURCES);
    }

}

