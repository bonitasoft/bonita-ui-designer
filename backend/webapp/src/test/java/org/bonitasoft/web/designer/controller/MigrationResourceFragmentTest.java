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

import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.service.FragmentService;
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

import java.util.Collections;

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MigrationResourceFragmentTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private MockMvc mockMvc;

    @InjectMocks
    private MigrationResource migrationResource;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private FragmentService fragmentService;

    @Mock
    private UiDesignerProperties uiDesignerProperties;

    @BeforeEach
    public void setUp() {
        mockMvc = mockServer(migrationResource).build();
        lenient().when(uiDesignerProperties.getModelVersion()).thenReturn("2.0");
    }

    @Test
    void should_return_200_when_fragment_migration_is_done_on_success() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().withId("my-fragment").withName("my-fragment").build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        when(fragmentService.migrateWithReport(fragment)).thenReturn(new MigrationResult<>(fragment, Collections.singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "my-fragment"))));

        mockMvc
                .perform(
                        put("/rest/migration/fragment/my-fragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(fragmentService).migrateWithReport(fragment);
    }

    @Test
    void should_return_200_when_fragment_migration_is_finished_with_warning() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().withId("my-fragment").withName("my-fragment").build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        when(fragmentService.migrateWithReport(fragment)).thenReturn(new MigrationResult<>(fragment, Collections.singletonList(new MigrationStepReport(MigrationStatus.WARNING, "my-fragment"))));

        mockMvc
                .perform(
                        put("/rest/migration/fragment/my-fragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(fragmentService).migrateWithReport(fragment);
    }

    @Test
    void should_return_500_when_an_error_occurs_during_fragment_migration() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().withId("my-fragment").withName("my-fragment").build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        when(fragmentService.migrateWithReport(fragment)).thenReturn(new MigrationResult<>(fragment, Collections.singletonList(new MigrationStepReport(MigrationStatus.ERROR, "my-fragment"))));

        mockMvc
                .perform(
                        put("/rest/migration/fragment/my-fragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(500));

        verify(fragmentService).migrateWithReport(fragment);
    }

    @Test
    void should_return_404_when_migration_is_trigger_but_fragment_id_doesnt_exist() throws Exception {
        when(fragmentRepository.get("unknownFragment")).thenThrow(new NotFoundException());

        mockMvc
                .perform(
                        put("/rest/migration/fragment/unknownFragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is(404));
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_fragment_version_is_incompatible() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().withId("my-fragment").withModelVersion("3.0").withName("my-fragment").withArtifactStatusReport(new ArtifactStatusReport(false, true)).build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        when(fragmentService.getStatus(fragment)).thenReturn(new ArtifactStatusReport(false, false));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/fragment/my-fragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"Artifact is incompatible with actual version\",\"status\":\"incompatible\",\"elementId\":\"my-fragment\",\"migrationStepReport\":[]}");
        verify(fragmentService, never()).migrateWithReport(fragment);
    }

    @Test
    void should_not_process_migration_and_return_none_status_when_fragment_not_needed_migration() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().withId("my-fragment").withModelVersion("2.0").withName("my-fragment").isMigration(false).build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        lenient().when(fragmentService.migrateWithReport(fragment)).thenReturn(new MigrationResult<>(fragment, Collections.singletonList(new MigrationStepReport(MigrationStatus.ERROR, "my-fragment"))));
        when(fragmentService.getStatus(fragment)).thenReturn(new ArtifactStatusReport(true, false));

        MvcResult result = mockMvc
                .perform(
                        put("/rest/migration/fragment/my-fragment").contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("{\"comments\":\"No migration is needed\",\"status\":\"none\",\"elementId\":\"my-fragment\",\"migrationStepReport\":[]}");
        verify(fragmentService, never()).migrateWithReport(fragment);
    }


    @Test
    void should_return_artifact_status_when_migration_required() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withDesignerVersion("1.10.0").withPreviousDesignerVersion("1.9.0").withArtifactStatusReport(new ArtifactStatusReport(true, true)).build();

        // with json
        mockMvc = mockServer(migrationResource).build();
        ResultActions result = postStatusRequest(fragment);
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        mockMvc = mockServer(migrationResource).build();
        Fragment fragment2 = aFragment().withId("myFragment").withName("myFragment").withDesignerVersion("1.10.0").build();
        when(fragmentRepository.get("myFragment"))
                .thenReturn(fragment2);
        when(fragmentService.getStatus(fragment2)).thenReturn(new ArtifactStatusReport(true, true));
        result = getStatusRequestById("fragment", "myFragment");
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_artifact_status_when_no_migration_required() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").withArtifactStatusReport(new ArtifactStatusReport(true, false)).build();

        // with json
        mockMvc = mockServer(migrationResource).build();
        ResultActions result = postStatusRequest(fragment);
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        mockMvc = mockServer(migrationResource).build();
        Fragment fragment2 = aFragment().withId("myFragment").withName("myFragment").withDesignerVersion("2.0").withArtifactStatusReport(new ArtifactStatusReport(true, false)).build();
        when(fragmentRepository.get("myFragment"))
                .thenReturn(fragment2);
        when(fragmentService.getStatus(fragment2)).thenReturn(new ArtifactStatusReport(true, false));
        result = getStatusRequestById("fragment", "myFragment");
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_artifact_status_when_not_compatible_required() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.1").isMigration(false).isCompatible(false).build();

        // with json
        mockMvc = mockServer(migrationResource).build();
        ResultActions result = postStatusRequest(fragment);
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());

        // by id
        mockMvc = mockServer(migrationResource).build();
        Fragment fragment2 = aFragment().withId("myFragment").withName("myFragment").withDesignerVersion("2.1").isMigration(false).isCompatible(false).build();
        when(fragmentRepository.get("myFragment"))
                .thenReturn(fragment2);
        when(fragmentService.getStatus(fragment2)).thenReturn(new ArtifactStatusReport(false, false));
        result = getStatusRequestById("fragment", "myFragment");
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_to_migrate() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        // Get dependencies status
        when(fragmentService.getStatus(fragment)).thenReturn(new ArtifactStatusReport(true, true));

        ResultActions result = getStatusRequestByIdRecursive("fragment", "myFragment");
        assertThat(getStatusReport(true, true)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_not_compatible() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        // Get dependencies status
        when(fragmentService.getStatus(fragment)).thenReturn(new ArtifactStatusReport(false, false));

        ResultActions result = getStatusRequestByIdRecursive("fragment", "myFragment");
        assertThat(getStatusReport(false, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_correct_migration_status_when_embedded_artifact_not_to_migrate() throws Exception {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        // Get dependencies status
        when(fragmentService.getStatus(fragment)).thenReturn(new ArtifactStatusReport(true, false));

        ResultActions result = getStatusRequestByIdRecursive("fragment", "myFragment");
        assertThat(getStatusReport(true, false)).isEqualTo(result.andReturn().getResponse().getContentAsString());
    }

    @Test
    void should_return_not_found_when_invalid_artifact_id() throws Exception {
        mockMvc = mockServer(migrationResource).build();
        when(fragmentRepository.get("invalidFragmentId"))
                .thenThrow(new NotFoundException());
        getStatusRequestByIdInvalid("fragment", "invalidFragmentId");
    }

    private ResultActions postStatusRequest(DesignerArtifact artifact) throws Exception {
        return mockMvc
                .perform(post("/rest/migration/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(artifact)))
                .andExpect(status().isOk());
    }

    private ResultActions getStatusRequestById(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s", artifactType, artifactId);
        return mockMvc
                .perform(get(url))
                .andExpect(status().isOk());
    }

    private String getStatusReport(boolean compatible, boolean migration) {
        return new ArtifactStatusReport(compatible, migration).toString();
    }

    private void getStatusRequestByIdInvalid(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s", artifactType, artifactId);
        mockMvc
                .perform(get(url))
                .andExpect(status().isNotFound());
    }

    private ResultActions getStatusRequestByIdRecursive(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s?recursive=true", artifactType, artifactId);
        return mockMvc
                .perform(get(url))
                .andExpect(status().isOk());
    }
}
