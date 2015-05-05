/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller.export.steps;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.controller.export.properties.PagePropertiesBuilder;
import org.bonitasoft.web.designer.model.page.Page;

@Named
public class PagePropertiesExportStep implements ExportStep<Page> {

    private PagePropertiesBuilder pagePropertiesBuilder;

    @Inject
    public PagePropertiesExportStep(PagePropertiesBuilder pagePropertiesBuilder) {
        this.pagePropertiesBuilder = pagePropertiesBuilder;
    }

    @Override
    public void execute(Zipper zipper, Page page) throws IOException {
        byte[] pageProperties = pagePropertiesBuilder.build(page);
        zipper.addToZip(pageProperties, "page.properties");
    }

}
