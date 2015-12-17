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

import static java.nio.file.Files.readAllBytes;
import static javax.servlet.http.HttpServletResponse.SC_TEMPORARY_REDIRECT;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

@RunWith(MockitoJUnitRunner.class)
public class PreviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Previewer previewer;

    @Mock
    private PageRepository pageRepository;

    private Path widgetRepositoryPath;
    private Path pageRepositoryPath;

    @Before
    public void beforeEach() throws Exception {
        widgetRepositoryPath = Paths.get(getClass().getResource("/workspace/widgets").toURI());
        pageRepositoryPath = Paths.get(getClass().getResource("/workspace/pages").toURI());
        mockMvc = standaloneSetup(new PreviewController(pageRepository, previewer, widgetRepositoryPath, pageRepositoryPath)).build();
    }

    @Test
    public void should_call_the_previewer() throws Exception {
        ResponseEntity<String> response = new ResponseEntity<>("Everything ok", HttpStatus.OK);
        when(previewer.render(eq("my-page"), eq(pageRepository), any(HttpServletRequest.class))).thenReturn(response);

        mockMvc
                .perform(get("/preview/page/my-page"))
                .andExpect(status().isOk())
                .andExpect(content().string("Everything ok"))
                .andExpect(content().encoding("UTF-8"));

    }

    @Test
    public void should_load_page_asset_on_disk() throws Exception {
        Path expectedFile = pageRepositoryPath.resolve("ma-page/assets/js/timeshift.js");

        mockMvc
                .perform(get("/preview/page/ma-page/assets/js/timeshift.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"timeshift.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_page_asset_is_not_found() throws Exception {
        mockMvc.perform(get("preview/page/ma-page/assets/js/unkknown.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_404_when_widget_asset_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/widget/widget-id/assets/widget-id/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_load_widget_asset_included_in_page_on_disk() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/assets/css/my-css-1.0.0.css");

        mockMvc
                .perform(get("/preview/page/page-id/widgets/pbLabel/assets/css/my-css-1.0.0.css"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my-css-1.0.0.css\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_asset_included_in_any_previewable_on_disk() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/assets/css/my-css-1.0.0.css");

        mockMvc
                .perform(get("/preview/aPreviewable/previewable-id/widgets/pbLabel/assets/css/my-css-1.0.0.css"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"my-css-1.0.0.css\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_directive() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");

        mockMvc.perform(get("/preview/page/page-id/widgets/pbLabel/pbLabel.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_load_widget_directive_for_any_previewable() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");

        mockMvc.perform(get("/preview/aPreviewable/previewable-id/widgets/pbLabel/pbLabel.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        mockMvc.perform(get("/preview/page/page-id/widgets/widget-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_return_a_fake_css_file_for_living_application_theme() throws Exception {
        String expectedContent = "/**" + System.lineSeparator()
                + "* Living application theme" + System.lineSeparator()
                + "*/";

        mockMvc
                .perform(get("/preview/page/theme/theme.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedContent))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"theme.css\""))
                .andExpect(content().encoding("UTF-8"));

    }

    @Test
    public void should_redirect_API_calls_to_the_real_API() throws Exception {
        mockMvc
                .perform(get("/preview/page/API/portal/page"))
                .andExpect(redirectedUrl("/bonita/API/portal/page"));
    }

    @Test
    public void should_redirect_API_calls_to_the_real_API_and_add_the_query_string() throws Exception {
        mockMvc
                .perform(get("/preview/page/API/portal/page?p=0&c=1"))
                .andExpect(redirectedUrl("/bonita/API/portal/page?p=0&c=1"));
    }

    @Test
    public void should_temporarily_redirect_ADI_post_to_the_real_API() throws Exception {
        mockMvc
                .perform(post("/preview/page/API/portal/page?id=123"))
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult result) throws Exception {
                        assertEquals(result.getResponse().getStatus(), SC_TEMPORARY_REDIRECT);
                        assertEquals(result.getResponse().getRedirectedUrl(), "/bonita/API/portal/page?id=123");
                    }
                });
    }
}
