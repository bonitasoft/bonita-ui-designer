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

import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class WorkspacePathResolverTest {

    private static final String WORKSPACE_KEY = "workspace";

    @Mock
    private Environment env;
    @InjectMocks
    private WorkspacePathResolver workspacePathResolver;

    @Test
    public void should_resolve_workspace_path_by_getting_system_property() throws Exception {
        Path expectedWorkspacePath = Paths.get("someUserDefinedDirectory");
        when(env.getProperty(WORKSPACE_KEY)).thenReturn(expectedWorkspacePath.toString());

        assertThat(workspacePathResolver.getWorkspacePath()).isEqualTo(expectedWorkspacePath);
    }

    @Test
    public void should_have_a_default_value_if_property_is_not_set() throws Exception {
        when(env.getProperty("user.home")).thenReturn(System.getProperty("user.home"));

        assertThat(workspacePathResolver.getWorkspacePath()).isEqualTo(Paths.get(System.getProperty("user.home") + "/.bonita"));
    }

    @Test
    public void should_retrieve_widgets_repository_path_from_environment_variable() throws Exception {
        when(env.getProperty("repository.widgets")).thenReturn("/path/to/widgets");

        assertThat(workspacePathResolver.getWidgetsRepositoryPath()).isEqualTo("/path/to/widgets");
    }

    @Test
    public void should_resolve_widgets_repository_path() throws Exception {
        when(env.getProperty("user.home")).thenReturn(System.getProperty("user.home"));

        assertThat(workspacePathResolver.getWidgetsRepositoryPath()).isEqualTo(Paths.get(System.getProperty("user.home") + "/.bonita/widgets"));
    }

    @Test
    public void should_resolve_pages_repository_path() throws Exception {
        when(env.getProperty("user.home")).thenReturn(System.getProperty("user.home"));

        assertThat(workspacePathResolver.getPagesRepositoryPath()).isEqualTo(Paths.get(System.getProperty("user.home") + "/.bonita/pages"));
    }

    @Test
    public void should_retrieve_pages_repository_path_from_environment_variable() throws Exception {
        when(env.getProperty("repository.pages")).thenReturn("path/to/pages");

        assertThat(workspacePathResolver.getPagesRepositoryPath()).isEqualTo("path/to/pages");
    }
}
