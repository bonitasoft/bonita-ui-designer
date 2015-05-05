/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;

/**
 * Simple servlet that serves widgets files located outside of the war
 *
 * ex : for a request GET [mappedurl]/pbInput/pbInput.js it will load [widgetRepositoryPath]/pbInput/pbInput.js located on disk
 * @author Colin Puy
 */
@Component("widgetDirectiveLoaderServlet")
public class WidgetDirectiveLoader implements HttpRequestHandler {

    private Path widgetRepositoryPath;

    @Inject
    public WidgetDirectiveLoader(@Named("widgetPath") Path widgetRepositoryPath) {
        this.widgetRepositoryPath = widgetRepositoryPath;
    }

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Path filePath = getAskedFilePath(request);

        if (Files.notExists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setHeader("Content-Type", request.getServletContext().getMimeType(filePath.getFileName().toString()));
        response.setHeader("Content-Length", String.valueOf(filePath.toFile().length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filePath.getFileName() + "\"");
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, response.getOutputStream());
        }
    }

    private Path getAskedFilePath(HttpServletRequest request) throws UnsupportedEncodingException {
        // could be /something/file.js
        String relativeFilePath = URLDecoder.decode(request.getPathInfo().substring(1), StandardCharsets.UTF_8.toString());
        return widgetRepositoryPath.resolve(relativeFilePath);
    }
}
