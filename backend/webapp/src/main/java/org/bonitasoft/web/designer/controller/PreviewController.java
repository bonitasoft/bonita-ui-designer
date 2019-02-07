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

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Controller
public class PreviewController {

    protected static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    private PageRepository pageRepository;
    private Previewer previewer;
    private Path widgetRepositoryPath;
    private Path pageRepositoryPath;

    @Inject
    public PreviewController(PageRepository pageRepository,
            Previewer previewer,
            @Named("widgetPath") Path widgetRepositoryPath,
            @Named("pagesPath") Path pageRepositoryPath) {
        this.pageRepository = pageRepository;
        this.previewer = previewer;
        this.widgetRepositoryPath = widgetRepositoryPath;
        this.pageRepositoryPath = pageRepositoryPath;
    }

    /**
     * Send redirect to the Rest API
     */
    @RequestMapping("/preview/{previewableType}/{appName}/API/**")
    public void proxyAPICall(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            String queryString = isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString();
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.addHeader("Location", request.getContextPath() + "/API/" + RequestMappingUtils.extractPathWithinPattern(request) + queryString);

            response.flushBuffer();
        } catch (IOException e) {
            String message = "Error while redirecting API call";
            logger.error(message, e);
            throw new ServletException(message, e);
        }
    }

    @RequestMapping(value = "/preview/page/{appName}/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewPage(@PathVariable(value = "id") String id, HttpServletRequest httpServletRequest) {
        return previewer.render(id, pageRepository, httpServletRequest);
    }

    @RequestMapping("/preview/page/{appName}/{id}/assets/**")
    public void servePageAsset(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String pageId) throws IOException {
        String matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        Path filePath = pageRepositoryPath.resolve(pageId).resolve("assets").resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    @RequestMapping("/preview/{previewableType}/{appName}/{id}/widgets/**")
    public void serveWidgetFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        HttpFile.writeFileInResponse(request, response, widgetRepositoryPath.resolve(matchingPath));
    }

    @RequestMapping("/preview/page/{appName}/{id}/js/**")
    public void servePageJs(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String pageId) throws IOException {
        String matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        Path filePath = pageRepositoryPath.resolve(pageId).resolve("js").resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    /**
     * Returns the theme.css file present on the application
     */
    @RequestMapping("/preview/{previewableType}/{appName}/theme/theme.css")
    public void serveWidgetAsset(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "appName") String appName) throws ServletException {

        if (!appName.equals("no-app-selected")) {
            try {
                response.setContentType("text/css");
                response.sendRedirect(request.getContextPath() + "/apps/" + appName + "/theme/theme.css");
            } catch (IOException ie) {

            }
        } else {
            response.setHeader("Content-Type", "text/css");
            response.setHeader("Content-Disposition", "inline; filename=\"theme.css\"");
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setStatus(HttpStatus.OK.value());
            try (PrintWriter writer = response.getWriter()) {
                writer.println("/**");
                writer.println("* Living application theme");
                writer.print("*/");
            } catch (IOException e) {
                // fail silently
            }
        }
    }

}
