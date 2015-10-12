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
package org.bonitasoft.web.designer.controller.preview;


import static org.apache.commons.lang3.StringUtils.isBlank;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Named
public class Previewer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Previewer.class);

    @Inject
    private HtmlGenerator generator;

    /**
     * Build a preview for a previewable
     */
    public <T extends Previewable & Identifiable> ResponseEntity<String> render(String id, Repository<T> repository, HttpServletRequest httpServletRequest) {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Need to specify the id of the page to preview.");
        }

        try {
            String html = generator.generateHtml(getResourceContext(httpServletRequest),repository.get(id));
            return new ResponseEntity<>(html, HttpStatus.OK);
        } catch (GenerationException e) {
            LOGGER.error("Error during page generation", e);
            return new ResponseEntity<>("Error during page generation", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("Page <" + id + "> not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Determines the resourceContext if it isn't defined
     */
    private String getResourceContext(HttpServletRequest httpServletRequest) {
        return StringUtils.isEmpty(httpServletRequest.getContextPath()) ? "/runtime/" : httpServletRequest.getContextPath() + "/runtime/";
    }

}
