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

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import java.time.Instant;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.Path;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;


public class PageImportMock {

    private PageRepository pageRepositoryMock;
    private JsonHandler jsonHandlerMock;
    private Path unzippedPath;

    public PageImportMock(Path unzippedPath, PageRepository pageRepositoryMock, JsonHandler jsonHandlerMock) {
        this.pageRepositoryMock = pageRepositoryMock;
        this.jsonHandlerMock = jsonHandlerMock;
        this.unzippedPath = unzippedPath;
    }

    public Page mockPageToBeImported() throws IOException {
        return mockPageToBeImported(aPage().withId("id"));
    }

    public Page mockPageToBeImported(PageBuilder pageBuilder) throws IOException {
        Page page = pageBuilder.build();
        lenient().doReturn(page).when(jsonHandlerMock).fromJson(any(Path.class), eq(Page.class), eq(JsonViewPersistence.class));
        lenient().when(pageRepositoryMock.updateLastUpdateAndSave(page)).thenAnswer((Answer<Page>) invocationOnMock -> {
            Page pageArg =  invocationOnMock.getArgument(0);
            pageArg.setLastUpdate(Instant.now());
            return pageArg;
        });
        return page;
    }
}
