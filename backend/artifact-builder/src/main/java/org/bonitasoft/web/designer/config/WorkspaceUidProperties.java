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
package org.bonitasoft.web.designer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createDirectories;
import static org.bonitasoft.web.designer.i18n.I18nInitializer.I18N_RESOURCES;
import static org.bonitasoft.web.designer.workspace.Workspace.EXTRACT_BACKEND_RESOURCES;

@Data
@ConfigurationProperties(prefix = "designer.workspace-uid")
public class WorkspaceUidProperties {

    public static final String FRAGMENTS = "fragments";
    public static final String TEMPLATES_RESOURCES = "templates";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";
    
    private boolean liveBuildEnabled = true;

    private Path path = Path.of(System.getProperty("java.io.tmpdir")).resolve("workspace-uid");

    private Path extractPath = getPath().resolve("extract");

    public Path getTmpFragmentsRepositoryPath() {
        return getPath().resolve(FRAGMENTS);
    }

    public Path getTmpPagesRepositoryPath() {
        return getPath().resolve(PAGES_DEFAULT_DIRECTORY);
    }

    public Path getTmpI18nPath() throws IOException {
        return createDirectories(getPath().resolve(I18N_RESOURCES));
    }

    public Path getExportBackendResourcesPath() {
        return getExtractPath().resolve(EXTRACT_BACKEND_RESOURCES).resolve("runtime");
    }

    public Path getTemplateResourcesPath() {
        return getExtractPath().resolve(TEMPLATES_RESOURCES);
    }

}
