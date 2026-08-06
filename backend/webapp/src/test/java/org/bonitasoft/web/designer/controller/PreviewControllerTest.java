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

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.common.generator.rendering.GenerationException;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static junit.framework.Assert.assertEquals;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
public class PreviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArtifactBuilder artifactBuilder;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FragmentRepository fragmentRepository;

    private Path tmpWorkspacePath;

    private WorkspaceProperties workspaceProperties;

    private WorkspaceUidProperties workspaceUidProperties;

    @BeforeEach
    public void beforeEach() throws Exception {
        tmpWorkspacePath = Paths.get(getClass().getResource("/tmpWorkspace").toURI());

        workspaceProperties = new WorkspaceProperties();
        workspaceUidProperties = new WorkspaceUidProperties();
        workspaceUidProperties.setPath(tmpWorkspacePath);
        workspaceProperties.getPages().setDir(Paths.get(getClass().getResource("/workspace/pages").toURI()));
        workspaceProperties.getWidgets().setDir(Paths.get(getClass().getResource("/workspace/widgets").toURI()));
        workspaceProperties.getFragments().setDir(Paths.get(getClass().getResource("/workspace/fragments").toURI()));

        mockMvc = standaloneSetup(new PreviewController(pageRepository, fragmentRepository, artifactBuilder, workspaceProperties, workspaceUidProperties)).build();
    }

    @Test
    public void should_call_the_previewer() throws Exception {
        final Page page = aPage().withId("my-page").withName("my-page").build();
        when(pageRepository.get("my-page")).thenReturn(page);
        when(artifactBuilder.buildHtml(eq(page), anyString())).thenReturn("Everything ok");

        mockMvc
                .perform(get("/preview/page/no-app-selected/my-page"))
                .andExpect(status().isOk())
                .andExpect(content().string("Everything ok"))
                .andExpect(content().encoding("UTF-8"));

    }

    @Test
    public void should_return_error_response_when_error_occur_on_generation() throws Exception {
        final Page page = aPage().withId("my-page").withName("my-page").build();
        when(pageRepository.get("my-page")).thenReturn(page);
        when(artifactBuilder.buildHtml(eq(page), eq("/runtime/"))).thenThrow(new GenerationException("error", new Exception()));

        mockMvc
                .perform(get("/preview/page/no-app-selected/my-page"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error during page generation"))
        ;
    }


    @Test
    public void should_return_error_response_when_page_is_not_found() throws Exception {

        when(pageRepository.get("unexisting-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc
                .perform(get("/preview/page/no-app-selected/unexisting-page"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Page <unexisting-page> not found"))
        ;
    }

    @Test
    public void should_load_page_asset_on_disk() throws Exception {
        Path expectedFile = workspaceProperties.getPages().getDir().resolve("ma-page/assets/js/timeshift.js");

        mockMvc
                .perform(get("/preview/page/no-app-selected/ma-page/assets/js/timeshift.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"timeshift.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_page_asset_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/page/no-app-selected/ma-page/assets/js/unkknown.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_404_when_widget_asset_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/widget/no-app-selected/widget-id/assets/widget-id/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_load_widget_asset_included_in_page_on_disk() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/assets/css/my-css-1.0.0.css");

        mockMvc
                .perform(get("/preview/page/no-app-selected/page-id/widgets/pbLabel/assets/css/my-css-1.0.0.css"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my-css-1.0.0.css\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_asset_included_in_any_previewable_on_disk() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/assets/css/my-css-1.0.0.css");

        mockMvc
                .perform(get("/preview/aPreviewable/no-app-selected/previewable-id/widgets/pbLabel/assets/css/my-css-1.0.0.css"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my-css-1.0.0.css\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_directive() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.js");

        mockMvc.perform(get("/preview/page/no-app-selected/page-id/widgets/pbLabel/pbLabel.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_directive_for_any_previewable() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.js");

        mockMvc.perform(get("/preview/aPreviewable/no-app-selected/previewable-id/widgets/pbLabel/pbLabel.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/page/no-app-selected/page-id/widgets/widget-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_return_a_fake_css_file_for_living_application_theme() throws Exception {
        String expectedContent = "/**" + System.lineSeparator()
                + "* Living application theme" + System.lineSeparator()
                + "*/";

        mockMvc
                .perform(get("/preview/page/no-app-selected/theme/theme.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedContent))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"theme.css\""))
                .andExpect(content().encoding("UTF-8"));

    }

    @Test
    public void should_redirect_page_theme_calls_to_the_real_theme_resource() throws Exception {
        String expectedContent = "/**" + System.lineSeparator()
                + "* Living application theme" + System.lineSeparator()
                + "*/";

        mockMvc
                .perform(get("/preview/page/myApp/theme/images/logo.png"))
                .andExpect(redirectedUrl("/apps/myApp/theme/images/logo.png"));

    }

    @Test
    public void should_redirect_page_API_calls_to_the_real_API() throws Exception {
        mockMvc
                .perform(get("/preview/page/no-app-selected/API/portal/page"))
                .andExpect(redirectedUrl("/API/portal/page"));
    }

    @Test
    public void should_redirect_layout_API_calls_to_the_real_API() throws Exception {
        mockMvc
                .perform(get("/preview/layout/no-app-selected/API/portal/page"))
                .andExpect(redirectedUrl("/API/portal/page"));
    }

    @Test
    public void should_redirect_API_calls_to_the_real_API_and_add_the_query_string() throws Exception {
        mockMvc
                .perform(get("/preview/page/no-app-selected/API/portal/page?p=0&c=1"))
                .andExpect(redirectedUrl("/API/portal/page?p=0&c=1"));
    }

    @Test
    public void should_temporarily_redirect_API_post_to_the_real_API() throws Exception {
        mockMvc
                .perform(post("/preview/page/no-app-selected/API/portal/page?id=123"))
                .andExpect(result -> {
                    assertEquals(result.getResponse().getStatus(), SC_TEMPORARY_REDIRECT);
                    assertEquals(result.getResponse().getRedirectedUrl(), "/API/portal/page?id=123");
                });
    }

    @Test
    public void should_load_widget_minify_files_for_any_previawable() throws Exception {
        Path expectedFile = workspaceUidProperties.getTmpPagesRepositoryPath().resolve("ma-page/js/widgets-abc123.min.js");

        mockMvc
                .perform(get("/preview/page/no-app-selected/ma-page/js/widgets-abc123.min.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"widgets-abc123.min.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_call_the_previewer_for_fragments() throws Exception {
        final Fragment fragment = aFragment().withId("my-fragment").withName("my-fragment").build();
        when(fragmentRepository.get("my-fragment")).thenReturn(fragment);
        when(artifactBuilder.buildHtml(eq(fragment), anyString())).thenReturn("Everything ok");

        mockMvc
                .perform(get("/preview/fragment/no-app-selected/my-fragment"))
                .andExpect(status().isOk())
                .andExpect(content().string("Everything ok"))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_fragment_directive() throws Exception {
        Path expectedFile = workspaceProperties.getFragments().getDir().resolve("person/person.js");

        mockMvc.perform(get("/preview/page/no-app-selected/a-page/fragments/person/person.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"person.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_not_found_when_fragment_directive_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/page/no-app-selected/a-page/fragments/unknown/unkwnon.js"))
                .andExpect(status().isNotFound());
    }

}
