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

import java.io.IOException;

import org.bonitasoft.web.designer.ArtifactBuilder;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.format;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@RunWith(MockitoJUnitRunner.class)
public class ExportControllerTest {

    @Rule
    public TemporaryFolder repositoryFolder = new TemporaryFolder();

    private MockMvc mockMvc;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageService pageService;
    @Mock
    private FragmentService fragmentService ;
    @Mock
    private WidgetService widgetService ;

    @Mock
    private ArtifactBuilder artifactBuilder;

    private ExportController exportController;

    @Before
    public void setUp() {
        exportController = spy(new ExportController(
                pageService,
                fragmentService,
                widgetService,
                artifactBuilder
        ));
        mockMvc = mockServer(exportController).build();
    }

    @Test
    public void should_export_a_page() throws Exception {
        mockMvc.perform(get("/export/page/a-page"));

        verify(pageService).get("a-page");
    }

    @Test
    public void should_export_a_widget() throws Exception {

        mockMvc.perform(get("/export/widget/a-widget"));

        verify(widgetService).get("a-widget");
    }

    @Test
    public void should_export_a_fragment() throws Exception {

        mockMvc.perform(get("/export/fragment/a-fragment"));

        verify(fragmentService).get("a-fragment");
    }

    private Page create(Page page) throws IOException {
        if (page.getId() == null) {
            page.setId("default-id");
        }
        lenient().when(pageService.get(page.getId())).thenReturn(page);
        write(repositoryFolder.toPath().resolve(format("%s.json", page.getId())), "foobar".getBytes());
        return page;
    }

    @Test
    public void should_set_artifact_type_in_filename() throws Exception {
        final Page myLayout = create(aPage().withId("myLayout").withType("layout").withName("thelayout").build());

        mockMvc.perform(get("/export/page/myLayout"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"layout-thelayout.zip\""));
    }

    @Test
    public void should_set_formatted_filename_in_header() throws Exception {
        final Page myPage = create(aPage().withId("myPage").withName("é&az zer/è\"").build());
        final Page myLayout = create(aPage().withId("myLayout").withType("layout").withName("thelayout").build());

        String zipFileNamePage = exportController.getZipFileName(myPage);
        String zipFileNameLayout = exportController.getZipFileName(myLayout);

        assertThat(zipFileNamePage).isEqualTo("page-az-zer.zip");
        assertThat(zipFileNameLayout).isEqualTo("layout-thelayout.zip");
    }

    @Test
    public void should_set_zip_content_type_to_response() throws Exception {
        create(aPage().withId("myPage").build());
        mockMvc.perform(get("/export/page/myPage"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
// TODO: check with content-type should be used .andExpect(content().contentType("application/zip"));
    }

}
