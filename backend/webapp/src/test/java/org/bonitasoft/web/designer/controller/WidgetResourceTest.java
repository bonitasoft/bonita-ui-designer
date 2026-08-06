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

import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.ArtifactStatusReport;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.DefaultWidgetService;
import org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.nio.file.Files.readAllBytes;
import static java.time.Instant.parse;
import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de {@link org.bonitasoft.web.designer.controller.WidgetResource}
 */
@ExtendWith(MockitoExtension.class)
class WidgetResourceTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
	DefaultWidgetService widgetService;

    private MockMvc mockMvc;

    private WorkspaceProperties workspaceProperties;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getWidgets().setDir(Paths.get(getClass().getResource("/workspace/widgets").toURI()));
        WidgetResource widgetResource = new WidgetResource(
                jsonHandler,
                widgetService,
                null,
                workspaceProperties
        );
        mockMvc = UIDesignerMockMvcBuilder.mockServer(widgetResource).build();
    }

    @Test
    void should_serve_all_widgets_in_repository() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").build();
        when(widgetService.getAll()).thenReturn(asList(input, label));

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")))
                .andExpect(jsonPath("$[*].type").value(everyItem(is("widget"))));
    }

    @Test
    void should_serve_empty_list_if_widget_repository_is_empty() throws Exception {
        when(widgetService.getAll()).thenReturn(new ArrayList<Widget>());

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void should_respond_500_internal_server_error_if_an_error_occurs_while_getting_widgets() throws Exception {
        when(widgetService.getAll()).thenThrow(new RepositoryException("error occurs", new Exception()));

        mockMvc.perform(get("/rest/widgets")).andExpect(status().is(500));
    }

    @Test
    void should_get_a_widget_by_its_id() throws Exception {
        Widget input = aWidget().withId("input").build();
        when(widgetService.getWithAsset("input")).thenReturn(input);

        mockMvc.perform(get("/rest/widgets/input"))

                .andExpect(status().isOk())
                .andExpect(content().json(jsonHandler.toJsonString(input)));
    }

    @Test
    void should_get_a_widget_with_asset_by_its_id() throws Exception {
        Widget input = aWidget().withId("input").assets(anAsset().withName("myScopeWidgetAsset").withScope("widget").withType(AssetType.CSS))
                .build();
        when(widgetService.getWithAsset("input")).thenReturn(input);

        mockMvc.perform(get("/rest/widgets/input"))

                .andExpect(status().isOk())
                .andExpect(content().json(jsonHandler.toJsonString(input)))
                .andExpect(jsonPath("assets[*].scope").exists())
                .andExpect(jsonPath("assets[*].scope").value(everyItem(is("widget"))));
    }


    @Test
    void should_respond_404_when_getting_an_unexisting_widget() throws Exception {
        when(widgetService.getWithAsset("notExistingWidget")).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/rest/widgets/notExistingWidget"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_save_a_widget() throws Exception {
        Widget customLabel = aWidget().withId("customLabel").custom().build();

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isOk());

        verify(widgetService).save("customLabel", customLabel);
    }

    @Test
    void should_not_allow_to_save_a_pb_widget() throws Exception {
        Widget pbWidget = aWidget().custom().build();

        when(widgetService.save("pbLabel", pbWidget)).thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(put("/rest/widgets/pbLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(pbWidget)))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_not_allow_to_save_a_not_custom_widget() throws Exception {
        Widget pbWidget = aWidget().withId("input").build();

        when(widgetService.save("customLabel", pbWidget)).thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(pbWidget)))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_respond_500_internal_server_error_if_an_error_occurs_while_saving_a_widget() throws Exception {
        Widget customLabel = aWidget().withId("customLabel").custom().build();
        doThrow(new RepositoryException("error occurs", new Exception())).when(widgetService).save("customLabel", customLabel);

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_create_a_new_widget() throws Exception {
        Widget customLabel = aWidget().withName("label").custom().build();
        when(widgetService.create(customLabel)).thenReturn(customLabel);

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isOk());

        verify(widgetService).create(notNull());
    }

    @Test
    void should_duplicate_a_widget_from_a_widget() throws Exception {
        Widget customLabel = aWidget().withName("label").assets(anAsset().withName("myfile.js")).custom().build();
        String sourceWidgetId = "my-widget-source";


        mockMvc
                .perform(post("/rest/widgets?duplicata=" + sourceWidgetId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isOk());

        verify(widgetService).createFrom(sourceWidgetId, customLabel);
    }

    @Test
    void should_not_allow_to_create_a_widget_with_an_empty_name() throws Exception {
        Widget customLabel = aWidget().withName("").custom().build();
        when(widgetService.create(customLabel)).thenThrow(new IllegalArgumentException());

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_not_allow_to_create_a_widget_with_an_existing_name() throws Exception {
        Widget customLabel = aWidget().withName("alreadyExistingName").build();
        when(widgetService.create(customLabel)).thenThrow(new NotAllowedException("already existing name"));

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(customLabel)))
                .andExpect(status().isForbidden());

    }

    @Test
    void should_delete_a_widget() throws Exception {
        mockMvc.perform(delete("/rest/widgets/customLabel")).andExpect(status().isOk());
        verify(widgetService).delete("customLabel");
    }

    @Test
    void should_not_allow_to_delete_a_pb_widget() throws Exception {
        doThrow(new NotAllowedException("We can only delete a custom widget")).when(widgetService).delete("pbWidget");
        mockMvc.perform(delete("/rest/widgets/pbWidget")).andExpect(status().isForbidden());
    }

    @Test
    void should_respond_404_if_trying_to_delete_an_unknown_widget() throws Exception {
        doThrow(new NotFoundException("not found")).when(widgetService).delete("customLabel");
        mockMvc.perform(delete("/rest/widgets/customLabel")).andExpect(status().isNotFound());
    }

    @Test
    void should_not_allow_to_delete_a_custom_widget_used_in_a_page() throws Exception {
        doThrow(new InUseException("The widget cannot be deleted because it is used in 1 page, <person>")).when(widgetService).delete("customLabel");

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("The widget cannot be deleted because it is used in 1 page, <person>"));
    }

    @Test
    void should_respond_404_not_found_if_custom_widget_is_not_existing_when_renaming() throws Exception {
        mockMvc
                .perform(put("/rest/widgets/my-widget/name").contentType(MediaType.APPLICATION_JSON_VALUE).content("hello"))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_add_a_property_to_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = Collections.singletonList(property);
        when(widgetService.addProperty("customLabel", property)).thenReturn(expectedProperties);

        mockMvc.perform(post("/rest/widgets/customLabel/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(jsonHandler.toJsonString(expectedProperties)));

        verify(widgetService).addProperty("customLabel", property);
    }

    @Test
    void should_not_allow_to_add_a_property_to_a_pb_widget() throws Exception {
        Property property = aProperty().build();
        when(widgetService.addProperty("pbLabel", property))
                .thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(post("/rest/widgets/pbLabel/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(property)))

                .andExpect(status().isForbidden());
    }

    @Test
    void should_respond_404_when_adding_a_property_to_an_unexisting_widget() throws Exception {
        when(widgetService.addProperty(eq("unknownWidget"), any(Property.class))).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(post("/rest/widgets/unknownWidget/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_error_appear_while_saving_property() throws Exception {
        when(widgetService.addProperty(eq("label"), any(Property.class))).thenThrow(RepositoryException.class);

        mockMvc.perform(post("/rest/widgets/label/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_update_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = Collections.singletonList(property);
        when(widgetService.updateProperty("customLabel", "toBeUpdated", property)).thenReturn(expectedProperties);

        mockMvc.perform(put("/rest/widgets/customLabel/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(jsonHandler.toJsonString(expectedProperties)));

        verify(widgetService).updateProperty("customLabel", "toBeUpdated", property);
    }

    @Test
    void should_not_allow_to_update_a_property_of_a_pb_widget() throws Exception {
        Property property = aProperty().build();
        when(widgetService.updateProperty("pbLabel", "toBeUpdated", property))
                .thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(put("/rest/widgets/pbLabel/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(property)))

                .andExpect(status().isForbidden());
    }

    @Test
    void should_respond_404_when_widget_or_property_not_found_while_updating_property() throws Exception {
        when(widgetService.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_error_appear_while_updating_property() throws Exception {
        when(widgetService.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(RepositoryException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_delete_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = Collections.singletonList(property);
        when(widgetService.deleteProperty("customLabel", "toBeDeleted")).thenReturn(expectedProperties);

        mockMvc.perform(delete("/rest/widgets/customLabel/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(jsonHandler.toJsonString(expectedProperties)));

        verify(widgetService).deleteProperty("customLabel", "toBeDeleted");
    }

    @Test
    void should_not_allow_to_delete_a_property_of_a_pb_widget() throws Exception {
        when(widgetService.deleteProperty("pbLabel", "toBeDeleted"))
                .thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(delete("/rest/widgets/pbLabel/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isForbidden())
                .andExpect(jsonPath("message").value("Not allowed to modify a non custom widgets"));
    }

    @Test
    void should_respond_404_when_widget_or_property_not_found_while_deleting_property() throws Exception {
        when(widgetService.deleteProperty("label", "toBeDeleted")).thenThrow(new NotFoundException("Widget [ toBeDeleted ] not found"));

        mockMvc.perform(delete("/rest/widgets/label/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Widget [ toBeDeleted ] not found"));

    }

    @Test
    void should_respond_500_when_error_appear_while_deleting_property() throws Exception {
        when(widgetService.deleteProperty("label", "toBeDeleted")).thenThrow(RepositoryException.class);

        mockMvc.perform(delete("/rest/widgets/label/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(jsonHandler.toJson(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    void should_upload_a_local_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withType(AssetType.JAVASCRIPT).build();

        when(widgetService.saveOrUpdateAsset(eq("my-widget"), eq(expectedAsset.getType()), eq(expectedAsset.getName()), any())).thenReturn(expectedAsset);

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));
    }

    @Test
    void should_not_upload_an_empty_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "".getBytes());

        mockMvc.perform(multipart("/rest/widgets/my-widget/assets/js").file(file))
                .andExpect(status().isBadRequest());

    }

    @Test
    void should_not_upload_an_asset_with_unknown_type() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "hello".getBytes());

        mockMvc.perform(multipart("/rest/widgets/my-widget/assets/unknown").file(file))
                .andExpect(status().isBadRequest());
    }


    @Test
    void should_not_upload_an_asset_for_internal_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());

        when(widgetService.saveOrUpdateAsset("pbwidget", AssetType.JAVASCRIPT, "myfile.js", "foo".getBytes()))
                .thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(multipart("/rest/widgets/pbwidget/assets/js").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_save_an_external_asset() throws Exception {
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withType(AssetType.JAVASCRIPT).build();
        when(widgetService.saveAsset("my-widget", expectedAsset)).thenReturn(expectedAsset);

        mockMvc.perform(
                post("/rest/widgets/my-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(expectedAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

        verify(widgetService).saveAsset("my-widget", expectedAsset);
    }

    @Test
    void should_not_save_an_external_asset_for_internal_widget() throws Exception {
        Asset asset = anAsset().build();
        when(widgetService.saveAsset("pb-widget", asset))
                .thenThrow(new NotAllowedException("Not allowed to modify a non custom widgets"));

        mockMvc.perform(
                post("/rest/widgets/pb-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Asset asset = anAsset().build();
        when(widgetService.saveAsset("my-widget", asset)).thenThrow(IllegalArgumentException.class);

        mockMvc.perform(
                post("/rest/widgets/my-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_delete_an_asset() throws Exception {

        mockMvc.perform(
                delete("/rest/widgets/my-widget/assets/UIID")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(widgetService).deleteAsset("my-widget", "UIID");
    }

    @Test
    void should_increment_an_asset() throws Exception {
        Asset asset = anAsset().withComponentId("my-widget").withOrder(3).build();

        mockMvc.perform(
                put("/rest/widgets/my-widget/assets/UIID?increment=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isOk());

        verify(widgetService).changeAssetOrder("my-widget", "UIID", INCREMENT);
        verify(widgetService, times(0)).changeAssetStateInPreviewable("my-widget", "UIID", true);
        verify(widgetService, times(0)).changeAssetStateInPreviewable("my-widget", "UIID", false);
    }

    @Test
    void should_decrement_an_asset() throws Exception {
        Asset asset = anAsset().withComponentId("my-widget").withOrder(3).build();

        mockMvc.perform(
                put("/rest/widgets/my-widget/assets/UIID?decrement=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(asset)))
                .andExpect(status().isOk());

        verify(widgetService).changeAssetOrder("my-widget", "UIID", DECREMENT);
        verify(widgetService, times(0)).changeAssetStateInPreviewable("my-widget", "UIID", true);
        verify(widgetService, times(0)).changeAssetStateInPreviewable("my-widget", "UIID", false);
    }

    @Test
    void should_mark_a_widget_as_favorite() throws Exception {
        mockMvc
                .perform(
                        put("/rest/widgets/my-widget/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(widgetService).markAsFavorite("my-widget", true);
    }

    @Test
    void should_unmark_a_widget_as_favorite() throws Exception {
        mockMvc
                .perform(
                        put("/rest/widgets/my-widget/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(widgetService).markAsFavorite("my-widget", false);
    }

    @Test
    void should_load_widget_asset_on_disk_with_content_type_text() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.js");
        when(widgetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    void should_download_widget_asset() throws Exception {
        Path expectedFile = workspaceProperties.getWidgets().getDir().resolve("pbLabel/pbLabel.js");
        when(widgetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/widgets/widget-id/assets/js/asset.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        when(widgetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix()))
                .thenReturn(null);

        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text")).andExpect(status().isNotFound());
        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    void should_respond_500_when_widget_asset_included_in_page_produce_IOException() throws Exception {
        when(widgetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix()))
                .thenThrow(new IOException("can't read file"));

        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js")).andExpect(status().isInternalServerError());
    }

    @Test
    void should_respond_404_when_widget_help_is_not_found() throws Exception {
        mockMvc.perform(get("/rest/widgets/pbLabel/help")).andExpect(status().isNotFound());
    }

    @Test
    void should_respond_422_when_custom_widget_is_incompatible() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        widget.setStatus(new ArtifactStatusReport(false, true));
        when(widgetService.getWithAsset("my-widget")).thenReturn(widget);

        mockMvc.perform(get("/rest/widgets/my-widget")).andExpect(status().is(422));
    }

    @Test
    void should_serve_all_light_widgets_in_repository() throws Exception {
        Page page = aPage().withName("hello").build();
        Fragment fragment = aFragment().withName("helloFragment").build();

        Instant lastUpdateDate = parse("2015-02-02T00:00:00.000Z");
        Widget input = aWidget().withId("input").lastUpdate(lastUpdateDate).build();
        input.addUsedBy("page", Collections.singletonList(page));

        Widget label = aWidget().withId("label").lastUpdate(lastUpdateDate).build();
        label.addUsedBy("fragment", Collections.singletonList(fragment));

        List<Widget> returnedWidgets = asList(input,label);

        when(widgetService.getAllWithUsedBy()).thenReturn(returnedWidgets);

        mockMvc.perform(get("/rest/widgets?view=light")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")))
                .andExpect(jsonPath("$[*].lastUpdate").value(hasItem(lastUpdateDate.toEpochMilli())))
                .andExpect(jsonPath("$[*].usedBy.fragment[*].name").value(hasItems("helloFragment")))
                .andExpect(jsonPath("$[*].usedBy.page[*].name").value(hasItems("hello")));
    }

    @Test
    void should_not_allow_to_delete_a_custom_widget_used_in_a_fragment() throws Exception {
        doThrow(new InUseException("The widget cannot be deleted because it is used in 1 fragment, <person>"))
                .when(widgetService).delete("customLabel");

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("The widget cannot be deleted because it is used in 1 fragment, <person>"));
    }
}
