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
import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.convertObjectToJsonBytes;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.createContextForTest;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.joda.time.Instant.parse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.controller.upload.AssetUploader;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Mock
    private PageRepository pageRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AssetUploader<Widget> widgetAssetUploader;

    @Before
    public void setUp() {
        initMocks(this);
        WidgetResource widgetResource = new WidgetResource(new DesignerConfig().objectMapperWrapper(), widgetRepository, widgetAssetUploader);
        widgetResource.setUsedByRepositories(Arrays.<Repository>asList(widgetRepository, pageRepository));
        mockMvc = standaloneSetup(widgetResource)
                .setHandlerExceptionResolvers(createContextForTest().handlerExceptionResolver())
                .build();
        when(widgetRepository.getComponentName()).thenReturn("widget");
        when(pageRepository.getComponentName()).thenReturn("page");
    }

    @Test
    public void should_serve_all_widgets_in_repository() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").build();
        when(widgetRepository.getAll()).thenReturn(asList(input, label));

        mockMvc.perform(get("/rest/widgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")));
    }

    @Test
    public void should_serve_all_light_widgets_in_repository() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").lastUpdate(parse("2015-02-02")).build();
        when(widgetRepository.getAll()).thenReturn(asList(input, label));
        when(pageRepository.findByObjectId(anyString())).thenReturn(asList(aPage().withName("hello").build()));
        when(widgetRepository.findByObjectId(anyString())).thenReturn(asList(aWidget().name("helloWidget").build()));

        mockMvc.perform(get("/rest/widgets?view=light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("input", "label")))
                .andExpect(jsonPath("$[*].lastUpdate").value(hasItem(parse("2015-02-02").getMillis())))
                .andExpect(jsonPath("$[*].usedBy.widget[*].name").value(hasItems("helloWidget")))
                .andExpect(jsonPath("$[*].usedBy.page[*].name").value(hasItems("hello")));
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
        when(widgetRepository.getAll()).thenThrow(new RepositoryException("error occurs", new Exception()));

        mockMvc.perform(get("/rest/widgets")).andExpect(status().is(500));
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget input = aWidget().id("input").build();
        when(widgetRepository.get("input")).thenReturn(input);

        mockMvc.perform(get("/rest/widgets/input"))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(input)));
    }

    @Test
    public void should_respond_404_when_getting_an_unexisting_widget() throws Exception {
        when(widgetRepository.get("notExistingWidget")).thenThrow(new NotFoundException("not found"));

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

        verify(widgetRepository).save(customLabel);
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
        Widget pbWidget = aWidget().build();

        mockMvc.perform(put("/rest/widgets/customLabel")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(pbWidget)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void should_respond_500_internal_server_error_if_an_error_occurs_while_saving_a_widget() throws Exception {
        doThrow(new RepositoryException("error occurs", new Exception())).when(widgetRepository).save(any(Widget.class));
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
        when(pageRepository.findByObjectId("customLabel")).thenReturn(Arrays.asList(aPage().withName("person").build()));

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$[*].message").value("The widget cannot be deleted because it is used in 1 page, <person>"));
    }

    @Test
    public void should_not_allow_to_delete_a_custom_widget_used_in_another_widget() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().id("customLabel").build());
        when(widgetRepository.findByObjectId("customLabel")).thenReturn(Arrays.asList(aWidget().custom().name("customLabel2").build()));

        mockMvc.perform(delete("/rest/widgets/customLabel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$[*].message").value("The widget cannot be deleted because it is used in 1 widget, <customLabel2>"));
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
        List<Property> expectedProperties = Arrays.asList(property);
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
        List<Property> expectedProperties = Arrays.asList(property);
        when(widgetRepository.updateProperty("customLabel", "toBeUpdated", property)).thenReturn(expectedProperties);

        mockMvc.perform(put("/rest/widgets/customLabel/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(property)))

                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedProperties)));

        verify(widgetRepository).updateProperty("customLabel", "toBeUpdated", property);
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
        when(widgetRepository.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_when_error_appear_while_updating_property() throws Exception {
        when(widgetRepository.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(RepositoryException.class);

        mockMvc.perform(put("/rest/widgets/label/properties/toBeUpdated")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_delete_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = Arrays.asList(property);
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
                .andExpect(jsonPath("$[*].message").value("Not allowed to modify a non custom widgets"));
    }

    @Test
    public void should_respond_404_when_widget_or_property_not_found_while_deleting_property() throws Exception {
        when(widgetRepository.deleteProperty("label", "toBeDeleted")).thenThrow(new NotFoundException("Widget [ toBeDeleted ] not found"));

        mockMvc.perform(delete("/rest/widgets/label/properties/toBeDeleted")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(convertObjectToJsonBytes(aProperty().build())))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$[*].message").value("Widget [ toBeDeleted ] not found"));
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
    public void should_upload_an_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file)).andExpect(status().isCreated());

        verify(widgetAssetUploader).upload(file, widget, "js");
    }

    @Test
    public void should_not_upload_an_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetUploader.upload(file, widget, "js")).thenReturn(new ErrorMessage("error", "error"));

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file)).andExpect(status().isInternalServerError());

        verify(widgetAssetUploader).upload(file, widget, "js");
    }

    @Test
    public void should_not_upload_an_asset_for_custom_widget() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/javascript", "foo".getBytes());
        Widget widget = aWidget().id("my-widget").build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetUploader.upload(file, widget, "js")).thenReturn(new ErrorMessage("error", "error"));

        mockMvc.perform(fileUpload("/rest/widgets/my-widget/assets/js").file(file)).andExpect(status().isForbidden());

    }

    private String toJson(Object o) throws IOException {
        return new String(convertObjectToJsonBytes(o));
    }
}
