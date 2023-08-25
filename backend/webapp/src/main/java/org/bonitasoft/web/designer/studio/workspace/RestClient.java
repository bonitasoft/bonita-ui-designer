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

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

/**
 * @author Romain Bioteau
 */
@Component
@RequiredArgsConstructor
public class RestClient {

    private final RestTemplate template;

    private final WorkspaceProperties workspaceProperties;

    /**
     * Gets rest template.
     */
    public RestTemplate getRestTemplate() {
        return template;
    }

    /**
     * Creates URL based on the URI passed in.
     */
    public String createURI(String uri) {
        var sb = new StringBuilder(workspaceProperties.getApiUrl());
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append("/");
        }
        sb.append(uri);
        return sb.toString();
    }
}
