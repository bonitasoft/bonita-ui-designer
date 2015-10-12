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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.config.WebMvcConfiguration.BACKEND_RESOURCES;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.springframework.core.io.ResourceLoader;

@Named
public class HtmlExportStep implements ExportStep<Page> {

    private HtmlGenerator generator;
    private ResourceLoader resourceLoader;

    @Inject
    public HtmlExportStep(HtmlGenerator generator, ResourceLoader resourceLoader) {
        this.generator = generator;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void execute(Zipper zipper, Page page) throws IOException {
        zipper.addDirectoryToZip(Paths.get(resourceLoader.getResource(BACKEND_RESOURCES + "runtime").getURI()), RESOURCES);

        byte[] html = generator.generateHtml(page).getBytes(StandardCharsets.UTF_8);
        zipper.addToZip(html, RESOURCES + "/index.html");
    }
}
