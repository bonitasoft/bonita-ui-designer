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
package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.DefaultWidgetService;
import org.bonitasoft.web.designer.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.net.URISyntaxException;
import java.util.Collections;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MigrationResourceTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private MockMvc mockMvc;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageService pageService;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private UiDesignerProperties uiDesignerProperties;

    @Mock
    private DefaultWidgetService widgetService;

    @InjectMocks
    private MigrationResource MigrationResource;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        lenient().when(uiDesignerProperties.getModelVersion()).thenReturn("2.0");
        mockMvc = mockServer(MigrationResource).build();
    }

    @Test
    void should_return_artifact_status_when_migration_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.10.0").withPreviousDesignerVersion("1.9.0").build();
        Widget widget = WidgetBuilder.aWidget().withId("myWidget").designerVersion("1.10.0").previousDesignerVersion("1.9.0").build();

        // with json
        ResultActions result = postStatusRequest(page);
        String resultType = result.andReturn().getResponse().getContentType();
        assertThat(resultType.startsWith(MediaType.APPLICATION_JSON.toString())).isTrue();
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        when(pageRepository.get("myPage"))
                .thenReturn(aPage().withId("myPage").withName("myPage").withDesignerVersion("1.10.0").build());
        when(pageService.getStatus(page)).thenReturn(new ArtifactStatusReport(true, true));

        result = getStatusRequestById("page", "myPage");
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        Widget widget2 = aWidget().withId("myWidget").withName("myWidget").designerVersion("1.10.0").build();
        when(widgetRepository.get("myWidget"))
                .thenReturn(widget2);
        when(widgetService.getStatus(widget2)).thenReturn(new ArtifactStatusReport(true, true));
        result = getStatusRequestById("widget", "myWidget");
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_artifact_status_when_no_migration_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Widget widget = WidgetBuilder.aWidget().withId("myWidget").modelVersion("2.0").build();

        // with json
        ResultActions result = postStatusRequest(page);
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        Page page2 = aPage().withId("myPage").withName("myPage").isMigration(false).withModelVersion("2.0").build();
        when(pageRepository.get("myPage"))
                .thenReturn(page2);
        when(pageService.getStatus(page2)).thenReturn(new ArtifactStatusReport(true, false));
        result = getStatusRequestById("page", "myPage");
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        Widget widget2 = aWidget().withId("myWidget").withName("myWidget").isMigration(false).designerVersion("2.0").build();
        when(widgetRepository.get("myWidget")).thenReturn(widget2);
        when(widgetService.getStatus(widget2)).thenReturn(new ArtifactStatusReport(true, false));
        result = getStatusRequestById("widget", "myWidget");
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_artifact_status_when_not_compatible_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.1").build();
        Widget widget = WidgetBuilder.aWidget().withId("myWidget").modelVersion("2.1").build();

        // with json
        ResultActions result = postStatusRequest(page);
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        when(pageRepository.get("myPage"))
                .thenReturn(aPage().withId("myPage").withName("myPage").isCompatible(false).isMigration(false).withModelVersion("2.1").build());
        when(pageRepository.get("myPage"))
                .thenReturn(page);
        when(pageService.getStatus(page)).thenReturn(new ArtifactStatusReport(false, false));
        result = getStatusRequestById("page", "myPage");
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        when(widgetRepository.get("myWidget"))
                .thenReturn(aWidget().withId("myWidget").withName("myWidget").isCompatible(false).isMigration(false).designerVersion("2.1").build());
        when(widgetRepository.get("myWidget")).thenReturn(widget);
        when(widgetService.getStatus(widget)).thenReturn(new ArtifactStatusReport(false, false));
        result = getStatusRequestById("widget", "myWidget");
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_bad_request_when_invalid_artifact_json() throws Exception {
        postStatusBadRequest();
    }

    @Test
    void should_return_not_found_when_invalid_artifact_id() throws Exception {
        when(pageRepository.get("invalidPageId"))
                .thenThrow(new NotFoundException());
        getStatusRequestByIdInvalid("page", "invalidPageId");

        when(widgetRepository.get("invalidWidgetId"))
                .thenThrow(new NotFoundException());
        getStatusRequestByIdInvalid("widget", "invalidWidgetId");
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_to_migrate() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        // Get dependencies status
        when(pageService.getStatus(page)).thenReturn(new ArtifactStatusReport(true, true));

        ResultActions result = getStatusRequestByIdRecursive("page", "myPage");
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_not_compatible() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        // Get dependencies status
        when(pageService.getStatus(page)).thenReturn(new ArtifactStatusReport(false, false));

        ResultActions result = getStatusRequestByIdRecursive("page", "myPage");
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_not_to_migrate() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        // Get dependencies status
        when(pageService.getStatus(page)).thenReturn(new ArtifactStatusReport(true, false));

        ResultActions result = getStatusRequestByIdRecursive("page", "myPage");
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    private ResultActions postStatusRequest(DesignerArtifact artifact) throws Exception {
        return mockMvc
                .perform(post("/rest/migration/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(artifact)))
                .andExpect(status().isOk());
    }

    private void postStatusBadRequest() throws Exception {
        mockMvc
                .perform(post("/rest/migration/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"id\":\"test\"\"name\":\"test\",\"rows\":[],\"assets\":[],\"type\" : \"page\"}"))
                .andExpect(status().isBadRequest());
    }

    private ResultActions getStatusRequestById(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s", artifactType, artifactId);
        return mockMvc
                .perform(get(url))
                .andExpect(status().isOk());
    }

    private ResultActions getStatusRequestByIdRecursive(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s?recursive=true", artifactType, artifactId);
        return mockMvc
                .perform(get(url))
                .andExpect(status().isOk());
    }

    private void getStatusRequestByIdInvalid(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s", artifactType, artifactId);
        mockMvc
                .perform(get(url))
                .andExpect(status().isNotFound());
    }

    private String getStatusReport(boolean compatible, boolean migration) {
        return new ArtifactStatusReport(compatible, migration).toString();
    }

    @Test
    void should_return_200_when_page_migration_is_done_on_success() throws Exception {
        Page pageToMigrate = aPage().withId("my-page-to-migrate").withName("page-name").build();
        when(pageRepository.get("my-page-to-migrate")).thenReturn(pageToMigrate);
        when(pageService.migrateWithReport(pageToMigrate)).thenReturn(new MigrationResult<>(pageToMigrate, Collections.singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "my-page-to-migrate"))));

        mockMvc
                .perform(
                        put("/rest/migration/page/my-page-to-migrate").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(pageService).migrateWithReport(pageToMigrate);
    }

    @Test
    void should_return_200_when_migration_is_finished_with_warning() throws Exception {
        Page pageToMigrate = aPage().withId("my-page-to-migrate").withName("page-name").build();
        when(pageRepository.get("my-page-to-migrate")).thenReturn(pageToMigrate);
        when(pageService.migrateWithReport(pageToMigrate)).thenReturn(new MigrationResult<>(pageToMigrate, Collections.singletonList(new MigrationStepReport(MigrationStatus.WARNING, "my-page-to-migrate"))));

        mockMvc
                .perform(
                        put("/rest/migration/page/my-page-to-migrate").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(pageService).migrateWithReport(pageToMigrate);
    }

    @Test
    void should_return_500_when_an_error_occurs_during_page_migration() throws Exception {
        Page pageToMigrate = aPage().withId("my-page-to-migrate").withDesignerVersion("1.1.9").withName("page-name").build();
        when(pageRepository.get("my-page-to-migrate")).thenReturn(pageToMigrate);
        when(pageService.migrateWithReport(pageToMigrate)).thenReturn(new MigrationResult<>(pageToMigrate, Collections.singletonList(new MigrationStepReport(MigrationStatus.ERROR, "my-page-to-migrate"))));
        when(pageService.getStatus(pageToMigrate)).thenReturn(new ArtifactStatusReport(true, true));

        mockMvc
                .perform(
                        put("/rest/migration/page/my-page-to-migrate").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(500));

        verify(pageService).migrateWithReport(pageToMigrate);
    }


    @Test
    void should_return_200_when_widget_migration_is_done_on_success() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.migrateWithReport(widget)).thenReturn(new MigrationResult<>(widget,
                Collections.singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "my-widget"))));

        mockMvc
                .perform(
                        put("/rest/migration/widget/my-widget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(widgetService).migrateWithReport(widget);
    }

    @Test
    void should_return_200_when_widget_migration_is_finish_with_warning() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.migrateWithReport(widget)).thenReturn(new MigrationResult<>(widget,
                Collections.singletonList(new MigrationStepReport(MigrationStatus.WARNING, "my-widget"))));

        mockMvc
                .perform(
                        put("/rest/migration/widget/my-widget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(widgetService).migrateWithReport(widget);
    }

    @Test
    void should_return_500_when_an_error_occurs_during_widget_migration() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.migrateWithReport(widget)).thenReturn(new MigrationResult<>(widget,
                Collections.singletonList(new MigrationStepReport(MigrationStatus.ERROR, "my-widget"))));

        mockMvc
                .perform(
                        put("/rest/migration/widget/my-widget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(500));

        verify(widgetService).migrateWithReport(widget);
    }

    @Test
    void should_return_404_when_migration_is_trigger_but_page_id_doesnt_exist() throws Exception {
        when(pageRepository.get("unknownPage")).thenThrow(new NotFoundException());

        mockMvc
                .perform(
                        put("/rest/migration/page/unknownPage").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(404));
    }

    @Test
    void should_return_404_when_migration_is_trigger_but_widget_id_doesnt_exist() throws Exception {
        when(widgetRepository.get("unknownWidget")).thenThrow(new NotFoundException());

        mockMvc
                .perform(
                        put("/rest/migration/widget/unknownWidget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(404));
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_page_is_incompatible() throws Exception {
        Page pageToMigrate = aPage().withId("my-page-to-migrate").withModelVersion("3.0").withName("page-name").isCompatible(false).build();
        when(pageRepository.get("my-page-to-migrate")).thenReturn(pageToMigrate);
        when(pageService.getStatus(pageToMigrate)).thenReturn(new ArtifactStatusReport(false, false));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/page/my-page-to-migrate").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"Artifact is incompatible with actual version\",\"status\":\"incompatible\",\"elementId\":\"my-page-to-migrate\",\"migrationStepReport\":[]}");

        verify(pageService, never()).migrateWithReport(pageToMigrate);
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_page_not_needed_migration() throws Exception {
        Page pageToMigrate = aPage().withId("my-page-to-migrate").withModelVersion("2.0.").withName("page-name").build();
        when(pageRepository.get("my-page-to-migrate")).thenReturn(pageToMigrate);
        when(pageService.getStatus(pageToMigrate)).thenReturn(new ArtifactStatusReport(true, false));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/page/my-page-to-migrate").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"No migration is needed\",\"status\":\"none\",\"elementId\":\"my-page-to-migrate\",\"migrationStepReport\":[]}");
        verify(pageService, never()).migrateWithReport(pageToMigrate);
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_widget_version_is_incompatible() throws Exception {
        Widget widget = aWidget().withId("my-widget").modelVersion("3.0").custom().isCompatible(false).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.getStatus(widget)).thenReturn(new ArtifactStatusReport(false, true));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/widget/my-widget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"Artifact is incompatible with actual version\",\"status\":\"incompatible\",\"elementId\":\"my-widget\",\"migrationStepReport\":[]}");
        verify(widgetService, never()).migrateWithReport(widget);
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_widget_not_needed_migration() throws Exception {
        Widget widget = aWidget().withId("my-widget").modelVersion("2.0").custom().isMigration(false).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.getStatus(widget)).thenReturn(new ArtifactStatusReport(true, false));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/widget/my-widget").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"No migration is needed\",\"status\":\"none\",\"elementId\":\"my-widget\",\"migrationStepReport\":[]}");
        verify(widgetService, never()).migrateWithReport(widget);
}
}
