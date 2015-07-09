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
package org.bonitasoft.web.designer.workspace;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.bonitasoft.web.designer.migration.LiveMigration;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

public class WorkspaceInitializerTest {

    private static final String WAR_BASE_PATH = "src/main/webapp";

    @Mock
    private Workspace workspace;

    @Mock
    LiveMigration<Page> pageLiveMigration;

    @Mock
    LiveMigration<Widget> widgetLiveMigration;

    @InjectMocks
    private WorkspaceInitializer workspaceInitializer;

    @Before
    public void initializeWorkspaceInitializer() {
        initMocks(this);
        workspaceInitializer.setServletContext(new MockServletContext(WAR_BASE_PATH, new FileSystemResourceLoader()));
    }

    @Test
    public void should_initialize_workspace() throws Exception {
        workspaceInitializer.contextInitialized();
        verify(workspace).initialize();
    }

    @Test
    public void should_start_page_live_migration() throws Exception {
        workspaceInitializer.contextInitialized();
        verify(pageLiveMigration).start();
    }

    @Test
    public void should_start_widget_live_migration() throws Exception {
        workspaceInitializer.contextInitialized();
        verify(widgetLiveMigration).start();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtimeException_if_error_occurs_while_initializing_workspace() throws Exception {
        doThrow(new IOException()).when(workspace).initialize();
        workspaceInitializer.contextInitialized();
    }
}
