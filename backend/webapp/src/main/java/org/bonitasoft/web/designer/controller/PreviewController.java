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

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PreviewController {

    protected static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

    @Inject
    private PageRepository pageRepository;

    @Inject
    private Previewer previewer;

    @Inject
    private AssetRepository<Page> pageAssetUploader;

    @RequestMapping(value = "/preview/page/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewPage(@PathVariable(value = "id") String id, HttpServletRequest httpServletRequest) {
        return previewer.render(id, pageRepository, httpServletRequest);
    }

    @RequestMapping("/preview/page/{id}/assets/{type}/{filename:.*}")
    public void servePageAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("id") String id,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename) throws ServletException {

        try {
            HttpFile.writeFileInResponse(
                    request,
                    response,
                    pageAssetUploader.findAssetPath(id, filename, AssetType.getAsset(type)));
        } catch (IOException e) {
            logger.error("Error on widget generation", e);
            throw new ServletException("Error on widget generation", e);
        }

    }
}
