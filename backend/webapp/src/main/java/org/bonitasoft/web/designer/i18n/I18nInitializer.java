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
package org.bonitasoft.web.designer.i18n;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.requireNonNull;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * Context Listener initializing bonita internationalization
 *
 * @author Vincent Elcrin
 */
@Slf4j
@Named
@RequiredArgsConstructor
public class I18nInitializer {

    private final LanguagePackBuilder languagePackBuilder;

    private final ResourcePatternResolver resourceLoader;

    private final WorkspacePathResolver workspacePathResolver;

    @PostConstruct
    public void contextInitialized() throws IOException {

        // uri is in the form because inside the fat jar.
        // => jar:file:/home/hugues/Projects/bonita/bonita-ui-designer/backend/webapp/target/ui-designer-1.13.0-SNAPSHOT.jar!/BOOT-INF/classes!/i18n
        // https://stackoverflow.com/questions/42655397/read-files-from-boot-inf-classes

        final Path tmpI18nRepositoryPath = workspacePathResolver.getTmpI18nRepositoryPath();

        // Extract po file form jar to a real working dir.
        extractToWorkingDir(tmpI18nRepositoryPath);

        try {
            log.info("I18n dir uri: {}", tmpI18nRepositoryPath);
            languagePackBuilder.start(tmpI18nRepositoryPath.toAbsolutePath());
        }
        catch (IOException e) {
            throw new DesignerInitializerException("Unable to convert po files into json", e);
        }
    }

    private void extractToWorkingDir(Path tmpI18nRepositoryPath) throws IOException {
        final File i18nFolder = tmpI18nRepositoryPath.toFile();
        Resource[] resources = resourceLoader.getResources(CLASSPATH_ALL_URL_PREFIX + "/i18n/*.po");
        if (!i18nFolder.exists() && !i18nFolder.mkdirs()) {
            throw new DesignerInitializerException("Failed to prepare conversion of po files into json in dir: " + tmpI18nRepositoryPath.toAbsolutePath());
        }
        for (Resource r : resources) {
            final String fileName = requireNonNull(r.getFilename());
            try {
                Files.copy(r.getInputStream(), tmpI18nRepositoryPath.resolve(fileName), REPLACE_EXISTING);
            }
            catch (IOException e) {
                log.error("Failed to process PO file: {}", fileName, e);
            }
        }
    }
}
