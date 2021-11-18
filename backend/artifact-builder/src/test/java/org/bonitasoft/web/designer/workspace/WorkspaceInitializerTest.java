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

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.migration.LiveRepositoryUpdate;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.angular.AngularAppGenerator;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceInitializerTest {

    @Rule
    public TemporaryFolder workspacePath = new TemporaryFolder();
    @Rule
    public TemporaryFolder workspaceUIDPath = new TemporaryFolder();

    @Mock
    private LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate;

    @Mock
    private LiveRepositoryUpdate<Widget> widgetRepositoryLiveUpdate;

    @Mock
    AngularAppGenerator<Page> angularAppGenerator;

    UiDesignerProperties uiDesignerProperties;

    private Workspace workspace;

    @Before
    public void setUp() throws IOException {

        uiDesignerProperties = new UiDesignerPropertiesBuilder()
                .workspacePath(workspacePath.toPath())
                .workspaceUidPath(workspaceUIDPath.toPath()).build();

        WidgetRepository widgetRepository = mock(WidgetRepository.class);
        when(widgetRepository.resolvePath(anyString())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return uiDesignerProperties.getWorkspace().getWidgets().getDir().resolve(id);
        });
        PageRepository pageRepository = mock(PageRepository.class);
        WidgetDirectiveBuilder widgetDirectiveBuilder = mock(WidgetDirectiveBuilder.class);
        FragmentDirectiveBuilder fragmentDirectiveBuilder = mock(FragmentDirectiveBuilder.class);
        AssetDependencyImporter<Widget> widgetAssetDependencyImporter = mock(AssetDependencyImporter.class);

        ResourcesCopier resourcesCopier = new ResourcesCopier();
        List<LiveRepositoryUpdate> migrations = List.of(widgetRepositoryLiveUpdate,pageRepositoryLiveUpdate);
        JsonHandler jsonHandler = new JsonHandlerFactory().create();

        workspace = spy(new Workspace(
                uiDesignerProperties,
                widgetRepository,
                pageRepository,
                widgetDirectiveBuilder,
                fragmentDirectiveBuilder,
                widgetAssetDependencyImporter,
                resourcesCopier,
                migrations,
                jsonHandler,
                angularAppGenerator
        ));

    }

    @Test
    public void should_initialize_workspace() throws Exception {
        uiDesignerProperties.setExperimental(true);
        // When
        workspace.initialize();
        // Then
        verify(workspace).doInitialize();
        verify(workspace).cleanPageWorkspace();
        Page genericPage = PageBuilder.aPage().withId(Workspace.GENERIC_ANGULAR_PAGE_ID).withModelVersion("3.0").build();
        verify(angularAppGenerator).generateAngularApp(genericPage);
        verify(angularAppGenerator).generatedAppInstallStart(genericPage);
        assertThat(workspace.initialized).isTrue();
    }

    @Test
    public void should_start_page_live_migration() throws Exception {
        // When
        workspace.initialize();
        // Then
        verify(pageRepositoryLiveUpdate).start();
    }

    @Test
    public void should_start_widget_live_migration() throws Exception {
        // When
        workspace.initialize();
        // Then
        verify(widgetRepositoryLiveUpdate).start();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtimeException_if_error_occurs_while_initializing_workspace() throws Exception {
        doThrow(new IOException()).when(workspace).doInitialize();
        // When
        workspace.initialize();
        // Then
    }
}
