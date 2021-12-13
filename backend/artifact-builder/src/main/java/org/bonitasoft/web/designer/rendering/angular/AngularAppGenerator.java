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
package org.bonitasoft.web.designer.rendering.angular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.rendering.AssetHtmlBuilder;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.angular.AngularPropertyValuesVisitor;
import org.bonitasoft.web.designer.visitor.angular.AngularVariableVisitor;
import org.bonitasoft.web.designer.workspace.Workspace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;

@RequiredArgsConstructor
@Slf4j
public class AngularAppGenerator<P extends Previewable & Identifiable> {

    protected static final String INDEX_HTML_TEMPLATE = "angular/src/index.hbs.html";
    protected static final String MAIN_TS_TEMPLATE = "angular/src/main.hbs.ts";

    private final WorkspaceUidProperties workspaceUidProperties;
    private final HtmlGenerator angularHtmlGenerator;
    private final AssetHtmlBuilder assetHtmlBuilder;
    private final AngularPropertyValuesVisitor angularPropertyValuesVisitor;
    private final AngularVariableVisitor angularVariableVisitor;
    private final WidgetBundleFile widgetBundleFile;

    public void generateAngularApp(P artifact) {
        try {
            // Copy Angular template app
            var appDir = workspaceUidProperties.getTmpAngularAppPath(Workspace.GENERIC_ANGULAR_PAGE_ID);
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
            copyAngularApp(workspaceUidProperties.getExportAngularBackendResourcesPath().toString(), appDir.toString());

            var srcFolderPath = appDir.resolve("src").resolve("app");
            if (!Files.exists(srcFolderPath)) {
                Files.createDirectories(srcFolderPath);
            }

            // Compile templating and generate app file
            srcAppGeneration(artifact, srcFolderPath);

            // Compile src files
            srcFilesGeneration(artifact, srcFolderPath.getParent());

            // Generate asset files
            var assetFolderPath = appDir.resolve("src").resolve("assets");
            if (!Files.exists(assetFolderPath)) {
                Files.createDirectories(assetFolderPath);
            }
            assetsFilesGeneration(artifact, assetFolderPath);
        } catch (IOException e) {
            var logMessage = format("Error during %s angular app generation %s", artifact.getId(), e.getMessage());
            log.error(logMessage);
            throw new GenerationException(logMessage, e);
        }
    }

    public void generatedAppInstallStart(P artifact) {
        // Start this in a separate thread to avoid block the tomcat server
        Thread threadGeneratedAppInstallation = new Thread(() -> {
            var appDir = workspaceUidProperties.getTmpAngularAppPath(artifact.getId());
            MavenCli maven = new MavenCli();
            System.setProperty("maven.multiModuleProjectDirectory", appDir.toString());
            maven.doMain(new String[]{"install"}, appDir.toString(), System.out, System.err);
        });
        threadGeneratedAppInstallation.start();
    }

    protected String generateComponentTs(P page) {
        var sortedAssets = assetHtmlBuilder.getSortedAssets(page);
        var template = new TemplateEngine("angular/src/app/app.component.hbs.ts")
                .with("appTag", page.getId())
                .with("widgets", widgetBundleFile.getWidgetsBundlePathUsedInArtifact(page))
                .with("jsAsset", assetHtmlBuilder.getAssetAngularSrcList(page.getId(), AssetType.JAVASCRIPT, sortedAssets));
        return template.build(page);
    }

    protected String generateAssetsStyle(P page) {
        var sortedAssets = assetHtmlBuilder.getSortedAssets(page);
        var template = new TemplateEngine("angular/src/app/app.style.hbs.css")
                .with("cssAsset", assetHtmlBuilder.getAssetAngularSrcList(page.getId(),AssetType.CSS, sortedAssets));
        return template.build(page);
    }

    protected String generateModuleTs(P page) {
        var template = new TemplateEngine("angular/src/app/app.module.hbs.ts")
                .with("appTag", page.getId());
        return template.build(page);
    }

    protected String generateFileWithAppTag(P page, String templateEngine) {
        var template = new TemplateEngine(templateEngine)
                .with("appTag", page.getId());
        return template.build(page);
    }

    private void srcAppGeneration(P artifact, Path srcFolderPath) throws IOException {
        var artifactId = artifact.getId();
        byte[] html = angularHtmlGenerator.generateHtml(artifact).getBytes(StandardCharsets.UTF_8);
        Files.write(srcFolderPath.resolve(String.format("%s.component.html", artifactId)), html);

        byte[] componentTs = generateComponentTs(artifact).getBytes(StandardCharsets.UTF_8);
        Files.write(srcFolderPath.resolve(String.format("%s.component.ts", artifactId)), componentTs);

        byte[] componentCss = generateAssetsStyle(artifact).getBytes(StandardCharsets.UTF_8);
        Files.write(srcFolderPath.resolve("style.css"), componentCss);

        byte[] moduleTs = generateModuleTs(artifact).getBytes(StandardCharsets.UTF_8);
        Files.write(srcFolderPath.resolve(String.format("%s.module.ts", artifactId)), moduleTs);
    }

    private void srcFilesGeneration(P artifact, Path srcFolderPath) throws IOException {
        Files.write(srcFolderPath.resolve("index.html"), generateFileWithAppTag(artifact, INDEX_HTML_TEMPLATE).getBytes(StandardCharsets.UTF_8));
        Files.write(srcFolderPath.resolve("main.ts"), generateFileWithAppTag(artifact, MAIN_TS_TEMPLATE).getBytes(StandardCharsets.UTF_8));
    }

    private void copyAngularApp(String sourceDirectoryLocation, String destinationDirectoryLocation) throws IOException {
        File sourceDirectory = new File(sourceDirectoryLocation);
        File destinationDirectory = new File(destinationDirectoryLocation);
        FileUtils.copyDirectory(sourceDirectory, destinationDirectory);
    }

    private void assetsFilesGeneration(P artifact, Path assetsFolderPath) throws IOException {
        Files.write(assetsFolderPath.resolve("propertiesValues.ts"),angularPropertyValuesVisitor.generate(artifact).getBytes(StandardCharsets.UTF_8));
        Files.write(assetsFolderPath.resolve("variableModel.ts"),angularVariableVisitor.generate(artifact).getBytes(StandardCharsets.UTF_8));
    }

}
