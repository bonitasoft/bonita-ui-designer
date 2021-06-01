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


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;

@Getter
@RequiredArgsConstructor
public class Previewer {

    private final HtmlGenerator generator;

    /**
     * Build a preview for a previewable
     */
    public <T extends Previewable & Identifiable> String render(T previewable, String resourceContext) {
        return generator.generateHtml(previewable, resourceContext);
    }

}
