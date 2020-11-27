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
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.convertObjectToJsonBytes;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.joda.time.Instant.parse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de {@link org.bonitasoft.web.designer.controller.WidgetResource}
 */
public class WidgetResourceTest {

    private MockMvc mockMvc;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock WidgetService widgetService;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private AssetService<Widget> widgetAssetService;

    private Path widgetRepositoryPath;

    private AssetVisitor assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

    @Before
    public void setUp() throws URISyntaxException {
        initMocks(this);
        widgetRepositoryPath = Paths.get(getClass().getResource("/workspace/widgets").toURI());
        WidgetResource widgetResource = new WidgetResource(
                new DesignerConfig().objectMapperWrapper(),
                widgetRepository,
                widgetService,
                widgetAssetService,
                widgetRepositoryPath,
                asList(pageRepository, fragmentRepository), assetVisitor);
        mockMvc = UIDesignerMockMvcBuilder.mockServer(widgetResource).build();
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(pageRepository.getComponentName()).thenReturn("page");
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
    }

    @Test
    public void should_serve_all_widgets_in_repository() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").build();
        when(widgetRepository.getAll(false)).thenReturn(asList(input, label));

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")))
                .andExpect(jsonPath("$[*].type").value(everyItem(is("widget"))));
    }

    @Test
    public void should_serve_empty_list_if_widget_repository_is_empty() throws Exception {
        when(widgetRepository.getAll()).thenReturn(new ArrayList<Widget>());

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void should_respond_500_internal_server_error_if_an_error_occurs_while_getting_widgets() throws Exception {
        when(widgetRepository.getAll(false)).thenThrow(new RepositoryException("error occurs", new Exception()));

        mockMvc.perform(get("/rest/widgets")).andExpect(status().is(500));
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget input = aWidget().id("input").build();

        when(widgetService.get("input")).thenReturn(input);

        mockMvc.perform(get("/rest/widgets/input"))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(input)));
    }

    @Test
    public void should_get_a_widget_with_asset_by_its_id() throws Exception {
        Widget input = aWidget().id("input").assets(anAsset().withName("myScopeWidgetAsset").withType(AssetType.CSS))
                .build();
        when(widgetService.get("input")).thenReturn(input);

        mockMvc.perform(get("/rest/widgets/input"))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(input)))
                .andExpect(jsonPath("assets[*].scope").exists())
                .andExpect(jsonPath("assets[*].scope").value(everyItem(is("widget"))));
    }

    @Test
    public void should_respond_404_when_getting_an_unexisting_widget() throws Exception {
        when(widgetService.get("notExistingWidget")).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/rest/widgets/notExistingWidget"))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_save_a_widget() throws Exception {
        Widget customLabel = aWidget().id("customLabel").custom().build();

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isOk());

        verify(widgetRepository).updateLastUpdateAndSave(customLabel);
    }

    @Test
    public void should_not_allow_to_save_a_pb_widget() throws Exception {
        Widget pbWidget = aWidget().custom().build();

        mockMvc.perform(put("/rest/widgets/pbLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(pbWidget)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void should_not_allow_to_save_a_not_custom_widget() throws Exception {
        Widget pbWidget = aWidget().id("input").build();
        when(widgetService.get("input")).thenReturn(pbWidget);

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(pbWidget)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void should_respond_500_internal_server_error_if_an_error_occurs_while_saving_a_widget() throws Exception {
        doThrow(new RepositoryException("error occurs", new Exception())).when(widgetRepository).updateLastUpdateAndSave(any(Widget.class));
        Widget customLabel = aWidget().id("customLabel").custom().build();

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isInternalServerError());

    }

    @Test
    public void should_create_a_new_widget() throws Exception {
        Widget customLabel = aWidget().name("label").custom().build();
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isOk());

        verify(widgetRepository).create(notNull(Widget.class));
    }

    @Test
    public void should_duplicate_a_widget_from_a_widget() throws Exception {
        Widget customLabel = aWidget().name("label").assets(anAsset().withName("myfile.js")).custom().build();
        when(widgetRepository.get("my-widget-source")).thenReturn(aWidget().id("my-widget-source").name("label").assets(anAsset().withName("myfile.js")).custom().build());
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);

        mockMvc
                .perform(post("/rest/widgets?duplicata=my-widget-source")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isOk());

        verify(widgetRepository).create(notNull(Widget.class));
        verify(widgetAssetService).duplicateAsset(any(Path.class), any(Path.class), eq("my-widget-source"), anyString());
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_an_empty_name() throws Exception {
        Widget customLabel = aWidget().name("").custom().build();
        when(widgetRepository.create(customLabel)).thenThrow(new IllegalArgumentException());

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_an_existing_name() throws Exception {
        Widget customLabel = aWidget().name("alreadyExistingName").build();
        when(widgetRepository.create(customLabel)).thenThrow(new NotAllowedException("already existing name"));

        mockMvc
                .perform(post("/rest/widgets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(customLabel)))
                .andExpect(status().isForbidden());

    }

    @Test
    public void should_delete_a_widget() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().id("customLabel").build());
        mockMvc.perform(delete("/rest/widgets/customLabel")).andExpect(status().isOk());
        verify(widgetRepository).delete("customLabel");
    }

    @Test
    public void should_not_allow_to_delete_a_pb_widget() throws Exception {
        when(widgetRepository.get("pbWidget")).thenReturn(aWidget().id("pbWidget").build());
        mockMvc.perform(delete("/rest/widgets/pbWidget")).andExpect(status().isForbidden());
    }

    @Test
    public void should_respond_404_if_trying_to_delete_an_unknown_widget() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().id("customLabel").build());
        doThrow(new NotFoundException("not found")).when(widgetRepository).get("customLabel");
        mockMvc.perform(delete("/rest/widgets/customLabel")).andExpect(status().isNotFound());
    }

    @Test
    public void should_not_allow_to_delete_a_custom_widget_used_in_a_page() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().id("customLabel").build());
        when(pageRepository.containsObject("customLabel")).thenReturn(true);
        when(pageRepository.getArtifactsUsingWidget("customLabel")).thenReturn(asList(aPage().withName("person").build()));

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("The widget cannot be deleted because it is used in 1 page, <person>"));
    }

    @Test
    public void should_respond_404_not_found_if_custom_widget_is_not_existing_when_renaming() throws Exception {
        when(widgetRepository.get("my-widget")).thenThrow(new NotFoundException("page not found"));

        mockMvc
                .perform(put("/rest/widgets/my-widget/name").contentType(MediaType.APPLICATION_JSON_VALUE).content("hello"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void should_add_a_property_to_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetRepository.addProperty("customLabel", property)).thenReturn(expectedProperties);

        mockMvc.perform(post("/rest/widgets/customLabel/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedProperties)));

        verify(widgetRepository).addProperty("customLabel", property);
    }

    @Test
    public void should_not_allow_to_add_a_property_to_a_pb_widget() throws Exception {
        mockMvc.perform(post("/rest/widgets/pbLabel/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isForbidden());
    }

    @Test
    public void should_respond_404_when_adding_a_property_to_an_unexisting_widget() throws Exception {
        when(widgetRepository.addProperty(eq("unknownWidget"), any(Property.class))).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(post("/rest/widgets/unknownWidget/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_when_error_appear_while_saving_property() throws Exception {
        when(widgetRepository.addProperty(eq("label"), any(Property.class))).thenThrow(RepositoryException.class);

        mockMvc.perform(post("/rest/widgets/label/properties")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_update_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetService.updateProperty("customLabel", "toBeUpdated", property)).thenReturn(expectedProperties);

        mockMvc.perform(put("/rest/widgets/customLabel/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedProperties)));

        verify(widgetService).updateProperty("customLabel", "toBeUpdated", property);
    }

    @Test
    public void should_not_allow_to_update_a_property_of_a_pb_widget() throws Exception {
        mockMvc.perform(put("/rest/widgets/pbLabel/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isForbidden());
    }

    @Test
    public void should_respond_404_when_widget_or_property_not_found_while_updating_property() throws Exception {
        when(widgetService.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_when_error_appear_while_updating_property() throws Exception {
        when(widgetService.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(RepositoryException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_delete_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetRepository.deleteProperty("customLabel", "toBeDeleted")).thenReturn(expectedProperties);

        mockMvc.perform(delete("/rest/widgets/customLabel/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedProperties)));

        verify(widgetRepository).deleteProperty("customLabel", "toBeDeleted");
    }

    @Test
    public void should_not_allow_to_delete_a_property_of_a_pb_widget() throws Exception {
        mockMvc.perform(delete("/rest/widgets/pbLabel/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isForbidden())
                .andExpect(jsonPath("message").value("Not allowed to modify a non custom widgets"));
    }

    @Test
    public void should_respond_404_when_widget_or_property_not_found_while_deleting_property() throws Exception {
        when(widgetRepository.deleteProperty("label", "toBeDeleted")).thenThrow(new NotFoundException("Widget [ toBeDeleted ] not found"));

        mockMvc.perform(delete("/rest/widgets/label/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Widget [ toBeDeleted ] not found"));

    }

    @Test
    public void should_respond_500_when_error_appear_while_deleting_property() throws Exception {
        when(widgetRepository.deleteProperty("label", "toBeDeleted")).thenThrow(RepositoryException.class);

        mockMvc.perform(delete("/rest/widgets/label/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_upload_a_local_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withType(AssetType.JAVASCRIPT).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetService.upload(file, widget, "js")).thenReturn(expectedAsset);

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

        verify(widgetAssetService).upload(file, widget, "js");
    }

    @Test
    public void should_not_upload_an_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doThrow(IllegalArgumentException.class).when(widgetAssetService).upload(file, widget, "js");

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file))
                .andExpect(status().isBadRequest());

        verify(widgetAssetService).upload(file, widget, "js");
    }

    @Test
    public void should_not_upload_an_asset_for_custom_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doThrow(IllegalArgumentException.class).when(widgetAssetService).upload(file, widget, "js");

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_save_an_external_asset() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withType(AssetType.JAVASCRIPT).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetService.save(widget, expectedAsset)).thenReturn(expectedAsset);

        mockMvc.perform(
                post("/rest/widgets/my-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(expectedAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("assetId"))
                .andExpect(jsonPath("$.name").value("myfile.js"))
                .andExpect(jsonPath("$.type").value("js"))
                .andExpect(jsonPath("$.order").value(2));

        verify(widgetAssetService).save(widget, expectedAsset);
    }

    @Test
    public void should_not_save_an_external_asset_for_internal_widget() throws Exception {
        Widget widget = aWidget().id("pb-widget").build();
        Asset asset = anAsset().build();
        when(widgetRepository.get("pb-widget")).thenReturn(widget);

        mockMvc.perform(
                post("/rest/widgets/pb-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset asset = anAsset().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        doThrow(IllegalArgumentException.class).when(widgetAssetService).save(widget, asset);

        mockMvc.perform(
                post("/rest/widgets/my-widget/assets")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_delete_an_asset() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset asset = anAsset().withComponentId("my-widget").build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        mockMvc.perform(
                delete("/rest/widgets/my-widget/assets/UIID")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        verify(widgetAssetService).delete(widget, "UIID");
    }

    @Test
    public void should_increment_an_asset() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset asset = anAsset().withComponentId("my-widget").withOrder(3).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        mockMvc.perform(
                put("/rest/widgets/my-widget/assets/UIID?increment=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isOk());

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", INCREMENT);
    }

    @Test
    public void should_decrement_an_asset() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        Asset asset = anAsset().withComponentId("my-widget").withOrder(3).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        mockMvc.perform(
                put("/rest/widgets/my-widget/assets/UIID?decrement=true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(asset)))
                .andExpect(status().isOk());

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", DECREMENT);
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/widgets/my-widget/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(widgetRepository).markAsFavorite("my-widget");
    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/widgets/my-widget/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(widgetRepository).unmarkAsFavorite("my-widget");
    }

    private String toJson(Object o) throws IOException {
        return new String(convertObjectToJsonBytes(o));
    }

    @Test
    public void should_load_widget_asset_on_disk_with_content_type_text() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_download_widget_asset() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(expectedFile);

        mockMvc
                .perform(get("/rest/widgets/widget-id/assets/js/asset.js"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_widget_asset_included_in_page_is_not_found() throws Exception {
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenReturn(null);

        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text")).andExpect(status().isNotFound());
        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_when_widget_asset_included_in_page_produce_IOException() throws Exception {
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix())).thenThrow(new IOException("can't read file"));

        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js?format=text")).andExpect(status().isInternalServerError());
        mockMvc.perform(get("/rest/widgets/widget-id/assets/js/asset.js")).andExpect(status().isInternalServerError());
    }

    @Test
    @Ignore("Test ignored because failed on CI")
     public void should_download_help() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbText/help.html");

        mockMvc
                .perform(get("/rest/widgets/pbText/help"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"help.html\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_widget_help_is_not_found() throws Exception {
        mockMvc.perform(get("/rest/widgets/pbLabel/help")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_422_when_custom_widget_is_incompatible() throws Exception {
        Widget widget = aWidget().id("my-widget").custom().build();
        widget.setStatus(new MigrationStatusReport(false, true));
        when(widgetService.get("my-widget")).thenReturn(widget);

        mockMvc.perform(get("/rest/widgets/my-widget")).andExpect(status().is(422));
    }

    @Test
    public void should_serve_all_light_widgets_in_repository() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").lastUpdate(parse("2015-02-02")).build();
        when(widgetRepository.getAll(false)).thenReturn(asList(input, label));
        String[] ids = {"input", "label"};
        Map<String, List<Page>> map = new HashMap();
        map.put("input", singletonList(aPage().withName("hello").build()));
        when(pageRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map);
        Map<String, List<Fragment>> map2 = new HashMap();
        map2.put("label", singletonList(aFragment().withName("helloFragment").build()));
        when(fragmentRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map2);

        mockMvc.perform(get("/rest/widgets?view=light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")))
                .andExpect(jsonPath("$[*].lastUpdate").value(hasItem(parse("2015-02-02").getMillis())))
                .andExpect(jsonPath("$[*].usedBy.fragment[*].name").value(hasItems("helloFragment")))
                .andExpect(jsonPath("$[*].usedBy.page[*].name").value(hasItems("hello")));
    }

    @Test
    public void should_not_allow_to_delete_a_custom_widget_used_in_a_fragment() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().id("customLabel").build());
        when(fragmentRepository.containsObject("customLabel")).thenReturn(true);
        when(fragmentRepository.getArtifactsUsingWidget("customLabel")).thenReturn(singletonList(aFragment().withName("person").build()));

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("The widget cannot be deleted because it is used in 1 fragment, <person>"));
    }
}
