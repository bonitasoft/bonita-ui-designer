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
package org.bonitasoft.web.designer.studio.repository;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.file.Path;
import java.util.Arrays;
import javax.inject.Inject;

import org.bonitasoft.web.designer.ApplicationConfig;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.studio.workspace.LockedResourceException;
import org.bonitasoft.web.designer.studio.workspace.ResourceNotFoundException;
import org.bonitasoft.web.designer.studio.workspace.WorkspaceResourceHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author Romain Bioteau
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class, DesignerConfig.class })
@WebAppConfiguration("src/test/resources")
@ActiveProfiles(profiles = "studio")
public class RepositoryAspectTest {

    @Inject
    @InjectMocks
    private RepositoryAspect repositoryAspect;

    @Inject
    private PageRepository formRepository;

    @Inject
    private WidgetRepository widgetRepository;

    @Mock(name = "handler")
    private WorkspaceResourceHandler workspaceResourceHandler;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void should_trigger_postSave_on_workspaceResourceHandler_when_saving_on_formRepository() throws Exception {
        formRepository.save(aPage().withId("aPageId").build());

        verify(workspaceResourceHandler, times(1)).postSave(formRepository.resolvePath("aPageId"));
    }

    @Test
    public void should_trigger_postSave_on_workspaceResourceHandler_when_saving_and_updating_last_update_date_on_formRepository() throws Exception {
        formRepository.updateLastUpdateAndSave(aPage().withId("aPageId").build());

        verify(workspaceResourceHandler, times(1)).postSave(formRepository.resolvePath("aPageId"));
    }

    @Test
    public void should_not_trigger_postSave_on_workspaceResourceHandler_when_saving_several_widgets_in_widgetRepository() throws Exception {
        widgetRepository.saveAll(Arrays.asList(
                        aWidget().id("widgetId1").build(),
                        aWidget().id("widgetId2").build())
        );

        verify(workspaceResourceHandler, never()).postSave(any(Path.class));
    }

    @Test
    public void should_trigger_postSave_on_workspaceResourceHandler_when_saving_widget_on_widgetRepository() throws Exception {
        widgetRepository.updateLastUpdateAndSave(
                aWidget().id("widgetId1").build()
        );

        verify(workspaceResourceHandler).postSave(any(Path.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_an_IllegalArgumentException_when_saving_a_null_reference() throws Exception {
        formRepository.updateLastUpdateAndSave((Page) null);
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_a_RepositoryException_when_saving_if_resource_is_not_found() throws Exception {
        doThrow(ResourceNotFoundException.class).when(workspaceResourceHandler).postSave(any(Path.class));

        formRepository.updateLastUpdateAndSave(aPage().withId("a_form_id").build());
    }

    @Test
    //"Test reversed until we find a solution for BS-14120"
    public void should_NOT_trigger_preOpen_workspaceResourceHandler_when_getting_a_form() throws Exception {
        formRepository.save(aPage().withId("aPageId").build());

        formRepository.get("aPageId");

        verify(workspaceResourceHandler, never()).preOpen(formRepository.resolvePath("aPageId"));
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore("Test ignored until we find a solution for BS-14120")
    public void should_throw_an_IllegalArgumentException_when_getting_a_form_with_a_null_id() throws Exception {
        formRepository.get(null);
    }

    @Test(expected = RepositoryException.class)
    @Ignore("Test ignored until we find a solution for BS-14120")
    public void should_throw_a_RepositoryException_when_getting_a_missing_resource() throws Exception {
        doThrow(ResourceNotFoundException.class).when(workspaceResourceHandler).preOpen(any(Path.class));

        formRepository.get("a_form_id");
    }

    @Test(expected = RepositoryException.class)
    @Ignore("Test ignored until we find a solution for BS-14120")
    public void should_throw_a_RepositoryException_when_getting_a_locked_resource() throws Exception {
        doThrow(LockedResourceException.class).when(workspaceResourceHandler).preOpen(any(Path.class));

        formRepository.get("a_form_id");
    }

    @Test
    public void should_trigger_postDelete_on_workspaceResourceHandler_when_deleting_a_form() throws Exception {
        formRepository.save(aPage().withId("aPageId").build());

        formRepository.delete("aPageId");

        verify(workspaceResourceHandler).postDelete(formRepository.resolvePath("aPageId"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_an_IllegalArgumentException_when_deleting_a_form_with_a_null_id() throws Exception {
        formRepository.delete(null);
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_a_RepositoryException_when_deleting_a_missing_resource() throws Exception {
        doThrow(ResourceNotFoundException.class).when(workspaceResourceHandler).postDelete(any(Path.class));

        formRepository.delete("a_form_id");
    }
}
