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
package org.bonitasoft.web.designer.studio.workspace;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.env.Environment;

@Named
public class RestClientProperties {

    protected static final String WORKSPACE_API_REST_URL = "workspace.api.rest.url";

    @Inject
    private Environment env;

    /**
     * Gets URL.
     */
    public String getUrl() {
        return env.getProperty(WORKSPACE_API_REST_URL);
    }

    /**
     * @return true if the System property 'workspace.api.rest.url' has been set
     */
    public boolean isURLSet() {
        return getUrl() != null;
    }

}
