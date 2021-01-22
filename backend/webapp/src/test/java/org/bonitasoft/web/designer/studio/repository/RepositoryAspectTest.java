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

import java.nio.file.Path;
import java.util.Arrays;

import javax.inject.Inject;

import org.bonitasoft.web.designer.Main;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.studio.workspace.LockedResourceException;
import org.bonitasoft.web.designer.studio.workspace.ResourceNotFoundException;
import org.bonitasoft.web.designer.studio.workspace.RestClient;
import org.bonitasoft.web.designer.studio.workspace.WorkspaceResourceHandler;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Romain Bioteau
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
@ActiveProfiles(profiles = "studio")
public class RepositoryAspectTest {

    @Inject
    private PageRepository formRepository;

    @Inject
    private WidgetRepository widgetRepository;

    @MockBean(name = "handler")
    private WorkspaceResourceHandler workspaceResourceHandler;

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
    public void should_trigger_delete_on_workspaceResourceHandler_when_deleting_a_widget() throws Exception {
        widgetRepository.save(aWidget().id("widgetId1").custom().build());

        widgetRepository.delete("widgetId1");

        verify(workspaceResourceHandler).delete(widgetRepository.resolvePath("widgetId1"));
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
    public void should_trigger_delete_on_workspaceResourceHandler_when_deleting_a_form() throws Exception {
        formRepository.save(aPage().withId("aPageId").build());

        formRepository.delete("aPageId");

        verify(workspaceResourceHandler).delete(formRepository.resolvePath("aPageId"));
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
