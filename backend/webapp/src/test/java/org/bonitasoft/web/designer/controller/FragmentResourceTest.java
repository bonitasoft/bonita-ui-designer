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

import org.bonitasoft.web.designer.builder.ComponentBuilder;
import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.service.DefaultFragmentService;
import org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de {@link FragmentResource}
 */
@ExtendWith(MockitoExtension.class)
public class FragmentResourceTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private MockMvc mockMvc;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private DefaultFragmentService fragmentService;


    @BeforeEach
    public void setUp() {
        JsonHandler jsonHandler = new JsonHandlerFactory().create();
        FragmentResource fragmentResource = new FragmentResource(fragmentService,jsonHandler, messagingTemplate);
        mockMvc = UIDesignerMockMvcBuilder.mockServer(fragmentResource).build();
    }

    @Test
    public void should_create_a_fragment() throws Exception {
        Fragment fragment = aFragment()
                .withName("person")
                .withId("fragmentId")
                .build();
        when(fragmentService.create(fragment)).thenReturn(fragment);

        mockMvc
                .perform(post("/rest/fragments")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(fragment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("person")));
    }

    @Test
    public void should_respond_an_error_when_fragment_name_already_exist() throws Exception {
        Fragment fragment = aFragment()
                .withName("person")
                .build();
        when(fragmentService.create(fragment)).thenThrow(NotAllowedException.class);

        mockMvc
                .perform(post("/rest/fragments")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(fragment)))
                .andExpect(status().is(403));

    }

    private Asset aPageAsset() {
        return anAsset().withName("myJs.js").withType(AssetType.JAVASCRIPT).build();
    }

    private Asset aWidgetAsset() {
        return anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build();
    }

    @Test
    public void should_save_a_fragment() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        Fragment fragment = aFragment().withId("fragment1").withName("Person").with(pageAsset, widgetAsset).build();

        when(fragmentService.save(fragment.getId(), fragment)).thenReturn(fragment);

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                jsonHandler.toJson(fragment)))
                .andExpect(status().isOk());

        verify(fragmentService).save(fragment.getId(), fragment);
        verify(messagingTemplate).convertAndSend("/previewableUpdates", fragment.getId());
    }

    @Test
    public void should_respond_422_on_save_when_fragment_is_incompatible() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        Fragment fragment = aFragment().
                withId("fragment1").withName("Person").with(pageAsset, widgetAsset).isCompatible(false).build();
        when(fragmentService.save(fragment.getId(), fragment)).thenThrow(ModelException.class);

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                jsonHandler.toJson(fragment)))
                .andExpect(status().is(422));
    }

    @Test
    public void should_respond_an_error_when_changing_name_and_that_name_already_exist() throws Exception {

        Fragment fragment = aFragment()
                .withId("fragment1")
                .withName("Persons")
                .build();

        when(fragmentService.save(fragment.getId(), fragment)).thenThrow(NotAllowedException.class);

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(jsonHandler.toJson(fragment)))
                .andExpect(status().is(403));
    }

    @Test
    public void should_respond_415_unsuported_media_type_when_trying_to_save_non_json_content() throws Exception {
        mockMvc
                .perform(put("/rest/fragments/my-fragment").content("this is not json"))
                .andExpect(status().is(415));
    }

    @Test
    public void should_respond_500_internal_error_if_error_occurs_while_saving_a_fragment() throws Exception {
        Fragment fragment = aFragment().build();
        when(fragmentService.save(fragment.getId(), fragment)).thenThrow(RuntimeException.class);
        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                jsonHandler.toJson(fragment)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_respond_404_when_trying_to_rename_an_unexisting_fragment() throws Exception {
        when(fragmentService.get("unknownfragment")).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/rest/fragments/unknownfragment/name").contentType(MediaType.APPLICATION_JSON_VALUE).content("newName"))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_get_all_fragments() throws Exception {
        Fragment fragment1 = aFragment().withId("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").withName("fragment2").build();

        when(fragmentService.getAllNotUsingFragment(null)).thenReturn(asList(fragment1, fragment2));
        when(fragmentService.listAsset(fragment1)).thenReturn(Set.of(anAsset().withId("myAsset").build()));

        mockMvc.perform(get("/rest/fragments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].assets[0].id").value("myAsset"));
        ;
    }

    @Test
    public void should_get_all_fragment_light() throws Exception {
        Fragment fragment1 = aFragment().withId("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").withName("fragment2").build();
        var lastUpdate = Instant.parse("2015-02-02T00:00:00.000Z");
        fragment2.setLastUpdate(lastUpdate);
        when(fragmentService.getAllNotUsingFragment(null)).thenReturn(asList(fragment1, fragment2));

        mockMvc.perform(get("/rest/fragments").param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[*].lastUpdate").value(hasItem(lastUpdate.toEpochMilli())));
    }

    @Test
    public void should_get_all_fragment_light_used_elsewhere() throws Exception {
        Fragment fragment1 = aFragment().withId("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").withName("fragment2").build();
        Page page1 = aPage().build();

        fragment1.addUsedBy("page",singletonList(page1));
        fragment2.addUsedBy("fragment",singletonList(fragment1));

        when(fragmentService.getAllNotUsingFragment(null)).thenReturn(asList(fragment1,fragment2));

        mockMvc.perform(get("/rest/fragments").param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)))
                .andExpect(jsonPath("$[1].usedBy.fragment", hasSize(1)));
    }

    @Test
    public void should_get_all_fragment_used_elsewhere() throws Exception {
        Fragment fragment = aFragment().withId("fragment").build();
        Page page = aPage().build();
        fragment.addUsedBy("page",singletonList(page));

        when(fragmentService.getAllNotUsingFragment(null)).thenReturn(singletonList(fragment));

        mockMvc.perform(get("/rest/fragments"))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)));
    }

    @Test
    public void should_get_all_fragment_not_using_a_fragment_id() throws Exception {
        String fragmentId = "used-fragment";
        when(fragmentService.getAllNotUsingFragment(fragmentId)).thenReturn(
                asList(aFragment().withId("fragment1").build(),
                        aFragment().withId("fragment2").build()));

        mockMvc.perform(get("/rest/fragments").param("notUsedBy", fragmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")));
    }

    @Test
    public void should_get_all_fragment_not_using_a_fragment_id_in_light_view() throws Exception {
        Fragment fragment1 = aFragment().withId("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").build();
        Page page = aPage().build();
        String fragmentId = "used-fragment";

        fragment1.addUsedBy("page",singletonList(page));
        fragment2.addUsedBy("fragment",singletonList(fragment1));
        when(fragmentService.getAllNotUsingFragment(fragmentId)).thenReturn(
                asList(fragment1,fragment2));

        mockMvc.perform(get("/rest/fragments").param("notUsedBy", fragmentId).param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)))
                .andExpect(jsonPath("$[0].assets").doesNotExist())
                .andExpect(jsonPath("$[1].usedBy.fragment", hasSize(1)))
                .andExpect(jsonPath("$[1].assets").doesNotExist());
    }

    @Test
    public void should_get_a_fragment_by_its_id() throws Exception {
        Fragment fragment1 = aFragment().withId("fragment1").build();

        when(fragmentService.get(fragment1.getId())).thenReturn(fragment1);
        when(fragmentService.listAsset(fragment1)).thenReturn(Set.of(anAsset().withId("myAsset").build()));

        mockMvc
                .perform(get("/rest/fragments/fragment1"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.assets[0].id").value("myAsset"));
    }

    @Test
    public void should_delete_a_fragment() throws Exception {
        String fragmentId = "my-fragment";
        mockMvc
                .perform(delete("/rest/fragments/" + fragmentId))
                .andExpect(status().isOk());

        verify(fragmentService).delete(fragmentId);
    }

    @Test
    public void should_not_allow_to_delete_a_fragment_used_in_a_page() throws Exception {
        String fragmentId = "my-fragment";
        String errorMessage = "The fragment cannot be deleted because it is used in 1 page, <person>";
        doThrow(new InUseException(errorMessage)).when(fragmentService).delete(fragmentId);

        mockMvc.perform(delete("/rest/fragments/" + fragmentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message", is(errorMessage)));
    }

    @Test
    public void should_respond_404_not_found_if_fragment_is_not_existing() throws Exception {
        when(fragmentService.get("nonExistingFragment")).thenThrow(new NotFoundException("fragment not found"));

        mockMvc.perform(get("/rest/fragments/nonExistingFragment")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_404_not_found_when_delete_non_existing_fragment() throws Exception {
        doThrow(new NotFoundException("fragment not found")).when(fragmentService).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_internal_error_when_error_on_deletion_fragment() throws Exception {
        doThrow(new RepositoryException("error occurs", new RuntimeException())).when(fragmentService).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isInternalServerError());
    }

    @Test
    public void should_respond_409_conflict_error_when_fragment_inuse_elsewhere() throws Exception {
        doThrow(new InUseException("fragment in use")).when(fragmentService).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isConflict());
    }

    @Test
    public void should_rename_a_fragment() throws Exception {
        String newName = "myNewFragment";
        Fragment fragment = aFragment().withName("oldName").withId("myFragment").with(ComponentBuilder.anInput()).build();
        when(fragmentService.get(fragment.getId())).thenReturn(fragment);
        when(fragmentService.rename(fragment, newName)).thenAnswer(new Answer<Fragment>() {
            @Override
            public Fragment answer(InvocationOnMock invocationOnMock) throws Throwable {
                Fragment fragment = invocationOnMock.getArgument(0);
                String newName = invocationOnMock.getArgument(1);
                fragment.setId(newName);
                fragment.setName(newName);
                return fragment;
            }
        });

        mockMvc.perform(put("/rest/fragments/myFragment/name").content(newName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/" + newName));
    }

    @Test
    public void should_throw_NotAllowedException_when_rename_a_fragment_who_new_name_already_exist() throws Exception {
        Fragment myFragmentToRename = aFragment().withId("my-fragment").withName("oldName").build();
        when(fragmentService.get(myFragmentToRename.getId())).thenReturn(myFragmentToRename);
        String newName = "newName";
        when(fragmentService.rename(myFragmentToRename, newName)).thenThrow(NotAllowedException.class);

        mockMvc.perform(put("/rest/fragments/my-fragment/name").content(newName)).andExpect(status().is(403));
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/fragments/my-fragment/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(fragmentService).markAsFavorite("my-fragment", true);
    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/fragments/my-fragment/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(fragmentService).markAsFavorite("my-fragment", false);
    }

    @Test
    public void should_save_a_fragment_renaming_it() throws Exception {
        String fragmentId = "myFragment";
        Fragment fragmentToBeSaved = aFragment().withId("myNewFragment").withName("myNewFragment").build();
        when(fragmentService.save(fragmentId, fragmentToBeSaved)).thenReturn(fragmentToBeSaved);

        mockMvc
                .perform(
                        put("/rest/fragments/myFragment").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonHandler.toJson(fragmentToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        verify(messagingTemplate).convertAndSend("/previewableRemoval", fragmentId);
    }

    @Test
    public void should_respond_422_when_custom_widget_is_incompatible() throws Exception {
        Fragment fragmentToSave = aFragment().withId("fragmentChild")
                .withName("fragmentChild").withHasValidationError(false).isCompatible(false).build();
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToSave);

        mockMvc.perform(get("/rest/fragments/fragmentChild")).andExpect(status().is(422));
    }
}
