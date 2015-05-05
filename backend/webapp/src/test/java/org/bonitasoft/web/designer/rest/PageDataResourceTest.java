/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Map;

import org.bonitasoft.web.designer.builder.DataBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.RestControllerUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test de {@link PageResource} => only the part links to data
 */
public class PageDataResourceTest {

    private MockMvc mockMvc;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PageResource pageResource;

    @Before
    public void setUp() {
        initMocks(this);
        mockMvc = standaloneSetup(pageResource)
                .setHandlerExceptionResolvers(RestControllerUtil.createContextForTest().handlerExceptionResolver())
                .build();
    }

    @Test
    public void should_get_page_data() throws Exception {
        Data data1 = DataBuilder.aConstantData().value("aValue").build();
        Data data2 = DataBuilder.aConstantData().value("anotherValue").build();
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withData("data1", data1).withData("data2", data2).build());

        mockMvc.perform(get("/rest/pages/my-page/data"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data1.type").value("constant"));

    }

    @Test
    public void should_respond_404_notFound_when_trying_to_get_data_on_unexisting_page() throws Exception {
        when(pageRepository.get("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc.perform(get("/rest/pages/my-page/data")).andExpect(status().isNotFound());
    }

    @Test
    public void should_create_a_new_data_for_a_page() throws Exception {
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withId("my-page").build());
        Data data = DataBuilder.aConstantData().value("aValue").build();

        mockMvc.perform(put("/rest/pages/my-page/data/new-data")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(RestControllerUtil.convertObjectToJsonBytes(data)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(pageRepository).save(PageBuilder.aPage().withId("my-page").withData("new-data", data).build());
    }

    @Test
    public void should_update_a_data_for_a_page() throws Exception {
        Data oldData = DataBuilder.aConstantData().value("anOldValue").build();
        Data expectedData = DataBuilder.aConstantData().value("newValue").build();
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withId("my-page").withData("updated-data", oldData).build());

        mockMvc.perform(put("/rest/pages/my-page/data/updated-data")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(RestControllerUtil.convertObjectToJsonBytes(expectedData)))
                .andExpect(status().isOk());

        verify(pageRepository).save(PageBuilder.aPage().withId("my-page").withData("updated-data", expectedData).build());
    }

    @Test
    public void should_return_page_data_while_saving_a_data_for_a_page() throws Exception {
        Data aData = DataBuilder.aConstantData().value("aValue").build();
        Data anotherData = DataBuilder.aConstantData().value("anotherValue").build();
        Data newData = DataBuilder.aConstantData().value("newValue").build();
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withData("aData", aData).withData("anotherData", anotherData).build());

        MvcResult result = mockMvc.perform(put("/rest/pages/my-page/data/new-data")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(RestControllerUtil.convertObjectToJsonBytes(newData)))
                .andReturn();

        assertThat(RestControllerUtil.convertJsonByteToObject(result.getResponse().getContentAsByteArray(), Map.class))
                .hasSize(3)
                .containsKeys("aData", "anotherData", "new-data");
    }

    @Test
    public void should_respond_404_notFoundException_when_trying_to_save_data_on_an_unknown_page() throws Exception {
        when(pageRepository.get("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc.perform(put("/rest/pages/my-page/data/new-data")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(RestControllerUtil.convertObjectToJsonBytes(DataBuilder.aConstantData().value("newValue").build())))
                .andExpect(status().isNotFound());

    }

    @Test
    public void should_respond_500_internal_server_error_when_error_occurs_while_saving_a_data_on_a_page() throws Exception {
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withId("my-page").build());
        Mockito.doThrow(new RepositoryException("page not found", new Exception())).when(pageRepository).save(any(Page.class));

        mockMvc.perform(put("/rest/pages/my-page/data/new-data")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(RestControllerUtil.convertObjectToJsonBytes(DataBuilder.aConstantData().value("newValue").build())))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_delete_a_data_of_a_page() throws Exception {
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withId("my-page").withData("aData", DataBuilder.aConstantData().value("aValue")).build());

        mockMvc.perform(delete("/rest/pages/my-page/data/aData")).andExpect(status().isOk());

        ArgumentCaptor<Page> argument = ArgumentCaptor.forClass(Page.class);
        verify(pageRepository).save(argument.capture());
        assertThat(argument.getValue().getData()).doesNotContainKey("aData");
    }

    @Test
    public void should_return_page_data_while_deleting_a_data_of_a_page() throws Exception {
        Data aData = DataBuilder.aConstantData().value("aValue").build();
        Data toBeDeletedData = DataBuilder.aConstantData().value("toBeDeletedValue").build();
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withData("aData", aData).withData("anotherData", toBeDeletedData).build());

        MvcResult result = mockMvc.perform(delete("/rest/pages/my-page/data/anotherData"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertThat(RestControllerUtil.convertJsonByteToObject(result.getResponse().getContentAsByteArray(), Map.class))
                .hasSize(1)
                .containsKeys("aData");
    }

    @Test
    public void should_respond_404_NotFound_when_trying_to_delete_an_unknown_page_data() throws Exception {
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().build());

        mockMvc.perform(delete("/rest/pages/my-page/data/aData")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_internalError_if_error_occurs_while_deleting_data() throws Exception {
        when(pageRepository.get("my-page")).thenReturn(PageBuilder.aPage().withId("my-page").withData("aData", DataBuilder.aConstantData().value("aValue")).build());
        doThrow(new RepositoryException("page not found", new Exception())).when(pageRepository).save(any(Page.class));

        mockMvc.perform(delete("/rest/pages/my-page/data/aData")).andExpect(status().isInternalServerError());
    }
}
