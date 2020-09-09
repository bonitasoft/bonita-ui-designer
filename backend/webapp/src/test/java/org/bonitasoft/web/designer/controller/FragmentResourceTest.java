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

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.bonitasoft.web.designer.utils.RestControllerUtil.convertObjectToJsonBytes;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.FragmentChangeVisitor;
import org.bonitasoft.web.designer.visitor.PageHasValidationErrorVisitor;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import org.bonitasoft.web.designer.builder.ComponentBuilder;
import org.bonitasoft.web.designer.builder.ContainerBuilder;
import org.bonitasoft.web.designer.builder.FormContainerBuilder;
import org.bonitasoft.web.designer.builder.TabContainerBuilder;
import org.bonitasoft.web.designer.builder.TabsContainerBuilder;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.generator.mapping.Form;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de {@link FragmentResource}
 */
public class FragmentResourceTest {

    private MockMvc mockMvc;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private FragmentService fragmentService;

    @Mock
    private AssetVisitor assetVisitor;

    private FragmentChangeVisitor fragmentChangeVisitor = new FragmentChangeVisitor();

    private PageHasValidationErrorVisitor pageHasValidationErrorVisitor = new PageHasValidationErrorVisitor();

    @Before
    public void setUp() {
        initMocks(this);
        FragmentResource fragmentResource = new FragmentResource(fragmentRepository, fragmentService, new DesignerConfig().objectMapperWrapper(), messagingTemplate, assetVisitor, pageRepository, fragmentChangeVisitor, pageHasValidationErrorVisitor);
        fragmentResource.setUsedByRepositories(Arrays.<Repository>asList(fragmentRepository, pageRepository));
        mockMvc = UIDesignerMockMvcBuilder.mockServer(fragmentResource).build();

        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(pageRepository.getComponentName()).thenReturn("page");
    }

    @Test
    public void should_create_a_fragment() throws Exception {
        when(fragmentRepository.getAll()).thenReturn(Collections.<Fragment>emptyList());
        Fragment fragment = aFragment()
                .withName("person")
                .id("fragmentId")
                .build();
        when(fragmentRepository.getNextAvailableId("person")).thenReturn("person");
        mockMvc
                .perform(post("/rest/fragments")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(fragment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", equalTo("person")));

        verify(fragmentRepository).updateLastUpdateAndSave(notNull(Fragment.class));
    }

    @Test
    public void should_respond_an_error_when_fragment_name_already_exist() throws Exception {
        Fragment fragment = aFragment()
                .withName("person")
                .build();
        when(fragmentRepository.getAll()).thenReturn(asList(fragment));

        mockMvc
                .perform(post("/rest/fragments")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(convertObjectToJsonBytes(fragment)))
                .andExpect(status().is(403));
        when(fragmentRepository.getNextAvailableId("person")).thenReturn("person");
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

        Fragment fragment = aFragment().id("fragment1").withName("Person").with(pageAsset, widgetAsset).build();
        when(fragmentRepository.get("fragment1")).thenReturn(fragment);
        when(fragmentService.get("fragment1")).thenReturn(fragment);
        when(fragmentRepository.getNextAvailableId("Person")).thenReturn("Person");

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(fragment)))
                .andExpect(status().isOk());

        ArgumentCaptor<Fragment> argument = ArgumentCaptor.forClass(Fragment.class);
        verify(fragmentRepository).updateLastUpdateAndSave(argument.capture());
        assertThat(argument.getValue()).isEqualTo(fragment);
        assertThat(argument.getValue().getAssets()).containsOnly(pageAsset);
        verify(messagingTemplate).convertAndSend("/previewableUpdates", fragment.getId());
    }

    @Test
    public void should_respond_422_on_save_when_fragment_is_incompatible() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        Fragment fragment = aFragment().id("fragment1").withName("Person").with(pageAsset, widgetAsset).isCompatible(false).build();
        when(fragmentRepository.get("fragment1")).thenReturn(fragment);
        when(fragmentService.get("fragment1")).thenReturn(fragment);
        when(fragmentRepository.getNextAvailableId("Person")).thenReturn("Person");

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(fragment)))
                .andExpect(status().is(422));

        ArgumentCaptor<Fragment> argument = ArgumentCaptor.forClass(Fragment.class);
        verify(fragmentRepository,never()).updateLastUpdateAndSave(argument.capture());
    }

    @Test
    public void should_respond_an_error_when_changing_name_and_that_name_already_exist() throws Exception {
        when(fragmentRepository.getAll()).thenReturn(asList(
                aFragment().id("fragment1").withName("Person").build(),
                aFragment().id("fragment2").withName("Persons").build()));

        Fragment fragment = aFragment()
                .id("fragment1")
                .withName("Persons")
                .build();

        when(fragmentRepository.getNextAvailableId("Persons")).thenReturn("Persons");

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(convertObjectToJsonBytes(fragment)))
                .andExpect(status().is(403));
    }

    @Test
    public void should_respond_415_unsuported_media_type_when_trying_to_save_non_json_content() throws Exception {
        mockMvc
                .perform(put("/rest/fragments/my-fragment").content("this is not json"))
                .andExpect(status().is(415));
    }

    @Test
    public void should_respond_500_internal_error_if_error_occurs_while_saving_a_page() throws Exception {
        Fragment fragment = aFragment().build();
        when(fragmentRepository.getNextAvailableId("fragment")).thenReturn("fragment");
        doThrow(new RepositoryException("exception occured", new Exception())).when(fragmentRepository).updateLastUpdateAndSave(fragment);

        mockMvc
                .perform(
                        put("/rest/fragments/" + fragment.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).content(
                                convertObjectToJsonBytes(fragment)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void should_respond_404_when_trying_to_rename_an_unexisting_fragment() throws Exception {
        when(fragmentRepository.get("unknownfragment")).thenThrow(NotFoundException.class);
        when(fragmentService.get("unknownfragment")).thenThrow(NotFoundException.class);

        mockMvc.perform(put("/rest/fragments/unknownfragment/name").contentType(MediaType.APPLICATION_JSON_VALUE).content("newName"))

                .andExpect(status().isNotFound());
    }

    @Test
    public void should_get_all_fragments() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().id("fragment2").withName("fragment2").build();
        when(fragmentRepository.getAll()).thenReturn(asList(fragment1, fragment2));
        when(fragmentRepository.getNextAvailableId("fragment1")).thenReturn("fragment1");
        when(fragmentRepository.getNextAvailableId("fragment2")).thenReturn("fragment2");
        when(assetVisitor.visit(fragment1)).thenReturn(Sets.newHashSet(anAsset().withId("myAsset").build()));

        mockMvc.perform(get("/rest/fragments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].assets[0].id").value("myAsset"));
        ;
    }

    @Test
    public void should_get_all_fragment_light() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().id("fragment2").withName("fragment2").build();
        fragment2.setLastUpdate(Instant.parse("2015-02-02"));
        when(fragmentRepository.getAll()).thenReturn(asList(fragment1, fragment2));
        when(fragmentRepository.getNextAvailableId("fragment1")).thenReturn("fragment1");
        when(fragmentRepository.getNextAvailableId("fragment2")).thenReturn("fragment2");

        mockMvc.perform(get("/rest/fragments").param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[*].lastUpdate").value(hasItem(Instant.parse("2015-02-02").getMillis())));
    }

    @Test
    public void should_get_all_fragment_light_used_elsewhere() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().id("fragment2").withName("fragment2").build();
        when(fragmentRepository.getNextAvailableId("fragment1")).thenReturn("fragment1");
        when(fragmentRepository.getNextAvailableId("fragment2")).thenReturn("fragment2");
        Page page1 = aPage().build();
        when(fragmentRepository.getAll()).thenReturn(asList(fragment1, fragment2));
        String[] ids = {"fragment1", "fragment2"};
        // fragment1 is used in page1
        Map<String, List<Page>> map = new HashMap();
        map.put("fragment1", asList(page1));
        when(pageRepository.findByObjectIds(Arrays.asList(ids))).thenReturn(map);
        // fragment2 is used in fragment1
        Map<String, List<Fragment>> map2 = new HashMap();
        map2.put("fragment2", asList(fragment1));
        when(fragmentRepository.findByObjectIds(Arrays.asList(ids))).thenReturn(map2);

        mockMvc.perform(get("/rest/fragments").param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)))
                .andExpect(jsonPath("$[1].usedBy.fragment", hasSize(1)));
    }

    @Test
    public void should_get_all_fragment_used_elsewhere() throws Exception {
        Fragment fragment = aFragment().id("fragment").build();
        Page page = aPage().build();
        when(fragmentRepository.getAll()).thenReturn(singletonList(fragment));
        String[] ids = {"fragment"};
        Map<String, List<Page>> map = new HashMap();
        map.put("fragment", asList(page));
        when(pageRepository.findByObjectIds(Arrays.asList(ids))).thenReturn(map);
        when(fragmentRepository.getNextAvailableId("fragment")).thenReturn("fragment");

        mockMvc.perform(get("/rest/fragments"))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)));
    }

    @Test
    public void should_get_all_fragment_not_using_a_fragment_id() throws Exception {
        when(fragmentRepository.getAllNotUsingElement("used-fragment")).thenReturn(
                asList(aFragment().id("fragment1").build(),
                        aFragment().id("fragment2").build()));

        mockMvc.perform(get("/rest/fragments").param("notUsedBy", "used-fragment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")));
    }

    @Test
    public void should_get_all_fragment_not_using_a_fragment_id_in_light_view() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").build();
        Fragment fragment2 = aFragment().id("fragment2").build();
        Page page1 = aPage().build();
        when(fragmentRepository.getAllNotUsingElement("used-fragment")).thenReturn(asList(fragment1, fragment2));
        String[] ids = {"fragment1", "fragment2"};
        // fragment1 is used in page1
        Map<String, List<Page>> map = new HashMap();
        map.put("fragment1", asList(page1));
        when(pageRepository.findByObjectIds(Arrays.asList(ids))).thenReturn(map);
        // fragment2 is used in fragment1
        Map<String, List<Fragment>> map2 = new HashMap();
        map2.put("fragment2", asList(fragment1));
        when(fragmentRepository.findByObjectIds(Arrays.asList(ids))).thenReturn(map2);


        mockMvc.perform(get("/rest/fragments").param("notUsedBy", "used-fragment").param("view", "light"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id").value(hasItems("fragment1", "fragment2")))
                .andExpect(jsonPath("$[0].usedBy.page", hasSize(1)))
                .andExpect(jsonPath("$[1].usedBy.fragment", hasSize(1)));
    }

    @Test
    public void should_get_a_fragment_by_its_id() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").build();
        when(fragmentService.get("fragment1")).thenReturn(fragment1);
        when(assetVisitor.visit(fragment1)).thenReturn(Sets.newHashSet(anAsset().withId("myAsset").build()));

        mockMvc
                .perform(get("/rest/fragments/fragment1"))
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.assets[0].id").value("myAsset"));
    }

    @Test
    public void should_delete_a_fragment() throws Exception {
        mockMvc
                .perform(delete("/rest/fragments/my-fragment"))
                .andExpect(status().isOk());
    }

    @Test
    public void should_not_allow_to_delete_a_fragment_used_in_a_page() throws Exception {
        when(pageRepository.containsObject("my-fragment")).thenReturn(true);
        when(pageRepository.findByObjectId("my-fragment")).thenReturn(Arrays.asList(aPage().withName("person").build()));

        mockMvc.perform(delete("/rest/fragments/my-fragment"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message",is("The fragment cannot be deleted because it is used in 1 page, <person>")));
    }

    @Test
    public void should_not_allow_to_delete_a_fragment_used_in_another_fragment() throws Exception {
        when(fragmentRepository.containsObject("my-fragment")).thenReturn(true);
        when(fragmentRepository.findByObjectId("my-fragment")).thenReturn(Arrays.asList(aFragment().withName("person").build()));

        mockMvc.perform(delete("/rest/fragments/my-fragment"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message", is("The fragment cannot be deleted because it is used in 1 fragment, <person>")));
    }

    @Test
    public void should_respond_404_not_found_if_fragment_is_not_existing() throws Exception {
        when(fragmentService.get("nonExistingFragment")).thenThrow(new NotFoundException("fragment not found"));

        mockMvc.perform(get("/rest/fragments/nonExistingFragment")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_404_not_found_when_delete_inexisting_fragment() throws Exception {
        doThrow(new NotFoundException("fragment not found")).when(fragmentRepository).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isNotFound());
    }

    @Test
    public void should_respond_500_internal_error_when_error_on_deletion_fragment() throws Exception {
        doThrow(new RepositoryException("error occurs", new RuntimeException())).when(fragmentRepository).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isInternalServerError());
    }

    @Test
    public void should_respond_409_conflict_error_when_fragment_inuse_elsewhere() throws Exception {
        doThrow(new InUseException("fragment in use")).when(fragmentRepository).delete("my-fragment");

        mockMvc.perform(delete("/rest/fragments/my-fragment")).andExpect(status().isConflict());
    }

    @Test
    public void should_rename_a_fragment() throws Exception {
        String newName = "myNewFragment";
        Fragment fragment = aFragment().withName("oldName").id("myFragment").with(ComponentBuilder.anInput()).build();
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(fragmentRepository.get(fragment.getId())).thenReturn(fragment);
        when(fragmentService.get(fragment.getId())).thenReturn(fragment);

        mockMvc.perform(put("/rest/fragments/myFragment/name").content(newName))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/" + newName));

        ArgumentCaptor<Fragment> argument = ArgumentCaptor.forClass(Fragment.class);

        verify(fragmentRepository).updateLastUpdateAndSave(argument.capture());
        verify(fragmentRepository).getNextAvailableId(newName);
        assertThat(argument.getValue().getName()).isEqualTo(newName);
        assertThat(argument.getValue().getId()).isEqualTo(newName);
        assertThat(argument.getValue().getRows()).isEqualTo(fragment.getRows());
        assertThat(argument.getValue().getAssets()).isEqualTo(fragment.getAssets());
    }

    @Test
    public void should_throw_NotAllowedException_when_rename_a_fragment_who_new_name_already_exist() throws Exception {
        Fragment myFragmentToRename = aFragment().id("my-fragment").withName("oldName").build();
        when(fragmentRepository.getAll()).thenReturn(asList(
                aFragment().id("my-fragment1").withName("newName").build(),
                myFragmentToRename));
        when(fragmentRepository.get(myFragmentToRename.getId())).thenReturn(myFragmentToRename);
        when(fragmentService.get(myFragmentToRename.getId())).thenReturn(myFragmentToRename);

        mockMvc.perform(put("/rest/fragments/my-fragment/name").content("newName")).andExpect(status().is(403));
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/fragments/my-fragment/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("true"))
                .andExpect(status().isOk());

        verify(fragmentRepository).markAsFavorite("my-fragment");
    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {

        mockMvc
                .perform(
                        put("/rest/fragments/my-fragment/favorite").contentType(MediaType.APPLICATION_JSON_VALUE).content("false"))
                .andExpect(status().isOk());

        verify(fragmentRepository).unmarkAsFavorite("my-fragment");
    }

    @Test
    public void should_save_a_fragment_renaming_it() throws Exception {
        Fragment fragmentToBeUpdated = aFragment().id("myFragment").withName("myFragment").build();
        //Fragment1 is used in a page
        when(fragmentRepository.get("myFragment")).thenReturn(fragmentToBeUpdated);
        when(fragmentService.get("myFragment")).thenReturn(fragmentToBeUpdated);
        Fragment fragmentToBeSaved = aFragment().withName("myNewFragment").build();
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");

        mockMvc
                .perform(
                        put("/rest/fragments/myFragment").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        verify(fragmentRepository).updateLastUpdateAndSave(aFragment().id("myNewFragment").withName("myNewFragment").build());
        //verify(messagingTemplate).convertAndSend("/previewableUpdates", "myNewFragment");
        verify(messagingTemplate).convertAndSend("/previewableRemoval", "myFragment");
    }

    @Test
    public void should_update_reference_of_fragment_in_a_page_when_fragment_is_saving_with_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        Fragment fragmentToBeSaved = aFragment().id("myNewFragment").withName("myNewFragment").build();
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc
                .perform(
                        put("/rest/fragments/aFragment").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        FragmentElement fragmentExpected = (FragmentElement) page.getRows().get(0).get(0);

        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_two_container_when_fragment_is_saving_with_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aContainer().with(aContainer().with(fragmentElement)))
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        Fragment fragmentToBeSaved = aFragment().id("myNewFragment").withName("myNewFragment").build();
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc
                .perform(
                        put("/rest/fragments/aFragment").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        Container firstContainer = (Container) page.getRows().get(0).get(0);
        Container secondContainer = (Container) firstContainer.getRows().get(0).get(0);

        FragmentElement fragmentExpected = (FragmentElement) secondContainer.getRows().get(0).get(0);

        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        FragmentElement fragmentExpected = (FragmentElement) page.getRows().get(0).get(0);

        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aContainer().with(fragmentElement).build())
                .build();
        fragmentChangeVisitor.setNewFragmentId("myNewFragment");
        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        Container container = (Container) page.getRows().get(0).get(0);
        FragmentElement fragmentExpected = (FragmentElement) container.getRows().get(0).get(0);
        //assertThat(page.getRows().get(0).get)).isEqualTo("myNewFragment");
        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_form_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(FormContainerBuilder.aFormContainer().with(aContainer().with(fragmentElement).build()))
                .build();

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        FormContainer formContainer = (FormContainer) page.getRows().get(0).get(0);
        Container container = formContainer.getContainer();
        FragmentElement fragmentExpected = (FragmentElement) container.getRows().get(0).get(0);
        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_modal_container_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aModalContainer().with(aContainer().with(fragmentElement).build()))
                .build();
        fragmentChangeVisitor.setNewFragmentId("myNewFragment");
        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        ModalContainer modalContainer = (ModalContainer) page.getRows().get(0).get(0);
        FragmentElement fragmentExpected = (FragmentElement) modalContainer.getContainer().getRows().get(0).get(0);
        //assertThat(page.getRows().get(0).get)).isEqualTo("myNewFragment");
        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }


    @Test
    public void should_update_reference_of_only_good_fragment_use_in_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));

        FragmentElement myFragmentElementToKeep = new FragmentElement();
        myFragmentElementToKeep.setId("myFragmentToKeep");
        myFragmentElementToKeep.setDimension(ImmutableSortedMap.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(ContainerBuilder.aContainer().with(fragmentElement, myFragmentElementToKeep).build())
                .build();

        Fragment myFragmentToKeep = aFragment().id("myFragmentToKeep").withName("myFragmentToKeep").build();
        when(fragmentRepository.get("myFragmentToKeep")).thenReturn(myFragmentToKeep);
        when(fragmentService.get("myFragmentToKeep")).thenReturn(myFragmentToKeep);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(myFragmentToKeep.getId())).thenReturn(asList(page));

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        Container container = (Container) page.getRows().get(0).get(0);
        FragmentElement fragmentExpectedNoChange = (FragmentElement) container.getRows().get(0).get(1);
        FragmentElement fragmentExpected = (FragmentElement) container.getRows().get(0).get(0);
        //assertThat(page.getRows().get(0).get)).isEqualTo("myNewFragment");
        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        assertThat(fragmentExpectedNoChange.getId()).isEqualTo("myFragmentToKeep");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_tab_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        TabsContainerBuilder tabs = TabsContainerBuilder.aTabsContainer().with(TabContainerBuilder.aTabContainer().with(aContainer().with(fragmentElement).build()));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(tabs)
                .build();
        fragmentChangeVisitor.setNewFragmentId("myNewFragment");
        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));
        TabsContainer tabContainer = (TabsContainer) page.getRows().get(0).get(0);
        Container container = tabContainer.getTabList().get(0).getContainer();
        FragmentElement fragmentExpected = (FragmentElement) container.getRows().get(0).get(0);
        //assertThat(page.getRows().get(0).get)).isEqualTo("myNewFragment");
        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }


    @Test
    public void should_update_reference_of_fragment_in_a_form_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Form form = new Form("myForm");
        form.setId("myForm");
        form.addNewRow(fragmentElement);


        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(form.getId())).thenReturn(form);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(form));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        FragmentElement fragmentExpected = (FragmentElement) form.getRows().get(0).get(0);

        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(form);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_layout_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Page layout = aPage()
                .withId("myLayout")
                .withName("myLayout")
                .withType("layout")
                .with(fragmentElement)
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();

        Fragment fragmentToUpdated = aFragment().id("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("aFragment")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("myNewFragment")).thenReturn("myNewFragment");
        when(pageRepository.get(layout.getId())).thenReturn(layout);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(layout));

        mockMvc.perform(put("/rest/fragments/aFragment/name").content("myNewFragment"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/myNewFragment"));

        FragmentElement fragmentExpected = (FragmentElement) layout.getRows().get(0).get(0);

        assertThat(fragmentExpected.getId()).isEqualTo("myNewFragment");
        verify(pageRepository).updateLastUpdateAndSave(layout);
    }

    @Test
    public void should_update_reference_of_fragment_in_an_parent_fragment_when_fragment_is_saving_with_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("fragmentChild");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Fragment fragmentParent = aFragment().id("fragmentParent").with(fragmentElement).build();
        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").build();
        when(fragmentRepository.get("fragmentParent")).thenReturn(fragmentParent);
        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentParent")).thenReturn(fragmentParent);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(fragmentParent));
        Fragment fragmentToBeSaved = aFragment().id("fragmentChildRenaming").withName("fragmentChildRenaming").build();
        when(fragmentRepository.getNextAvailableId("fragmentChildRenaming")).thenReturn("fragmentChildRenaming");

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToBeSaved)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/fragmentChildRenaming"));

        FragmentElement fragmentExpected = (FragmentElement) fragmentParent.getRows().get(0).get(0);
        assertThat(fragmentExpected.getId()).isEqualTo("fragmentChildRenaming");
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentParent);
    }

    @Test
    public void should_update_reference_of_fragment_in_an_parent_fragment_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("fragmentChild");
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        Fragment fragmentParent = aFragment().id("fragmentParent").with(fragmentElement).build();
        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").build();
        when(fragmentRepository.get("fragmentParent")).thenReturn(fragmentParent);
        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentParent")).thenReturn(fragmentParent);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(fragmentParent));
        when(fragmentRepository.getNextAvailableId("fragmentChildRenaming")).thenReturn("fragmentChildRenaming");

        mockMvc.perform(put("/rest/fragments/fragmentChild/name").content("fragmentChildRenaming"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/fragments/fragmentChildRenaming"));

        FragmentElement fragmentExpected = (FragmentElement) fragmentParent.getRows().get(0).get(0);
        assertThat(fragmentExpected.getId()).isEqualTo("fragmentChildRenaming");
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentParent);
    }

    @Test
    public void should_update_to_true_parent_page_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        page.setHasValidationError(false);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(page.getHasValidationError()).isTrue();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_update_to_true_parent_fragment_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        Fragment fragmentParent = aFragment()
                .id("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();

        fragmentParent.setHasValidationError(false);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(fragmentParent));

        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(fragmentParent.getHasValidationError()).isTrue();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_update_to_true_parent_page_of_parent_fragment_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        FragmentElement fragmentElementParent = new FragmentElement();
        fragmentElementParent.setHasValidationError(false);
        fragmentElementParent.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElementParent.setId("parentFragment");

        Fragment fragmentParent = aFragment()
                .id("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElementParent)
                .build();

        page.setHasValidationError(false);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(fragmentRepository.get("parentFragment")).thenReturn(fragmentParent);
        when(fragmentService.get("parentFragment")).thenReturn(fragmentParent);
        when(fragmentRepository.getNextAvailableId("parentFragment")).thenReturn("parentFragment");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(page.getHasValidationError()).isTrue();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_update_to_false_parent_page_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(true);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        page.setHasValidationError(true);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(page));

        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(page.getHasValidationError()).isFalse();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_update_to_false_parent_fragment_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(true);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        Fragment fragmentParent = aFragment()
                .id("fragmentParent")
                .withName("fragmentParent")
                .with(fragmentElement)
                .build();

        fragmentParent.setHasValidationError(true);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(fragmentRepository.get("fragmentParent")).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectId("fragmentChild")).thenReturn(asList(fragmentParent));

        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(fragmentParent.getHasValidationError()).isFalse();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_update_to_false_parent_page_of_parent_fragment_when_validation_error_status_changes() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(true);
        fragmentElement.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElement.setId("fragmentChild");

        FragmentElement fragmentElementParent = new FragmentElement();
        fragmentElementParent.setHasValidationError(true);
        fragmentElementParent.setDimension(ImmutableSortedMap.of("md", 8));
        fragmentElementParent.setId("parentFragment");

        Fragment fragmentParent = aFragment()
                .id("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .withHasValidationError(true)
                .build();

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElementParent)
                .build();

        page.setHasValidationError(true);

        Fragment fragmentToUpdated = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(true).build();
        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        when(fragmentRepository.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToUpdated);
        when(fragmentRepository.getNextAvailableId("fragmentChild")).thenReturn("fragmentChild");
        when(fragmentRepository.get("parentFragment")).thenReturn(fragmentParent);
        when(fragmentService.get("parentFragment")).thenReturn(fragmentParent);
        when(fragmentRepository.getNextAvailableId("parentFragment")).thenReturn("parentFragment");
        when(fragmentRepository.findByObjectId(fragmentToUpdated.getId())).thenReturn(asList(fragmentParent));
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectId(fragmentParent.getId())).thenReturn(asList(page));

        mockMvc
                .perform(
                        put("/rest/fragments/fragmentChild").contentType(MediaType.APPLICATION_JSON_VALUE).content(convertObjectToJsonBytes(fragmentToSave)))
                .andExpect(status().isOk());

        assertThat(fragmentParent.getHasValidationError()).isFalse();
        assertThat(page.getHasValidationError()).isFalse();

        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToUpdated);
    }

    @Test
    public void should_respond_422_when_custom_widget_is_incompatible() throws Exception {
        Fragment fragmentToSave = aFragment().id("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();
        fragmentToSave.setStatus(new MigrationStatusReport(false, true));
        when(fragmentService.get("fragmentChild")).thenReturn(fragmentToSave);

        mockMvc.perform(get("/rest/fragments/fragmentChild")).andExpect(status().is(422));
    }
}
