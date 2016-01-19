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

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.nio.file.Files.readAllBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.model.asset.AssetScope.PAGE;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aSimpleContract;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.convertObjectToJsonBytes;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.FakeJsonProcessingException;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.MalformedJsonException;
import org.bonitasoft.web.designer.experimental.mapping.ContractToPageMapper;
import org.bonitasoft.web.designer.experimental.mapping.FormScope;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.hamcrest.Matchers;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.google.common.collect.Sets;

/**
 * Test de {@link org.bonitasoft.web.designer.controller.PageResource}
 */
@RunWith(MockitoJUnitRunner.class)
public class PageResourceTest {

    private MockMvc mockMvc;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ContractToPageMapper contractToPageMapper;

    @Mock
    private AssetService<Page> pageAssetService;

    @Mock
    private AssetVisitor assetVisitor;

    @InjectMocks
    private PageResource pageResource;

    private Path widgetRepositoryPath;

    @Before
    public void setUp() throws URISyntaxException {
        mockMvc = mockServer(pageResource).build();
        widgetRepositoryPath = Paths.get(getClass().getResource("/workspace/widgets").toURI());
    }

    private Page mockPageOfId(String id) {
        Page page = aPage().withId(id).build();
        when(pageRepository.get(id)).thenReturn(page);
        return page;
    }

    @Test
    public void should_list_pages() throws Exception {
        Page page = new Page();
        page.setId("id");
        page.setName("name");
        page.setLastUpdate(Instant.parse("2015-02-02"));
        when(pageRepository.getAll()).thenReturn(Arrays.asList(page));

        mockMvc.perform(get("/rest/pages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id").value("id"))
                .andExpect(jsonPath("$[*].name").value("name"))
                .andExpect(jsonPath("$[*].lastUpdate").value(Instant.parse("2015-02-02").getMillis()));
    }

    @Test
    public void should_create_a_page_from_a_Page() throws Exception {
        Page pageToBeSaved = aPage().withId("my-page").build();
        List<Element> emptyRow = Collections.emptyList();
        List<List<Element>> rows = Collections.singletonList(emptyRow);
        pageToBeSaved.setRows(rows);
        pageToBeSaved.setName("test");

        mockMvc
                .perform(post("/rest/pages")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(pageToBeSaved)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageRepository).updateLastUpdateAndSave(notNull(Page.class));
        verify(pageAssetService).loadDefaultAssets(any(Page.class));
    }

    @Test
    public void should_duplicate_a_page_from_a_Page() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();
        Page pageToBeSaved = aPage().withId("my-page").withName("test").withAsset(pageAsset, widgetAsset).build();
        when(pageRepository.get("my-page-source"))
                .thenReturn(aPage().withId("my-page-source").withName("test").withAsset(pageAsset, widgetAsset).build());

        mockMvc
                .perform(post("/rest/pages?duplicata=my-page-source")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(pageToBeSaved)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        ArgumentCaptor<Page> argument = ArgumentCaptor.forClass(Page.class);
        verify(pageRepository).updateLastUpdateAndSave(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo(pageToBeSaved.getName());
        assertThat(argument.getValue().getAssets()).containsOnly(pageAsset);
        verify(pageAssetService).duplicateAsset(any(Path.class), any(Path.class), eq("my-page-source"), anyString());
    }

    @Test
    public void should_create_a_page_from_a_Contract_at_task_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(Contract.class), eq(FormScope.TASK))).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/task/myPage")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageRepository).updateLastUpdateAndSave(newPage);
    }

    @Test
    public void should_create_a_page_from_a_Contract_at_process_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(Contract.class), eq(FormScope.PROCESS))).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/process/myPage")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageRepository).updateLastUpdateAndSave(newPage);
    }

    @Test
    public void should_create_a_page_from_a_Contract_at_overview_scope() throws Exception {
        Contract contract = aSimpleContract();
        Page newPage = aPage().withName("myPage").build();
        when(contractToPageMapper.createFormPage(eq("myPage"), notNull(Contract.class), eq(FormScope.OVERVIEW))).thenReturn(newPage);

        mockMvc
                .perform(post("/rest/pages/contract/overview/myPage")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(contract)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));

        verify(pageRepository).updateLastUpdateAndSave(newPage);
    }

    @Test
    public void should_save_a_page() throws Exception {
        Page pageToBeSaved = aFilledPage("my-page");

        mockMvc
                .perform(
                        put("/rest/pages/my-page").contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(pageToBeSaved)))
                .andExpect(status().isOk());

        verify(pageRepository).updateLastUpdateAndSave(pageToBeSaved);
        verify(messagingTemplate).convertAndSend("/previewableUpdates", "my-page");
    }

    @Test
    public void should_not_save_widget_assets_while_saving_a_page() throws Exception {
        Page pageToBeSaved = aPage().withId("my-page").withAsset(
                aWidgetAsset(),
                aPageAsset()).build();
        Page expectedPage = aPage().withId("my-page").withAsset(
                aPageAsset()).build();

        mockMvc
                .perform(
                        put("/rest/pages/my-page").contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(pageToBeSaved)))
                .andExpect(status().isOk());

        verify(pageRepository).updateLastUpdateAndSave(expectedPage);
    }

    private Asset aPageAsset() {
        return anAsset().withName("myJs.js").withType(AssetType.JAVASCRIPT).build();
    }

    private Asset aWidgetAsset() {
        return anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build();
    }

    @Test
    public void should_respond_415_unsupported_media_type_when_trying_to_save_non_json_content() throws Exception {

        mockMvc
                .perform(put("/rest/pages/my-page").content("this is not json"))
                .andExpect(status().is(415));
    }

    @Test
    public void should_respond_500_internal_error_if_error_occurs_while_saving_a_page() throws Exception {
        Page page = aPage().withId("my-page").build();
        Mockito.doThrow(new RepositoryException("exception occurs", new Exception())).when(pageRepository).updateLastUpdateAndSave(page);

        mockMvc
                .perform(
                        put("/rest/pages/my-page").contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(page)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_retrieve_a_page_representation_by_its_id() throws Exception {
        Page expectedPage = aFilledPage("my-page");
        when(pageRepository.get("my-page")).thenReturn(expectedPage);

        mockMvc
                .perform(get("/rest/pages/my-page"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    public void should_respond_404_not_found_if_page_is_not_existing() throws Exception {
        when(pageRepository.get("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc.perform(get("/rest/pages/my-page")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_404_not_found_when_delete_inexisting_page() throws Exception {
        doThrow(new NotFoundException("page not found")).when(pageRepository).delete("my-page");

        mockMvc.perform(delete("/rest/pages/my-page")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_internal_error_when_error_on_deletion_page() throws Exception {
        doThrow(new RepositoryException("error occurs", new RuntimeException())).when(pageRepository).delete("my-page");

        mockMvc.perform(delete("/rest/pages/my-page")).andExpect(status().isInternalServerError());
    }

    @Test
    public void should_delete_a_page() throws Exception {
        mockMvc
                .perform(delete("/rest/pages/my-page"))
                .andExpect(status().isOk());
    }

    @Test
    public void should_rename_a_page() throws Exception {
        String newName = "my-page-new-name";
        mockPageOfId("my-page");

        mockMvc
                .perform(
                        put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(newName))
                .andExpect(status().isOk());

        ArgumentCaptor<Page> pageArgumentCaptor = ArgumentCaptor.forClass(Page.class);
        verify(pageRepository).updateLastUpdateAndSave(pageArgumentCaptor.capture());

        assertThat(pageArgumentCaptor.getValue().getName()).isEqualTo(newName);
    }

    @Test
    public void should_respond_404_not_found_if_page_is_not_existing_when_renaming() throws Exception {
        when(pageRepository.get("my-page")).thenThrow(new NotFoundException("page not found"));

        mockMvc
                .perform(put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes("hello")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_internal_error_if_error_occurs_while_renaming_a_page() throws Exception {
        doThrow(new RepositoryException("exception occurs", new Exception())).when(pageRepository).updateLastUpdateAndSave(any(Page.class));
        mockPageOfId("my-page");

        mockMvc
                .perform(put("/rest/pages/my-page/name").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes("hello")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_upload_a_local_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Page page = mockPageOfId("my-page");
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withScope(PAGE)
                .withType(AssetType.JAVASCRIPT).build();
        when(pageAssetService.upload(file, page, "js")).thenReturn(expectedAsset);

        mockMvc.perform(fileUpload("/rest/pages/my-page/assets/js").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.scope").value(PAGE.toString()))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

        verify(pageAssetService).upload(file, page, "js");
    }

    @Test
    public void should_respond_202_with_error_when_uploading_a_json_asset_with_malformed_json_file() throws Exception {
        byte[] content = "notvalidjson".getBytes();
        MockMultipartFile file = aJsonFileWithContent(content);
        int expectedLine = 4, expectedColumn = 2;
        when(pageAssetService.upload(file, mockPageOfId("my-page"), "json")).thenThrow(aMalformedJsonException(content, expectedLine, expectedColumn));

        mockMvc.perform(fileUpload("/rest/pages/my-page/assets/json").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.type").value("MalformedJsonException"))
                .andExpect(jsonPath("$.infos.location.line").value(expectedLine))
                .andExpect(jsonPath("$.infos.location.column").value(expectedColumn));
    }

    private MockMultipartFile aJsonFileWithContent(byte[] content) {
        return new MockMultipartFile("file", "myfile.js", "application/json", content);
    }

    private MalformedJsonException aMalformedJsonException(byte[] bytes, int errorLine, int errorColumn) {
        return new MalformedJsonException(new FakeJsonProcessingException("Error while checking json", bytes, errorLine, errorColumn));
    }

    @Test
    public void should_not_upload_an_asset_when_upload_send_an_error() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Page page = mockPageOfId("my-page");
        doThrow(IllegalArgumentException.class).when(pageAssetService).upload(file, page, "js");

        mockMvc.perform(fileUpload("/rest/pages/my-page/assets/js").file(file))
                .andExpect(status().isBadRequest());

        verify(pageAssetService).upload(file, page, "js");
    }

    @Test
    public void should_save_an_external_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withScope(PAGE)
                .withType(AssetType.JAVASCRIPT).build();
        when(pageAssetService.save(page, expectedAsset)).thenReturn(expectedAsset);

        mockMvc.perform(
                post("/rest/pages/my-page/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(expectedAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.scope").value(PAGE.toString()))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

        verify(pageAssetService).save(page, expectedAsset);
    }

    @Test
    public void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().build();
        doThrow(IllegalArgumentException.class).when(pageAssetService).save(page, asset);

        mockMvc.perform(
                post("/rest/pages/my-page/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_list_page_assets() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset[] assets = new Asset[] {
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(JAVASCRIPT).withScope(AssetScope.PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withScope(AssetScope.PAGE).withType(JAVASCRIPT).build()
        };
        when(assetVisitor.visit(page)).thenReturn(Sets.newHashSet(assets));

        mockMvc.perform(get("/rest/pages/my-page/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].name", Matchers.containsInAnyOrder("https://mycdn.com/myExternalJs.js", "myJs.js", "myCss.css")))
                .andExpect(jsonPath("$[*].type", Matchers.containsInAnyOrder("js", "js", "css")))
                .andExpect(jsonPath("$[*].scope", Matchers.containsInAnyOrder("page", "page", "widget")))
                .andExpect(jsonPath("$[*].componentId", Matchers.containsInAnyOrder("widget-id")));

    }

    @Test
    public void should_list_page_assets_while_getting_a_page() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset[] assets = new Asset[] {
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(AssetType.JAVASCRIPT).withScope(AssetScope.PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withType(AssetType.JAVASCRIPT).withScope(AssetScope.PAGE).build()
        };
        when(assetVisitor.visit(page)).thenReturn(Sets.newHashSet(assets));

        mockMvc.perform(get("/rest/pages/my-page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets", hasSize(3)))
                .andExpect(jsonPath("$.assets[*].name", Matchers.containsInAnyOrder("https://mycdn.com/myExternalJs.js", "myJs.js", "myCss.css")))
                .andExpect(jsonPath("$.assets[*].type", Matchers.containsInAnyOrder("js", "js", "css")))
                .andExpect(jsonPath("$.assets[*].scope", Matchers.containsInAnyOrder("page", "page", "widget")))
                .andExpect(jsonPath("$.assets[*].componentId", Matchers.containsInAnyOrder("widget-id")));
    }

    @Test
    public void should_increment_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId("my-page").withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?increment=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isOk());

        verify(pageAssetService).changeAssetOrderInComponent(page, "UIID", INCREMENT);
    }

    @Test
    public void should_decrement_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId("my-page").withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?decrement=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isOk());

        verify(pageAssetService).changeAssetOrderInComponent(page, "UIID", DECREMENT);
    }

    @Test
    public void should_delete_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");

        mockMvc.perform(
                delete("/rest/pages/my-page/assets/UIID")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(pageAssetService).delete(page, "UIID");
    }

    @Test
    public void should_inactive_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId(page.getId()).withOrder(3).build();

        mockMvc.perform(
                put("/rest/pages/my-page/assets/UIID?active=false")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isOk());

        verify(pageAssetService).changeAssetStateInPreviewable(page, "UIID", false);
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/pages/my-page/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(pageRepository).markAsFavorite("my-page");
    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/pages/my-page/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(pageRepository).unmarkAsFavorite("my-page");
    }

    @Test
    public void should_load_page_asset_on_disk_with_content_type_text() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(pageAssetService.findAssetPath("page-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/pages/page-id/assets/js/asset.js?format=text"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_download_page_asset() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(pageAssetService.findAssetPath("page-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/pages/page-id/assets/js/asset.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        when(pageAssetService.findAssetPath("page-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(null);

        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js?format=text")).andExpect(status().isNotFound());
        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_when_widget_asset_included_in_page_produce_IOException() throws Exception {
        when(pageAssetService.findAssetPath("page-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenThrow(new IOException("can't read file"));

        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js?format=text")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/rest/pages/page-id/assets/js/asset.js")).andExpect(status().isInternalServerError());
    }

}
