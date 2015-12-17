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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

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
    @RequestMapping("/preview/page/API/**")
    public void proxyAPICall(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            String queryString = isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString();
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.addHeader("Location", "/bonita/API/" + RequestMappingUtils.extractPathWithinPattern(request) + queryString);
            response.flushBuffer();
        } catch (IOException e) {
            String message = "Error while redirecting API call";
            logger.error(message, e);
            throw new ServletException(message, e);
        }
    }

    @RequestMapping(value = "/preview/page/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewPage(@PathVariable(value = "id") String id, HttpServletRequest httpServletRequest) {
        return previewer.render(id, pageRepository, httpServletRequest);
    }

    @RequestMapping("/preview/page/{id}/assets/**")
    public void servePageAsset(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String pageId) throws IOException {
        String matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        Path filePath = pageRepositoryPath.resolve(pageId).resolve("assets").resolve(matchingPath);
        HttpFile.writeFileInResponse(request, response, filePath);
    }

    @RequestMapping("/preview/{previableType}/{id}/widgets/**")
    public void serveWidgetFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String matchingPath = RequestMappingUtils.extractPathWithinPattern(request);
        HttpFile.writeFileInResponse(request, response, widgetRepositoryPath.resolve(matchingPath));
    }

    /**
     * Returns fake css file for living app theme.css
     */
    @RequestMapping("/preview/page/theme/theme.css")
    public void serveWidgetAsset(HttpServletResponse response) throws ServletException {

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
