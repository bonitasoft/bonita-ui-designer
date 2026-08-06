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
package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

public class ResponseHeadersHelper {

    public static final String SLASH = "/";

    private ResponseHeadersHelper() {
        // Utility class
    }

    public static ResponseEntity<Void> getMovedResourceResponse(HttpServletRequest request, String newObjectId) throws RepositoryException {
        return getMovedResourceResponse(request, newObjectId, null);
    }

    public static ResponseEntity<Void> getMovedResourceResponse(HttpServletRequest request, String newObjectId, String currentURIAttributeSuffix) throws RepositoryException {
        var responseHeaders = new HttpHeaders();
        try {
            var currentURI = request.getRequestURI();
            String requestURI;
            if (currentURIAttributeSuffix != null && currentURI.lastIndexOf(currentURIAttributeSuffix) >= 0) {
                var indexOfSuffix = currentURI.lastIndexOf(currentURIAttributeSuffix);
                requestURI = currentURI.substring(0, indexOfSuffix);
            } else {
                requestURI = currentURI;
            }
            var currentURILastSeparatorIndex = requestURI.lastIndexOf(SLASH);
            var newLocation = new URI(requestURI.substring(0, currentURILastSeparatorIndex) + SLASH + newObjectId);
            responseHeaders.setLocation(newLocation);
            responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        } catch (URISyntaxException e) {
            throw new RepositoryException("Failed to generate new object URI", e);
        }
        return ResponseEntity.ok().headers(responseHeaders).build();
    }
}
