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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.convertObjectToJsonBytes;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class MigrationResourceTest {

    private MockMvc mockMvc;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private WidgetRepository widgetRepository;

    @InjectMocks
    private MigrationResource MigrationResource;

    @Before
    public void setUp() throws URISyntaxException {
        ReflectionTestUtils.setField(MigrationResource, "modelVersion", "2.0");
        mockMvc = mockServer(MigrationResource).build();
    }

    @Test
    public void should_return_artifact_status_when_migration_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.10.0").withPreviousDesignerVersion("1.9.0").build();
        Widget widget = WidgetBuilder.aWidget().id("myWidget").designerVersion("1.10.0").previousDesignerVersion("1.9.0").build();

        // with json
        ResultActions result = postStatusRequest(page);
        String resultType = result.andReturn().getResponse().getContentType();
        Assert.assertNotEquals(resultType, null);
        Assert.assertTrue(resultType.startsWith(MediaType.APPLICATION_JSON.toString()));
        Assert.assertEquals(getStatusReport(true, true), result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        Assert.assertEquals(getStatusReport(true, true), result.andReturn().getResponse().getContentAsString());

        // by id
        when(pageRepository.get("myPage"))
                .thenReturn(aPage().withId("myPage").withName("myPage").withDesignerVersion("1.10.0").build());
        result = getStatusRequestById("page", "myPage");
        Assert.assertEquals(getStatusReport(true, true), result.andReturn().getResponse().getContentAsString());

        when(widgetRepository.get("myWidget"))
                .thenReturn(aWidget().id("myWidget").name("myWidget").designerVersion("1.10.0").build());
        result = getStatusRequestById("widget", "myWidget");
        Assert.assertEquals(getStatusReport(true, true), result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void should_return_artifact_status_when_no_migration_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Widget widget = WidgetBuilder.aWidget().id("myWidget").modelVersion("2.0").build();

        // with json
        ResultActions result = postStatusRequest(page);
        Assert.assertEquals(getStatusReport(true, false), result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        Assert.assertEquals(getStatusReport(true, false), result.andReturn().getResponse().getContentAsString());

        // by id
        when(pageRepository.get("myPage"))
                .thenReturn(aPage().withId("myPage").withName("myPage").withModelVersion("2.0").build());
        result = getStatusRequestById("page", "myPage");
        Assert.assertEquals(getStatusReport(true, false), result.andReturn().getResponse().getContentAsString());

        when(widgetRepository.get("myWidget"))
                .thenReturn(aWidget().id("myWidget").name("myWidget").designerVersion("2.0").build());
        result = getStatusRequestById("widget", "myWidget");
        Assert.assertEquals(getStatusReport(true, false), result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void should_return_artifact_status_when_not_compatible_required() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.1").build();
        Widget widget = WidgetBuilder.aWidget().id("myWidget").modelVersion("2.1").build();

        // with json
        ResultActions result = postStatusRequest(page);
        Assert.assertEquals(getStatusReport(false, false), result.andReturn().getResponse().getContentAsString());

        result = postStatusRequest(widget);
        Assert.assertEquals(getStatusReport(false, false), result.andReturn().getResponse().getContentAsString());

        // by id
        when(pageRepository.get("myPage"))
                .thenReturn(aPage().withId("myPage").withName("myPage").withModelVersion("2.1").build());
        result = getStatusRequestById("page", "myPage");
        Assert.assertEquals(getStatusReport(false, false), result.andReturn().getResponse().getContentAsString());

        when(widgetRepository.get("myWidget"))
                .thenReturn(aWidget().id("myWidget").name("myWidget").designerVersion("2.1").build());
        result = getStatusRequestById("widget", "myWidget");
        Assert.assertEquals(getStatusReport(false, false), result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void should_return_bad_request_when_invalid_artifact_json() throws Exception {
        postStatusBadRequest();
    }

    @Test
    public void should_return_not_found_when_invalid_artifact_id() throws Exception {
        when(pageRepository.get("invalidPageId"))
                .thenThrow(new NotFoundException());
        getStatusRequestByIdInvalid("page", "invalidPageId");

        when(widgetRepository.get("invalidWidgetId"))
                .thenThrow(new NotFoundException());
        getStatusRequestByIdInvalid("widget", "invalidWidgetId");
    }

    private ResultActions postStatusRequest(DesignerArtifact artifact) throws Exception {
        return mockMvc
                .perform(post("/rest/migration/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(artifact)))
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

    private void getStatusRequestByIdInvalid(String artifactType, String artifactId) throws Exception {
        String url = String.format("/rest/migration/status/%s/%s", artifactType, artifactId);
         mockMvc
                .perform(get(url))
                .andExpect(status().isNotFound());
    }

    private String getStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }
}
