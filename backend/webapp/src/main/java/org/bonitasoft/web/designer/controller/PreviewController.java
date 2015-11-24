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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PreviewController {

    protected static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    private PageRepository pageRepository;
    private Previewer previewer;
    private AssetRepository<Page> pageAssetService;
    private AssetRepository<Widget> widgetAssetService;
    private RequestMappingUtils requestMappingUtils;

    @Inject
    public PreviewController(PageRepository pageRepository,
                             Previewer previewer,
                             AssetRepository<Page> pageAssetService,
                             AssetRepository<Widget> widgetAssetService,
                             RequestMappingUtils requestMappingUtils) {
        this.pageRepository = pageRepository;
        this.previewer = previewer;
        this.pageAssetService = pageAssetService;
        this.widgetAssetService = widgetAssetService;
        this.requestMappingUtils = requestMappingUtils;
    }

    /**
     * Send redirect to the Rest API
     */
    @RequestMapping("/preview/page/API/**")
    public void proxyAPICall(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        try {
            String queryString = isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString();
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.addHeader("Location", "/bonita/API/" + requestMappingUtils.extractPathWithinPattern(request) + queryString);
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

    /**
     * A page can serve its own assets or assets linked to its widgets
     */
    @RequestMapping("/preview/page/{id}/assets/{type}/{filename:.*}")
    public void servePageAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("id") String id,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename) throws ServletException {

        try {
            Path filePath = pageAssetService.findAssetPath(id, filename, AssetType.getAsset(type));
            HttpFile.writeFileInResponse(request, response, filePath);
        } catch (IOException e) {
            logger.error("Error when loading page asset", e);
            throw new ServletException("Error when loading page asset", e);
        }

    }

    /**
     * A page can serve its own assets or assets linked to its widgets
     */
    @RequestMapping("/preview/page/{id}/widgets/{widgetId}/assets/{type}/{filename:.*}")
    public void serveWidgetAssetIncludedInPage(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("id") String id,
            @PathVariable("widgetId") String widgetId,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename) throws ServletException {

        serveWidgetAsset(request, response, widgetId, type, filename);
    }

    /**
     * A widget can only serve its own assets
     */
    @RequestMapping("/preview/widget/{id}/assets/{type}/{filename:.*}")
    public void serveWidgetAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("id") String id,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename) throws ServletException {

        try {
            Path filePath = widgetAssetService.findAssetPath(id, filename, AssetType.getAsset(type));
            HttpFile.writeFileInResponse(request, response, filePath);

        } catch (IOException e) {
            logger.error("Error when loading widget asset", e);
            throw new ServletException("Error when loading widget asset", e);
        }

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
