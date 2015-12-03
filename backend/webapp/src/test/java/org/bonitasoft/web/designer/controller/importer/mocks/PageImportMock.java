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
package org.bonitasoft.web.designer.controller.importer.mocks;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;

public class PageImportMock {

    private JsonFileBasedLoader<Page> pageLoader;
    private Path unzippedPath;

    public PageImportMock(Path unzippedPath, JsonFileBasedLoader<Page> pageLoader) {
        this.pageLoader = pageLoader;
        this.unzippedPath = unzippedPath;
    }

    public Page mockPageToBeImported() {
        return mockPageToBeImported(aPage().withId("id"));
    }

    public Page mockPageToBeImported(PageBuilder pageBuilder) {
        Page page = pageBuilder.build();
        when(pageLoader.load(unzippedPath, "page.json")).thenReturn(page);
        return page;
    }
}
