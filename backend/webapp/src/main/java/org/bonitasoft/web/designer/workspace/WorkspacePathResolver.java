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

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.env.Environment;

/**
 * Resolve bonita page designer workspace Path.
 * Workspace directory should be specified via a system property <b>workspace</b>.
 * If not, default value is {user.home}/.bonita
 * <p>
 * Each repository folder can be changed as well by changing <b>repository.widgets</b>, <b>repository.pages</b>
 *
 * @author Colin Puy
 */
@Named
public class WorkspacePathResolver {

    private static final String WIDGETS_DEFAULT_DIRECTORY = "widgets";
    public static final String FRAGMENTS = "fragments";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";
    private static final String TEMP_DIR = "workspace-uid";
    public static final String WIDGETS_WC_SUFFIX = "Wc";

    public static final String I18N = "i18n";

    @Inject
    private Environment env;

    public Path getWorkspacePath() {
        String workspace = env.getProperty("workspace");
        if (workspace == null) {
            return Paths.get(env.getProperty("user.home"), ".bonita");
        }
        return Paths.get(workspace);
    }

    public Path getTemporaryWorkspacePath() {
        return Paths.get(System.getProperty("java.io.tmpdir")).resolve(TEMP_DIR);
    }

    public Path getPagesRepositoryPath() {
        return getRepositoryPath(PAGES_DEFAULT_DIRECTORY, env.getProperty("repository.pages"));
    }

    public Path getWidgetsRepositoryPath() {
        return getRepositoryPath(WIDGETS_DEFAULT_DIRECTORY, env.getProperty("repository.widgets"));
    }

    public Path getWidgetsWcRepositoryPath() {
        return Paths.get(getWidgetsRepositoryPath() + WIDGETS_WC_SUFFIX);
    }

    public Path getFragmentsRepositoryPath() {
        return getRepositoryPath(FRAGMENTS, env.getProperty("repository.fragments"));
    }

    public Path getTmpFragmentsRepositoryPath() {
        return getTemporaryWorkspacePath().resolve(FRAGMENTS);
    }

    public Path getTmpI18nRepositoryPath() {
        return getTemporaryWorkspacePath().resolve(I18N);
    }

    protected Path getRepositoryPath(String directoryName, String alternativeDirectoryPath) {
        if (alternativeDirectoryPath != null) {
            return Paths.get(alternativeDirectoryPath);
        }
        return getWorkspacePath().resolve(directoryName);
    }

    public Path getTmpPagesRepositoryPath() {
        return getTemporaryWorkspacePath().resolve(PAGES_DEFAULT_DIRECTORY);
    }
}
