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
package org.bonitasoft.web.designer.controller.export.steps.angular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.rendering.AssetHtmlBuilder;
import org.bonitasoft.web.designer.rendering.angular.AngularAppGenerator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;
import static java.nio.file.Paths.get;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
@Slf4j
@RequiredArgsConstructor
public class AngularAppExportStep implements ExportStep<Page> {

    private final AngularAppGenerator angularAppGenerator;

    @Override
    public void execute(Zipper zipper, Page page) throws IOException {
        //angularAppGenerator.generateAngularApp(page);
        log.info("AngularAppGeneratorStep: Not yet implemented !");
    }

}
