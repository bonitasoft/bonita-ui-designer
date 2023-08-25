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

import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.generator.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.generator.mapping.FormScope;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.service.DefaultPageService;
import org.bonitasoft.web.designer.service.exception.IncompatibleException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.*;
import static org.bonitasoft.web.designer.builder.AssetBuilder.aFilledAsset;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PageWithFragmentBuilder.aPageWithFragmentElement;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.controller.utils.HttpFile.getOriginalFilename;
import static org.bonitasoft.web.designer.model.asset.AssetScope.PAGE;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContract;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de {@link org.bonitasoft.web.designer.controller.PageResource}
 */
@ExtendWith(MockitoExtension.class)
class PageResourceTest {


    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private MockMvc mockMvc;

    @Mock
    private DefaultPageService pageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ContractToPageMapper contractToPageMapper;

    private PageResource pageResource;

    private Path widgetRepositoryPath;

    private Component startProcessComponent;

    private Component submitTaskComponent;

    private Page page;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        pageResource = spy(new PageResource(jsonHandler, pageService, contractToPageMapper, messagingTemplate));
        mockMvc = mockServer(pageResource).build();
        widgetRepositoryPath = Paths.get(getClass().getResource("/workspace/widgets").toURI());
    }

    private Page mockPageOfId(String id) {
        Page page = aPage().withId(id).build();
        lenient().when(pageService.get(id)).thenReturn(page);
        lenient().when(pageService.getWithAsset(id)).thenReturn(page);
        return page;
    }


    //1422835200.000
    //1422835200.000 000 000

    @Test
    void should_list_pages() throws Exception {
        Page page = new Page();
        page.setId("id");
        page.setName("name");
        var lastUpdate = Instant.parse("2015-02-02T00:00:00.000Z");
        page.setLastUpdate(lastUpdate);
        when(pageService.getAll()).thenReturn(Arrays.asList(page));

        mockMvc.perform(get("/rest/pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id").value("id"))
                .andExpect(jsonPath("$[*].name").value("name"))
                .andExpect(jsonPath("$[*].lastUpdate").value(lastUpdate.toEpochMilli()));
    }

    @Test
    void should_create_a_page() throws Exception {
        Page pageToBeSaved = aPage().withId("my-page").withName("test").build();
        pageToBeSaved.setRows(singletonList(emptyList()));

        when(pageService.create(any())).thenReturn(pageToBeSaved);

        mockMvc
                .perform(post("/rest/pages")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void should_duplicate_a_page_from_a_Page() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();
        Page pageToBeSaved = aPage().withId("my-page").withName("test").withAsset(pageAsset, widgetAsset).build();
        when(pageService.createFrom(anyString(), any())).thenReturn(pageToBeSaved);

        mockMvc
                .perform(post("/rest/pages?duplicata=my-page-source")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageService).createFrom(eq("my-page-source"), eq(pageToBeSaved));
    }

    @Test
    void should_create_a_page_from_a_Contract_at_task_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(), eq(FormScope.TASK))).thenReturn(newPage);
        when(pageService.create(any())).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/task/myPage")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageService).create(newPage);
    }


    @Test
    void should_create_a_page_from_a_Contract_at_process_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(), eq(FormScope.PROCESS))).thenReturn(newPage);
        when(pageService.create(any())).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/process/myPage")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageService).create(newPage);
    }

    @Test
    void should_create_a_page_from_a_Contract_at_overview_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(), eq(FormScope.OVERVIEW))).thenReturn(newPage);
        when(pageService.create(any())).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/overview/myPage")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageService).create(newPage);
    }


    @Test
    void should_save_a_page() throws Exception {
        Page pageToBeSaved = mockPageOfId("my-page");
        when(pageService.save(pageToBeSaved.getId(), pageToBeSaved)).thenReturn(pageToBeSaved);

        mockMvc
                .perform(put("/rest/pages/my-page")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE));

        verify(messagingTemplate).convertAndSend("/previewableUpdates", "my-page");
    }

    @Test
    void should_save_a_page_with_fragment() throws Exception {
        Page pageToBeSaved = aPageWithFragmentElement();
        pageToBeSaved.setId(pageToBeSaved.getName());
        when(pageService.save(pageToBeSaved.getId(), pageToBeSaved)).thenReturn(pageToBeSaved);

        mockMvc
                .perform(put("/rest/pages/" + pageToBeSaved.getName()).
                        contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isOk());

        verify(pageService).save(pageToBeSaved.getId(), pageToBeSaved);
        verify(messagingTemplate).convertAndSend("/previewableUpdates", pageToBeSaved.getName());
    }


    @Test
    void should_save_a_page_renaming_it() throws Exception {
        final String pageId = "my-page";

        Page pageToBeUpdated = mockPageOfId(pageId);
        pageToBeUpdated.addAsset(aFilledAsset(pageToBeUpdated));

        when(pageService.save(eq(pageId), any())).thenAnswer(invocation -> {
            final Page page = mockPageOfId("page-new-name");
            page.addAsset(aFilledAsset(page));
            page.setName("page-new-name");
            return page;
        });

        Page pageToBeSaved = aPage().withName("page-new-name").build();
        pageToBeSaved.addAsset(aFilledAsset(pageToBeUpdated));
        mockMvc.perform(
                put("/rest/pages/my-page")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/pages/page-new-name"));

        verify(messagingTemplate).convertAndSend("/previewableRemoval", pageId);

    }


    private Asset aPageAsset() {
        return anAsset().withName("myJs.js").withType(JAVASCRIPT).build();
    }

    private Asset aWidgetAsset() {
        return anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build();
    }

    @Test
    void should_respond_415_unsupported_media_type_when_trying_to_save_non_json_content() throws Exception {

        mockMvc
                .perform(put("/rest/pages/my-page").content("this is not json"))
                .andExpect(status().is(415));
    }

    @Test
    void should_respond_500_internal_error_if_error_occurs_while_saving_a_page() throws Exception {
        Page page = aPage().withId("my-page").build();
        doThrow(new RepositoryException("exception occurs", new Exception())).when(pageService).save(page.getId(), page);

        mockMvc
                .perform(
                        put("/rest/pages/my-page").contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                jsonHandler.toJson(page)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_retrieve_a_page_representation_by_its_id() throws Exception {
        final String pageId = "my-page";
        Page expectedPage = aFilledPage(pageId);
        expectedPage.setStatus(new ArtifactStatusReport(true, true));
        when(pageService.getWithAsset(pageId)).thenReturn(expectedPage);

        mockMvc
                .perform(get("/rest/pages/" + pageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void should_respond_404_not_found_if_page_is_not_existing() throws Exception {
        when(pageService.getWithAsset("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc.perform(get("/rest/pages/my-page")).andExpect(status().isNotFound());
    }


    @Test
    void should_respond_422_on_load_when_page_is_incompatible() throws Exception {
        Page expectedPage = aFilledPage("my-page");
        expectedPage.setStatus(new ArtifactStatusReport(false, true));
        when(pageService.getWithAsset("my-page")).thenReturn(expectedPage);

        mockMvc.perform(get("/rest/pages/my-page")).andExpect(status().is(422));
    }

    @Test
    void should_respond_422_on_save_when_page_is_incompatible() throws Exception {
        Page pageToBeSaved = mockPageOfId("my-page");
        when(pageService.save(pageToBeSaved.getId(), pageToBeSaved)).thenThrow(IncompatibleException.class);

        ResultActions result = mockMvc
                .perform(
                        put("/rest/pages/my-page")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonHandler.toJson(pageToBeSaved)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(TEXT_PLAIN));
    }

    @Test
    void should_respond_404_not_found_when_delete_inexisting_page() throws Exception {
        doThrow(new NotFoundException("page not found")).when(pageService).delete("my-page");

        mockMvc.perform(delete("/rest/pages/my-page")).andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_internal_error_when_error_on_deletion_page() throws Exception {
        doThrow(new RepositoryException("error occurs", new RuntimeException())).when(pageService).delete("my-page");

        mockMvc.perform(delete("/rest/pages/my-page")).andExpect(status().isInternalServerError());
    }

    @Test
    void should_delete_a_page() throws Exception {
        mockMvc
                .perform(delete("/rest/pages/my-page"))
                .andExpect(status().isOk());
    }

    @Test
    void should_rename_a_page() throws Exception {
        String newName = "my-page-new-name";
        final String pageId = "my-page";
        Page pageToBeUpdated = aPage().withId(pageId).withName("page-name").build();
        when(pageService.get(pageId)).thenReturn(pageToBeUpdated);
        when(pageService.rename(pageId, newName)).thenAnswer(invocation -> {
            pageToBeUpdated.setId(newName);
            pageToBeUpdated.setName(newName);
            return pageToBeUpdated;
        });

        mockMvc
                .perform(
                        put("/rest/pages/" + pageId + "/name")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(newName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/pages/" + newName));

        verify(pageService).rename(eq(pageId), eq(newName));

    }


    @Test
    void should_not_rename_a_page_if_name_is_same() throws Exception {
        String name = "page-name";
        Page pageToBeUpdated = aPage().withId("my-page").withName(name).build();
        when(pageService.get("my-page")).thenReturn(pageToBeUpdated);

        mockMvc
                .perform(
                        put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(name))
                .andExpect(status().isOk());

        verify(pageService, never()).rename(anyString(), any());
    }

    @Test
    void should_respond_404_not_found_if_page_is_not_existing_when_renaming() throws Exception {
        when(pageService.get("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc
                .perform(put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonHandler.toJson("hello")))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_internal_error_if_error_occurs_while_renaming_a_page() throws Exception {
        mockPageOfId("my-page");
        doThrow(new RepositoryException("exception occurs", new Exception())).when(pageService).rename(anyString(), any());

        mockMvc
                .perform(put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonHandler.toJson("hello")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_upload_a_local_asset() throws Exception {
        //We construct a mock file (the first arg is the name of the property expected in the controller)
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        final String pageId = "my-page";
        mockPageOfId(pageId);
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(1).withScope(PAGE)
                .withType(JAVASCRIPT).build();

        when(pageService.saveOrUpdateAsset(pageId, JAVASCRIPT, getOriginalFilename(file.getOriginalFilename()), file.getBytes())).thenReturn(expectedAsset);

        mockMvc.perform(multipart("/rest/pages/my-page/assets/js").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.scope").value(PAGE))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(1));
    }

    @Test
    void should_respond_202_with_error_when_uploading_a_json_asset_with_malformed_json_file() throws Exception {
        byte[] content = "notvalidjson".getBytes();
        MockMultipartFile file = aJsonFileWithContent(content);
        int expectedLine = 1, expectedColumn = 13;

        mockMvc.perform(multipart("/rest/pages/my-page/assets/json").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("MalformedJsonException"))
                .andExpect(jsonPath("$.infos.location.line").value(expectedLine))
                .andExpect(jsonPath("$.infos.location.column").value(expectedColumn));
    }

    private MockMultipartFile aJsonFileWithContent(byte[] content) {
        return new MockMultipartFile("file", "myfile.js", "application/json", content);
    }

    @Test
    void should_not_upload_an_asset_when_upload_send_an_error() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        doThrow(IllegalArgumentException.class).when(pageResource).saveOrUpdate(any(), anyString(), anyString());

        mockMvc.perform(multipart("/rest/pages/my-page/assets/js").file(file))
                .andExpect(status().isBadRequest());

    }

    @Test
    void should_save_an_external_asset() throws Exception {
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withScope(PAGE).withType(JAVASCRIPT).build();
        when(pageService.saveAsset(anyString(), any())).thenReturn(expectedAsset);

        mockMvc.perform(
                post("/rest/pages/my-page/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(expectedAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.scope").value(PAGE))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

    }

    @Test
    void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().build();
        doThrow(IllegalArgumentException.class).when(pageService).saveAsset(page.getId(), asset);

        mockMvc.perform(
                post("/rest/pages/my-page/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_list_page_assets() throws Exception {
        Page page = mockPageOfId("my-page");
        when(pageService.listAsset(page)).thenReturn(Set.of(
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(JAVASCRIPT).withScope(PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withScope(PAGE).withType(JAVASCRIPT).build()
        ));

        mockMvc.perform(get("/rest/pages/my-page/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", Matchers.containsInAnyOrder("https://mycdn.com/myExternalJs.js", "myJs.js", "myCss.css")))
                .andExpect(jsonPath("$[*].type", Matchers.containsInAnyOrder("js", "js", "css")))
                .andExpect(jsonPath("$[*].scope", Matchers.containsInAnyOrder("page", "page", "widget")))
                .andExpect(jsonPath("$[*].componentId", Matchers.containsInAnyOrder("widget-id")));

    }

    @Test
    void should_list_page_assets_while_getting_a_page() throws Exception {
        Page page = mockPageOfId("my-page");
        page.setAssets(Set.of(
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(JAVASCRIPT).withScope(PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withType(JAVASCRIPT).withScope(PAGE).build()
        ));
        when(pageService.getWithAsset(page.getId())).thenReturn(page);

        mockMvc.perform(get("/rest/pages/my-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets", hasSize(3)))
                .andExpect(jsonPath("$.assets[*].name", Matchers.containsInAnyOrder("https://mycdn.com/myExternalJs.js", "myJs.js", "myCss.css")))
                .andExpect(jsonPath("$.assets[*].type", Matchers.containsInAnyOrder("js", "js", "css")))
                .andExpect(jsonPath("$.assets[*].scope", Matchers.containsInAnyOrder("page", "page", "widget")))
                .andExpect(jsonPath("$.assets[*].componentId", Matchers.containsInAnyOrder("widget-id")));
    }

    @Test
    void should_increment_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId("my-page").withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?increment=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isOk());

        verify(pageService).changeAssetOrder(page.getId(), "UIID", INCREMENT);
    }

    @Test
    void should_decrement_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId("my-page").withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?decrement=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isOk());

        verify(pageService).changeAssetOrder(page.getId(), "UIID", DECREMENT);
    }

    @Test
    void should_delete_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");

        mockMvc.perform(
                delete("/rest/pages/my-page/assets/UIID")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(pageService).deleteAsset(page.getId(), "UIID");
    }

    @Test
    void should_inactive_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId(page.getId()).withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?active=false")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isOk());

        verify(pageService).changeAssetStateInPreviewable(page.getId(), "UIID", false);
    }

    @Test
    void should_mark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/pages/my-page/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(pageService).markAsFavorite("my-page", true);
    }

    @Test
    void should_unmark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/pages/my-page/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(pageService).markAsFavorite("my-page", false);
    }

    @Test
    void should_load_page_asset_on_disk_with_content_type_text() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(pageService.findAssetPath("page-id", "asset.js", JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/pages/page-id/assets/js/asset.js?format=text"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    void should_download_page_asset() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(pageService.findAssetPath("page-id", "asset.js", JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/pages/page-id/assets/js/asset.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        when(pageService.findAssetPath("page-id", "asset.js", JAVASCRIPT.getPrefix())).thenReturn(null);

        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js?format=text")).andExpect(status().isNotFound());
        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_widget_asset_included_in_page_produce_IOException() throws Exception {
        when(pageService.findAssetPath("page-id", "asset.js", JAVASCRIPT.getPrefix())).thenThrow(new IOException("can't read file"));

        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js?format=text")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js")).andExpect(status().isInternalServerError());
    }
    @Test
    void should_add_bonita_resources_found_in_pages_widgets() throws Exception {
        final String pageId = "myPage";
        Page page = mockPageOfId(pageId);
        when(pageService.getResources(page)).thenReturn(List.of(
                "GET|living/application-menu",
                "POST|bpm/process",
                "GET|bpm/userTask"
        ));

        mockMvc.perform(get("/rest/pages/{pageId}/resources", pageId))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"GET|living/application-menu\",\"POST|bpm/process\",\"GET|bpm/userTask\"]"))
        ;

    }

    @Test
    void should_show_the_correct_information_for_variables() throws Exception {
        Path expectedFilePath = Paths.get(getClass().getResource("/page-with-variables/").toURI()).resolve("page-with-variables.json");
        String expectedFileString = String.join("", readAllLines(expectedFilePath));

        Map<String, Variable> variables = new HashMap<>();
        variables.put("constantVar", new Variable(DataType.CONSTANT, "constantVariableValue"));
        variables.put("jsonVar", new Variable(DataType.JSON, "{\"var1\":1, \"var2\":2, \"var3\":\"value3\"}"));
        variables.put("jsVar", new Variable(DataType.EXPRESSION, "var variable = \"hello\"; return variable;"));
        Page page = new Page();
        page.setId("page-with-variables");
        page.setVariables(variables);
        page.setDesignerVersion("1.10.6");
        page.setName("page");
        page.setLastUpdate(Instant.ofEpochMilli(1514989634397L));
        page.setRows(new ArrayList<>());
        page.setWebResources(EMPTY_SET);
        page.setStatus(new ArtifactStatusReport());

        when(pageService.getWithAsset("id")).thenReturn(page);

        mockMvc.perform(get("/rest/pages/id"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedFileString));
    }

    @Test
    void should_return_error_when_uploading_file_empty() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        mockMvc.perform(multipart("/rest/pages/my-page/assets/js").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Part named [file] is needed to successfully import a component"))
        ;
    }

    @Test
    void should_return_error_when_uploadind_type_invalid() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        mockMvc.perform(multipart("/rest/pages/my-page/assets/INVALID").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("IllegalArgumentException"))
                .andExpect(jsonPath("$.message").value("Part named [file] is needed to successfully import a component"));
    }

    @Test
    void should_upload_newfile_and_save_new_asset() throws Exception {
        final String pageId = "my-page";
        Page page = mockPageOfId(pageId);

        byte[] fileContent = "function(){}".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "originalFileName.js", "application/javascript", fileContent);

        final String assetId = UUID.randomUUID().toString();
        when(pageService.saveOrUpdateAsset(page.getId(), JAVASCRIPT, file.getOriginalFilename(), fileContent))
                .thenReturn(
                        anAsset()
                                .withId(assetId)
                                .withName("originalFileName.js")
                                .withType(JAVASCRIPT)
                                .withOrder(1).build()
                );

        mockMvc.perform(multipart("/rest/pages/{pageId}/assets/js", pageId).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(assetId))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.name").value("originalFileName.js"))
                .andExpect(jsonPath("$.order").value(1))
                .andExpect(jsonPath("$.active").value(true))
        ;
        verify(pageService).saveOrUpdateAsset(page.getId(), JAVASCRIPT, "originalFileName.js", fileContent);
    }

    @Test
    void should_upload_a_json_asset() throws Exception {
        final String pageId = "page-id";
        Page page = mockPageOfId(pageId);

        byte[] fileContent = "{ \"some\": \"json\" }".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "asset.json", "application/javascript", fileContent);

        final String assetId = UUID.randomUUID().toString();
        when(pageService.saveOrUpdateAsset(page.getId(), AssetType.JSON, "asset.json", fileContent))
                .thenReturn(anAsset()
                        .withId(assetId)
                        .withName("asset.json")
                        .withType(AssetType.JSON)
                        .withOrder(1).build()
                );

        mockMvc.perform(multipart("/rest/pages/{pageId}/assets/json", pageId).file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(assetId))
                .andExpect(jsonPath("$.type").value("json"))
                .andExpect(jsonPath("$.name").value("asset.json"))
                .andExpect(jsonPath("$.order").value(1))
                .andExpect(jsonPath("$.active").value(true))
        ;

        verify(pageService).saveOrUpdateAsset(page.getId(), AssetType.JSON, "asset.json", fileContent);
    }

    @Test
    void should_return_error_when_uploading_with_error_onsave() throws Exception {
        final String pageId = "page-id";
        Page page = mockPageOfId(pageId);
        final byte[] content = "function(){}".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "lib.js", "application/javascript", content);

        final String message = "Error while saving internal asset";
        when(pageService.saveOrUpdateAsset(pageId, JAVASCRIPT, file.getOriginalFilename(), content))
                .thenThrow(new RepositoryException(message, new IllegalArgumentException()));

        mockMvc.perform(multipart("/rest/pages/{pageId}/assets/js", pageId).file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value("RepositoryException"))
                .andExpect(jsonPath("$.message").value(message))
        ;
    }

    @Test
    void should_check_that_json_is_well_formed_while_uploading_a_json_asset() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "asset.json", "application/javascript", "{ not json }".getBytes());

        mockMvc.perform(multipart("/rest/pages/page-id/assets/json").file(file))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("MalformedJsonException"))
        ;
    }

}
