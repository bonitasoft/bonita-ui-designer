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
package org.bonitasoft.web.designer.controller.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

import com.google.common.base.Preconditions;

public class HttpFile {

    public static void writeFileInResponseForVisualization(HttpServletRequest request, HttpServletResponse response, Path filePath) throws IOException {
        if (!isExistingFilePath(response, filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String mimeType = request.getServletContext().getMimeType(filePath.getFileName().toString());
        if (mimeType == null || !mimeType.contains("image")) {
            mimeType = MediaType.TEXT_PLAIN_VALUE;
        }
        writeFileInResponse(response, filePath, mimeType, "inline");
    }

    public static void writeFileInResponseForDownload(HttpServletResponse response, Path filePath) throws IOException {
        if (!isExistingFilePath(response, filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeFileInResponse(response, filePath, MediaType.APPLICATION_OCTET_STREAM_VALUE, "attachment");
    }

    public static void writeFileInResponse(HttpServletRequest request, HttpServletResponse response, Path filePath) throws IOException {
        if (!isExistingFilePath(response, filePath)){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeFileInResponse(response, filePath, request.getServletContext().getMimeType(filePath.getFileName().toString()), "inline");
    }

    /**
     * Write headers and content in the response
     */
    private static void writeFileInResponse(HttpServletResponse response, Path filePath, String mimeType,
            String contentDispositionType) throws IOException {
        response.setHeader("Content-Type", mimeType);
        response.setHeader("Content-Length", String.valueOf(filePath.toFile().length()));
        response.setHeader("Content-Disposition", new StringBuilder().append(contentDispositionType)
                .append("; filename=\"")
                .append(filePath.getFileName())
                .append("\"").toString());
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(filePath, out);
        }
    }

    /**
     * This method helps to fix a bug on IE when the browser send the full path of the file in the filename
     */
    public static String getOriginalFilename(String filename) {
        Preconditions.checkNotNull(filename, "File name is required");
        // check for Unix-style path
        int pos = filename.lastIndexOf("/");
        if (pos == -1) {
            // check for Windows-style path
            pos = filename.lastIndexOf("\\");
        }
        if (pos != -1) {
            // any sort of path separator found
            return filename.substring(pos + 1);
        }
        else {
            // plain name
            return filename;
        }
    }

    private static boolean isExistingFilePath(HttpServletResponse response, Path filePath) throws IOException {
        if (filePath == null || Files.notExists(filePath)) {
            return false;
        }
        return true;
    }

}
