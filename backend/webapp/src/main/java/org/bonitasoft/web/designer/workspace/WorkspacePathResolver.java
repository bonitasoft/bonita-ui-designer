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
 *
 * Each repository folder can be changed as well by changing <b>repository.widgets</b>, <b>repository.pages</b>
 *
 * @author Colin Puy
 */
@Named
public class WorkspacePathResolver {

    private static final String WIDGETS_DEFAULT_DIRECTORY = "widgets";
    private static final String PAGES_DEFAULT_DIRECTORY = "pages";

    @Inject
    private Environment env;

    public Path getWorkspacePath() {
        String workspace = env.getProperty("workspace");
        if (workspace == null) {
            return Paths.get(env.getProperty("user.home"), ".bonita");
        }
        return Paths.get(workspace);
    }

    public Path getPagesRepositoryPath() {
        return getRepositoryPath(PAGES_DEFAULT_DIRECTORY, env.getProperty("repository.pages"));
    }

    public Path getWidgetsRepositoryPath() {
        return getRepositoryPath(WIDGETS_DEFAULT_DIRECTORY, env.getProperty("repository.widgets"));
    }

    protected Path getRepositoryPath(String directoryName, String alternativeDirectoryPath) {
        if (alternativeDirectoryPath != null) {
            return Paths.get(alternativeDirectoryPath);
        }
        return getWorkspacePath().resolve(directoryName);
    }
}
