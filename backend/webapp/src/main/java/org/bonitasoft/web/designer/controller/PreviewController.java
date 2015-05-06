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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PreviewController {

    @Inject
    private PageRepository pageRepository;

    @Inject
    private Previewer previewer;

    @RequestMapping(value = "/preview/page/{id}", produces = "text/html; charset=UTF-8")
    public ResponseEntity<String> previewPage(@PathVariable(value = "id") String id, HttpServletRequest httpServletRequest) {
        return previewer.render(id, pageRepository, httpServletRequest);
    }
}
