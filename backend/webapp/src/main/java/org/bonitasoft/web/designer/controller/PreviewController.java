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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PreviewController {

    public static final String JS_FOLDER = "js";

    protected static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    private final PageRepository pageRepository;

    private final ArtifactBuilder artifactBuilder;

    private final Path widgetRepositoryPath;

    private final Path fragmentRepositoryPath;

    private final Path pageRepositoryPath;

    private final WorkspaceUidProperties workspaceUidProperties;

    private final FragmentRepository fragmentRepository;

    @Autowired
    public PreviewController(PageRepository pageRepository,
                             FragmentRepository fragmentRepository,
                             ArtifactBuilder artifactBuilder,
                             WorkspaceProperties workspaceProperties, WorkspaceUidProperties workspaceUidProperties) {
        this.pageRepository = pageRepository;
        this.fragmentRepository = fragmentRepository;
        this.artifactBuilder = artifactBuilder;

        this.widgetRepositoryPath = workspaceProperties.getWidgets().getDir();
        this.fragmentRepositoryPath = workspaceProperties.getFragments().getDir();
        this.pageRepositoryPath = workspaceProperties.getPages().getDir();
        this.workspaceUidProperties = workspaceUidProperties;
    }

    /**
     * Determines the resourceContext if it isn't defined
     */
    private String getResourceContext(HttpServletRequest httpServletRequest) {
        var contextPath = httpServletRequest.getContextPath();
        return isEmpty(contextPath) ? "/runtime/" : contextPath + "/runtime/";
    }

    /**
     * Send redirect to the Rest API
     */
    @RequestMapping("/preview/{previewableType}/{appName}/API/**")
    public void proxyAPICall(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            var queryString = isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString();
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.addHeader("Location", request.getContextPath() + "/API/" + RequestMappingUtils.extractPathWithinPattern(request) + queryString);

            response.flushBuffer();
        } catch (IOException e) {
            var message = "Error while redirecting API call";
            logger.error(message, e);
            throw new ServletException(message, e);
        }
    }

    @RequestMapping(value = "/preview/page/{appName}/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewPage(@PathVariable(value = "id") String id, HttpServletRequest httpServletRequest) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Need to specify the id of the page to preview.");
        }
        try {
            var resourceContext = getResourceContext(httpServletRequest);
            var page = pageRepository.get(id);
            var html = artifactBuilder.buildHtml(page, resourceContext);
            return ResponseEntity.ok(html);
        } catch (GenerationException e) {
            var message = "Error during page generation";
            logger.error(message, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(message);
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body("Page <" + id + "> not found");
        }
    }

    @RequestMapping("/preview/page/{appName}/{id}/assets/**")
    public void servePageAsset(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String pageId) throws IOException {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        var filePath = pageRepositoryPath.resolve(pageId).resolve("assets").resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    @RequestMapping("/preview/{previewableType}/{appName}/{id}/widgets/**")
    public void serveWidgetFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        HttpFile.writeFileInResponse(request, response, widgetRepositoryPath.resolve(matchingPath));
    }

    @RequestMapping("/preview/page/{appName}/{id}/js/**")
    public void servePageJs(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String pageId) throws IOException {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        var filePath = workspaceUidProperties.getTmpPagesRepositoryPath().resolve(pageId).resolve(JS_FOLDER).resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    @RequestMapping(value = "/preview/fragment/{appName}/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewFragment(@PathVariable(value = "id") String id, HttpServletRequest
            httpServletRequest) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Need to specify the id of the page to preview.");
        }
        try {
            var resourceContext = getResourceContext(httpServletRequest);
            var fragment = fragmentRepository.get(id);
            var html = artifactBuilder.buildHtml(fragment, resourceContext);
            return ResponseEntity.ok(html);
        } catch (GenerationException e) {
            var message = "Error during fragment generation";
            logger.error(message, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(message);
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body("Fragment <" + id + "> not found");
        }
    }

    @RequestMapping("/preview/fragment/{appName}/{id}/widgets*")
    public void serveFragmentWidgets(HttpServletRequest request, HttpServletResponse response, @PathVariable("id")
            String id) throws IOException {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        var filePath = workspaceUidProperties.getTmpFragmentsRepositoryPath().resolve(id).resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    @RequestMapping("/preview/page/{appName}/{id}/fragments/**")
    public void serveFragmentFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        HttpFile.writeFileInResponse(request, response, fragmentRepositoryPath.resolve(matchingPath));
    }

    /**
     * Send redirect to the Application theme resources if an application is selected
     * Else returns a default theme.css files.
     */
    @RequestMapping("/preview/{previewableType}/{appName}/theme/**")
    public void serveThemeResources(HttpServletRequest request, HttpServletResponse response, @PathVariable(value = "appName") String appName) {
        var matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        if (!appName.equals("no-app-selected")) {
            try {
                response.sendRedirect(request.getContextPath() + "/apps/" + appName + "/theme/" + matchingPath);
            } catch (IOException ie) {
                // fail silently
            }
        } else if ("theme.css".equals(matchingPath)) {
            response.setHeader("Content-Type", "text/css");
            response.setHeader("Content-Disposition", "inline; filename=\"theme.css\"");
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setStatus(OK.value());
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
