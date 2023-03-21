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
package org.bonitasoft.web.designer.service;

import org.assertj.core.api.Condition;
import org.bonitasoft.web.designer.builder.ComponentBuilder;
import org.bonitasoft.web.designer.builder.ContainerBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.TabContainerBuilder;
import org.bonitasoft.web.designer.builder.TabsContainerBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Form;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.*;

import java.time.Instant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FragmentServiceTest {

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FragmentMigrationApplyer fragmentMigrationApplyer;

    @Mock
    private FragmentIdVisitor fragmentIdVisitor;

    @Mock
    private AssetVisitor assetVisitor;

    private FragmentChangeVisitor fragmentChangeVisitor;
    private PageHasValidationErrorVisitor pageHasValidationErrorVisitor;

    @InjectMocks
    private DefaultFragmentService fragmentService;

    @Before
    public void setUp() {
        fragmentChangeVisitor = new FragmentChangeVisitor();
        pageHasValidationErrorVisitor = new PageHasValidationErrorVisitor();
        fragmentService = new DefaultFragmentService(fragmentRepository, pageRepository, fragmentMigrationApplyer,
                fragmentIdVisitor, fragmentChangeVisitor, pageHasValidationErrorVisitor, assetVisitor,
                new UiDesignerProperties("1.13.1", "2.0"),new WebResourcesVisitor(fragmentRepository, widgetRepository)
        );

        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(pageRepository.getComponentName()).thenReturn("page");
        lenient().when(fragmentRepository.updateLastUpdateAndSave(any())).thenAnswer((Answer<Fragment>) call -> {
            Fragment fragmentArg = call.getArgument(0);
            fragmentArg.setLastUpdate(Instant.now());
            return fragmentArg;
        });

    }

    @Test
    public void should_migrate_found_fragment_when_get_is_called() {
        Fragment fragment = aFragment().withId("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().withId("myFragment").withDesignerVersion("1.5.0").withPreviousDesignerVersion("1.0.0").build();
        MigrationResult<Fragment> mr = new MigrationResult(fragmentMigrated, singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "myFragmentBis")));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentMigrationApplyer.migrate(fragment, true)).thenReturn(mr);
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(fragment, true);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_not_save_fragment_when_migration_is_not_done() {
        Fragment fragment = aFragment().withId("myFragment").withModelVersion("2.0").withPreviousDesignerVersion("1.0.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, false));

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer, never()).migrate(fragment, true);
    }

    @Test
    public void should_not_save_fragment_when_migration_is_finish_in_error() {
        Fragment fragment = aFragment().withId("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().withId("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        MigrationResult mr = new MigrationResult(fragmentMigrated, singletonList(new MigrationStepReport(MigrationStatus.ERROR, "myFragmentBis")));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, true));
        when(fragmentMigrationApplyer.migrate(fragment, true)).thenReturn(mr);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(fragment, true);
        verify(fragmentRepository, never()).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_migrate_child_fragment_when_parent_fragment_is_migrate() {
        Fragment fragment = aFragment().withId("myFragmentBis").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment parentFragment = aFragment().withId("myFragment").withDesignerVersion("1.0.0").with(fragment).withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().withId("myFragmentBis").withDesignerVersion("2.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment parentFragmentMigrated = aFragment().withId("myFragment").withDesignerVersion("2.0").withPreviousDesignerVersion("1.0.0").with(fragmentMigrated).build();
        MigrationResult mr = new MigrationResult(fragmentMigrated, singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "myFragmentBis")));
        MigrationResult parentMigrated = new MigrationResult(parentFragmentMigrated, singletonList(new MigrationStepReport(MigrationStatus.SUCCESS, "myFragment")));
        when(fragmentRepository.get("myFragment")).thenReturn(parentFragment);
        lenient().when(fragmentRepository.get("myFragmentBis")).thenReturn(fragment);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, true));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(parentFragment)).thenReturn(new MigrationStatusReport(true, true));
        when(fragmentMigrationApplyer.migrate(parentFragment, true)).thenReturn(parentMigrated);
        when(fragmentMigrationApplyer.migrate(fragment, false)).thenReturn(mr);
        Set<String> h = new HashSet<>(singletonList("myFragmentBis"));
        when(fragmentRepository.getByIds(h)).thenReturn(singletonList(fragment));
        when(fragmentIdVisitor.visit(parentFragment)).thenReturn(h);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(parentFragment, true);
        verify(fragmentMigrationApplyer).migrate(fragment, false);
        verify(fragmentRepository).updateLastUpdateAndSave(parentFragmentMigrated);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_to_migrate() {
        Fragment fragment = aFragment().withId("fragment").withDesignerVersion("1.10.0").build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(singletonList("fragment"));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentRepository.getByIds(ids)).thenReturn(singletonList(fragment));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, true), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_compatible() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Fragment fragment1 = aFragment().withId("fragment1").withDesignerVersion("1.10.0").build();
        Fragment fragment2 = aFragment().withId("fragment2").withModelVersion("2.1").build(); //incompatible
        Set<String> ids = new HashSet<>(asList("fragment1", "fragment2"));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment1)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment2)).thenReturn(new MigrationStatusReport(false, false));
        when(fragmentRepository.getByIds(ids)).thenReturn(asList(fragment1, fragment2));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_fragment_contains_incompatible_fragment() {
        Fragment fragment1 = aFragment().withId("fragment1").withDesignerVersion("1.10.0").build();
        Fragment fragment2 = aFragment().withId("fragment2").withModelVersion("2.1").build(); //incompatible
        Set<String> ids = new HashSet<>(singletonList("fragment2"));
        lenient().when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment1)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment2)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentIdVisitor.visit(fragment1)).thenReturn(ids);
        when(fragmentRepository.getByIds(ids)).thenReturn(singletonList(fragment2));

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(fragment1);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_to_migrate() {
        Fragment fragment = aFragment().withId("fragment").withDesignerVersion("2.0").isMigration(false).build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(singletonList("fragment"));
        when(fragmentRepository.getByIds(ids)).thenReturn(singletonList(fragment));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true, false));
        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, false), status.toString());
    }

    private String getMigrationStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }

    @Test
    public void should_create_a_fragment() {
        // Given
        when(fragmentRepository.getAll()).thenReturn(Collections.<Fragment>emptyList());
        final String heidi = "Heidi"; // 👺
        Fragment fragment = aFragment()
                .withName(heidi)
                .withId("fragmentId")
                .build();
        when(fragmentRepository.getNextAvailableId(fragment.getName())).thenReturn(heidi);

        // When
        final Fragment savedFragment = fragmentService.create(fragment);

        // Then
        assertThat(savedFragment.getName()).isEqualTo(heidi);

        verify(fragmentRepository).updateLastUpdateAndSave(notNull());
    }

    @Test
    public void should_respond_an_error_when_fragment_name_already_exist() {
        Fragment fragment = aFragment()
                .withName("person")
                .build();
        // Given
        when(fragmentRepository.getAll()).thenReturn(singletonList(fragment));

        //When
        assertThatThrownBy(() -> fragmentService.create(fragment)).isInstanceOf(NotAllowedException.class);

    }

    private Asset aPageAsset() {
        return anAsset().withName("myJs.js").withType(AssetType.JAVASCRIPT).build();
    }

    private Asset aWidgetAsset() {
        return anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build();
    }

    @Test
    public void should_save_a_fragment() throws Exception {
        //given
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        Fragment fragment = aFragment().withId("fragment1").withName("Person").with(pageAsset, widgetAsset).withHasValidationError(false).build();
        when(fragmentRepository.get("fragment1")).thenReturn(fragment);
        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(fragment);

        //When
        fragmentService.save(fragment.getId(), fragment);

        //Then
        ArgumentCaptor<Fragment> argument = ArgumentCaptor.forClass(Fragment.class);
        verify(fragmentRepository).updateLastUpdateAndSave(argument.capture());
        assertThat(argument.getValue()).isEqualTo(fragment);
        assertThat(argument.getValue().getAssets()).containsOnly(pageAsset);

    }

    @Test()
    public void should_respond_422_on_save_when_fragment_is_incompatible() {
        //Given
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        Fragment fragment = aFragment().withId("fragment1").withName("Person").with(pageAsset, widgetAsset).build();
        when(fragmentRepository.get("fragment1")).thenReturn(fragment);
        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(false, false)).when(fragmentService).getStatus(fragment);

        //When
        assertThatThrownBy(() -> fragmentService.save(fragment.getId(), fragment)).isInstanceOf(ModelException.class);

        //Then
        verify(fragmentRepository, never()).updateLastUpdateAndSave(any());
    }


    @Test
    public void should_throw_not_allowed_when_changing_name_and_that_name_already_exist() {
        //Given
        when(fragmentRepository.getAll()).thenReturn(asList(
                aFragment().withId("fragment1").withName("Person").build(),
                aFragment().withId("fragment2").withName("Persons").build()));

        Fragment fragment = aFragment()
                .withId("fragment1")
                .withName("Persons")
                .build();

        //When
        assertThatThrownBy(() -> fragmentService.save(fragment.getId(), fragment)).isInstanceOf(NotAllowedException.class);

    }

    @Test
    public void should_throw_repo_exception_when_error_occurs_while_saving_a_page() {
        //given
        Fragment fragment = aFragment().build();
        when(fragmentRepository.get(fragment.getId())).thenReturn(fragment);
        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(fragment);
        when(fragmentRepository.updateLastUpdateAndSave(fragment)).thenThrow(new RepositoryException("exception occured", new Exception()));

        //When
        assertThatThrownBy(() -> fragmentService.save(fragment.getId(), fragment)).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_throw_not_found_when_trying_get_an_unexisting_fragment() {
        //Given
        when(fragmentRepository.get("unknownfragment")).thenThrow(NotFoundException.class);

        //When
        assertThatThrownBy(() -> fragmentService.get("unknownfragment")).isInstanceOf(NotFoundException.class);
    }


    @Test
    public void should_get_all_fragments() {
        //Given
        Fragment fragment1 = aFragment().withId("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").withName("fragment2").build();
        List<Fragment> expectedfragments = asList(fragment1, fragment2);

        when(fragmentRepository.getAll()).thenReturn(expectedfragments);
        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //when
        List<Fragment> fragments = fragmentService.getAllNotUsingFragment(null);

        //Then
        assertThat(fragments).hasSameSizeAs(expectedfragments);
        Condition<Fragment> hasStatus = new Condition<>(
                f -> f.getStatus() != null, "Status is not null"
        );
        assertThat(fragments.get(0)).is(hasStatus);
        assertThat(fragments.get(1)).is(hasStatus);

    }

    @Test
    public void should_get_all_fragment_used_elsewhere() {
        //Given
        Fragment fragment1 = aFragment().withId("fragment1").withName("fragment1").build();
        Fragment fragment2 = aFragment().withId("fragment2").withName("fragment2").build();
        Page page1 = aPage().build();
        List<Fragment> expectedfragments = asList(fragment1, fragment2);
        when(fragmentRepository.getAll()).thenReturn(expectedfragments);
        String[] ids = { "fragment1", "fragment2" };
        // fragment1 is used in page1
        when(pageRepository.findByObjectIds(asList(ids))).thenReturn(Map.of("fragment1", singletonList(page1)));
        // fragment2 is used in fragment1
        when(fragmentRepository.findByObjectIds(asList(ids))).thenReturn(Map.of("fragment2", singletonList(fragment1)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //when
        List<Fragment> fragments = fragmentService.getAllNotUsingFragment(null);

        //Then
        assertThat(fragments).hasSameSizeAs(expectedfragments);
        assertThat(fragments.stream().filter(o -> o.getName().equals("fragment1")).findFirst())
                .isPresent()
                .hasValueSatisfying(fragment ->
                        assertThat(fragment.getUsedBy().get("page").size()).isEqualTo(1));
        assertThat(fragments.stream().filter(o -> o.getName().equals("fragment2")).findFirst())
                .isPresent()
                .hasValueSatisfying(fragment ->
                        assertThat(fragment.getUsedBy().get("fragment").size()).isEqualTo(1));

    }

    @Test
    public void should_get_all_fragment_not_using_a_fragment_id() {
        //Given
        List<Fragment> expectedfragments = asList(aFragment().withId("fragment1").build(),
                aFragment().withId("fragment2").build());
        when(fragmentRepository.getAllNotUsingElement("used-fragment")).thenReturn(
                expectedfragments);

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //when
        List<Fragment> fragments = fragmentService.getAllNotUsingFragment("used-fragment");

        //Then
        assertThat(fragments).hasSameSizeAs(expectedfragments)
                .anyMatch(o -> o.getId().equals("fragment1"))
                .anyMatch(o -> o.getId().equals("fragment2"));
    }

    @Test
    public void should_get_a_fragment_by_its_id() {
        //Given
        String fragmentId = "fragment1";
        Fragment fragment1 = aFragment().withId(fragmentId).build();
        doReturn(fragment1).when(fragmentRepository).get(fragmentId);

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //When
        Fragment fragment = fragmentService.get(fragmentId);

        //Then
        assertThat(fragment.getId()).isEqualTo(fragmentId);
        assertThat(fragment.getAssets()).isEmpty();
        //instead of hasSameSizeAs(expectedAsset) because in FragmentResource, asset are set after the Get, using listAsset
    }

    @Test
    public void should_delete_a_fragment() {
        //When
        fragmentService.delete("my-fragment");

        //Then
        verify(fragmentRepository).delete("my-fragment");
    }

    @Test
    public void should_not_allow_to_delete_a_fragment_used_in_a_page() {
        //Given
        when(pageRepository.findByObjectIds(singletonList("my-fragment"))).thenReturn(Map.of("my-fragment", singletonList(aPage().withName("person").build())));

        //When
        assertThatThrownBy(() -> fragmentService.delete("my-fragment")).isInstanceOf(InUseException.class).hasMessage("The fragment cannot be deleted because it is used in 1 page <person>");

    }

    @Test
    public void should_not_allow_to_delete_a_fragment_used_in_another_fragment() {
        when(fragmentRepository.findByObjectIds(singletonList("my-fragment"))).thenReturn(Map.of("my-fragment", asList(aFragment().withName("person1").build(),aFragment().withName("person2").build())));

        //When
        assertThatThrownBy(() -> fragmentService.delete("my-fragment")).isInstanceOf(InUseException.class).hasMessage("The fragment cannot be deleted because it is used in 2 fragments <person1>, <person2>");
    }

    @Test
    public void should_return_not_found_when_get_inexisting_fragment() {
        //Given
        when(fragmentRepository.get("nonExistingFragment")).thenThrow(new NotFoundException("fragment not found"));

        //When
        assertThatThrownBy(() -> fragmentService.get("nonExistingFragment")).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_return_not_found_when_delete_inexisting_fragment() {
        doThrow(new NotFoundException("fragment not found")).when(fragmentRepository).delete("my-fragment");

        //When
        assertThatThrownBy(() -> fragmentService.delete("my-fragment")).isInstanceOf(NotFoundException.class);
    }


    @Test
    public void should_return_repo_exception_when_error_on_deletion_fragment() {
        //Given
        doThrow(new RepositoryException("error occurs", new RuntimeException())).when(fragmentRepository).delete("my-fragment");

        //When
        assertThatThrownBy(() -> fragmentService.delete("my-fragment")).isInstanceOf(RepositoryException.class);
    }


    @Test
    public void should_rename_a_fragment() throws Exception {
        //Given
        String newName = "myNewFragment";
        Fragment fragment = aFragment().withName("oldName").withId("myFragment").with(ComponentBuilder.anInput()).build();
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);

        //When
        fragmentService.rename(fragment, newName);

        //Then
        ArgumentCaptor<Fragment> argument = ArgumentCaptor.forClass(Fragment.class);

        verify(fragmentRepository).updateLastUpdateAndSave(argument.capture());
        verify(fragmentRepository).getNextAvailableId(newName);
        assertThat(argument.getValue().getName()).isEqualTo(newName);
        assertThat(argument.getValue().getId()).isEqualTo(newName);
        assertThat(argument.getValue().getRows()).isEqualTo(fragment.getRows());
        assertThat(argument.getValue().getAssets()).isEqualTo(fragment.getAssets());
    }

    @Test
    public void should_throw_NotAllowedException_when_rename_a_fragment_who_new_name_already_exist() {
        //Given
        Fragment myFragmentToRename = aFragment().withId("my-fragment").withName("oldName").build();
        when(fragmentRepository.getAll()).thenReturn(asList(
                aFragment().withId("my-fragment1").withName("newName").build(),
                myFragmentToRename));

        //When Then
        assertThatThrownBy(() -> fragmentService.rename(myFragmentToRename, "newName")).isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_mark_a_page_as_favorite() {
        //When
        fragmentService.markAsFavorite("my-fragment", true);
        //Then
        verify(fragmentRepository).markAsFavorite("my-fragment");
    }


    @Test
    public void should_unmark_a_page_as_favorite() {
        //When
        fragmentService.markAsFavorite("my-fragment", false);
        //Then
        verify(fragmentRepository).unmarkAsFavorite("my-fragment");
    }


    @Test
    public void should_save_a_fragment_renaming_it() throws Exception {
        //Given
        Fragment existingFragment = aFragment().withId("myFragment").withName("myFragment").build();
        when(fragmentRepository.get("myFragment")).thenReturn(existingFragment);
        String myNewFragmentId = "myNewFragment";
        Fragment fragmentToBeSaved = aFragment().withName(myNewFragmentId).build();
        when(fragmentRepository.getNextAvailableId(fragmentToBeSaved.getName())).thenReturn(myNewFragmentId);

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //When
        Fragment savedFragment = fragmentService.save(existingFragment.getId(), fragmentToBeSaved);

        //Then
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToBeSaved);
        assertThat(savedFragment.getId()).isEqualTo(myNewFragmentId);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_page_when_fragment_is_saving_with_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();
        when(pageRepository.get(page.getId())).thenReturn(page);

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(existingFragment);

        String newFragmentId = "myNewFragment";
        Fragment fragmentToBeSaved = aFragment().withName(newFragmentId).build();
        when(fragmentRepository.getNextAvailableId(fragmentToBeSaved.getName())).thenReturn(newFragmentId);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //When
        Fragment savedFragment = fragmentService.save(existingFragment.getId(), fragmentToBeSaved);

        //Then
        FragmentElement fragmentRefInPage = (FragmentElement) page.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(newFragmentId);
        assertThat(savedFragment.getId()).isEqualTo(newFragmentId);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_two_container_when_fragment_is_saving_with_renaming() throws Exception {
        //Given
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aContainer().with(aContainer().with(fragmentElement)))
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        when(fragmentRepository.get("aFragment")).thenReturn(existingFragment);

        String newFragmentId = "myNewFragment";
        Fragment fragmentToBeSaved = aFragment().withName(newFragmentId).build();
        when(fragmentRepository.getNextAvailableId(fragmentToBeSaved.getName())).thenReturn(newFragmentId);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //When
        Fragment savedFragment = fragmentService.save(existingFragment.getId(), fragmentToBeSaved);

        //Then
        Container firstPageContainer = (Container) page.getRows().get(0).get(0);
        Container secondPageContainer = (Container) firstPageContainer.getRows().get(0).get(0);
        FragmentElement fragmentRefInPage = (FragmentElement) secondPageContainer.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(newFragmentId);
        assertThat(savedFragment.getId()).isEqualTo(newFragmentId);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_page_when_fragment_is_renaming() throws Exception {
        //Given
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        FragmentElement fragmentRefInPage = (FragmentElement) page.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aContainer().with(fragmentElement).build())
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        Container firstPageContainer = (Container) page.getRows().get(0).get(0);
        FragmentElement fragmentRefInPage = (FragmentElement) firstPageContainer.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_form_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aFormContainer().with(aContainer().with(fragmentElement).build()))
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        FormContainer firstPageContainer = (FormContainer) page.getRows().get(0).get(0);
        FragmentElement fragmentRefInPage = (FragmentElement) firstPageContainer.getContainer().getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_modal_container_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(aModalContainer().with(aContainer().with(fragmentElement).build()))
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        ModalContainer firstPageContainer = (ModalContainer) page.getRows().get(0).get(0);
        FragmentElement fragmentRefInPage = (FragmentElement) firstPageContainer.getContainer().getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_only_good_fragment_use_in_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));

        FragmentElement myFragmentElementToKeep = new FragmentElement();
        String myFragmentToKeepId = "myFragmentToKeep";
        myFragmentElementToKeep.setId(myFragmentToKeepId);
        myFragmentElementToKeep.setDimension(Map.of("md", 8));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(ContainerBuilder.aContainer().with(fragmentElement, myFragmentElementToKeep).build())
                .build();
        when(pageRepository.get(page.getId())).thenReturn(page);
        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        Container firstPageContainer = (Container) page.getRows().get(0).get(0);
        FragmentElement fragmentRefInPage = (FragmentElement) firstPageContainer.getRows().get(0).get(0);
        FragmentElement fragmentToKeepRefInPage = (FragmentElement) firstPageContainer.getRows().get(0).get(1);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(fragmentToKeepRefInPage.getId()).isEqualTo(myFragmentToKeepId);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_use_in_tab_container_in_a_page_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        TabsContainerBuilder tabs = TabsContainerBuilder.aTabsContainer().with(TabContainerBuilder.aTabContainer().with(aContainer().with(fragmentElement).build()));
        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(tabs)
                .build();
        when(pageRepository.get(page.getId())).thenReturn(page);

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        TabsContainer tabContainer = (TabsContainer) page.getRows().get(0).get(0);
        Container container = tabContainer.getTabList().get(0).getContainer();
        FragmentElement fragmentRefInPage = (FragmentElement) container.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_form_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Form form = new Form("myForm");
        form.setId("myForm");
        form.addNewRow(fragmentElement);

        when(pageRepository.get(form.getId())).thenReturn(form);

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(form)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        FragmentElement fragmentRefInForm = (FragmentElement) form.getRows().get(0).get(0);

        assertThat(fragmentRefInForm.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(form);
    }

    @Test
    public void should_update_reference_of_fragment_in_a_layout_when_fragment_is_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("aFragment");
        fragmentElement.setDimension(Map.of("md", 8));
        Page layout = aPage()
                .withId("myLayout")
                .withName("myLayout")
                .withType("layout")
                .with(fragmentElement)
                .withVariable("aVariable", aConstantVariable().value("a value"))
                .build();

        Fragment existingFragment = aFragment().withId("aFragment").withName("aFragment").build();
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(pageRepository.get(layout.getId())).thenReturn(layout);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(layout)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        FragmentElement fragmentRefInPage = (FragmentElement) layout.getRows().get(0).get(0);

        assertThat(fragmentRefInPage.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(pageRepository).updateLastUpdateAndSave(layout);
    }

    @Test
    public void should_update_reference_of_fragment_in_an_parent_fragment_when_fragment_is_renaming() throws Exception {
        String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId(fragmentChildId);
        fragmentElement.setDimension(Map.of("md", 8));
        Fragment existingFragment = aFragment().withId(fragmentElement.getId()).withName(fragmentChildId).build();
        Fragment fragmentParent = aFragment().withId("fragmentParent").with(fragmentElement).build();

        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));

        //When
        Fragment savedFragment = fragmentService.rename(existingFragment, newName);

        //Then
        FragmentElement fragmentRefInFragment = (FragmentElement) fragmentParent.getRows().get(0).get(0);

        assertThat(fragmentRefInFragment.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentParent);
        verify(fragmentRepository).updateLastUpdateAndSave(existingFragment);
        verify(fragmentRepository).delete(fragmentChildId);

    }

    @Test
    public void should_update_reference_of_fragment_in_an_parent_fragment_when_fragment_is_saving_with_renaming() throws Exception {
        FragmentElement fragmentElement = new FragmentElement();
        String fragmentChildId = "fragmentChild";
        fragmentElement.setId(fragmentChildId);
        fragmentElement.setDimension(Map.of("md", 8));
        Fragment fragmentParent = aFragment().withId("fragmentParent").with(fragmentElement).build();
        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).build();

        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        String newName = "myNewFragment";
        when(fragmentRepository.getNextAvailableId(newName)).thenReturn(newName);
        when(fragmentRepository.get(fragmentElement.getId())).thenReturn(existingFragment);
        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        //When
        Fragment savedFragment = fragmentService.save(fragmentChildId, aFragment().withName(newName).build());

        //Then
        FragmentElement fragmentRefInFragment = (FragmentElement) fragmentParent.getRows().get(0).get(0);

        assertThat(fragmentRefInFragment.getId()).isEqualTo(savedFragment.getId()).isEqualTo(newName);
        assertThat(savedFragment.getName()).isEqualTo(newName);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentParent);
        verify(fragmentRepository).delete(fragmentChildId);
    }


    @Test
    public void should_update_to_true_parent_page_when_validation_error_status_changes() throws Exception {
        // Given
        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        page.setHasValidationError(false);
        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(false).build();

        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(page.getHasValidationError()).isTrue();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

    @Test
    public void should_update_to_true_parent_fragment_when_validation_error_status_changes() throws Exception {
        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        Fragment fragmentParent = aFragment()
                .withId("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();

        fragmentParent.setHasValidationError(false);
        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(false).build();

        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);

        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(fragmentParent.getHasValidationError()).isTrue();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

    @Test
    public void should_update_to_true_parent_page_of_parent_fragment_when_validation_error_status_changes() throws Exception {

        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        FragmentElement fragmentElementParent = new FragmentElement();
        fragmentElementParent.setHasValidationError(false);
        fragmentElementParent.setDimension(Map.of("md", 8));
        fragmentElementParent.setId("parentFragment");

        Fragment fragmentParent = aFragment()
                .withId("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();
        fragmentParent.setHasValidationError(false);

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElementParent)
                .build();
        when(pageRepository.get(page.getId())).thenReturn(page);

        page.setHasValidationError(false);

        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(pageRepository.findByObjectIds(singletonList(fragmentParent.getId()))).thenReturn(Map.of(fragmentParent.getId(), singletonList(page)));

        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(false).build();
        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(fragmentParent.getHasValidationError()).isTrue();
        assertThat(page.getHasValidationError()).isTrue();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

    @Test
    public void should_update_to_false_parent_page_when_validation_error_status_changes() throws Exception{
        // Given
        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElement)
                .build();

        page.setHasValidationError(true);
        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();

        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId("fragmentChild").withName("fragmentChild").withHasValidationError(false).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(page.getHasValidationError()).isFalse();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

    @Test
    public void should_update_to_false_parent_fragment_when_validation_error_status_changes() throws Exception{
        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(false);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        Fragment fragmentParent = aFragment()
                .withId("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();

        fragmentParent.setHasValidationError(true);
        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();

        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(false).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(fragmentParent.getHasValidationError()).isFalse();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

    @Test
    public void should_update_to_false_parent_page_of_parent_fragment_when_validation_error_status_changes() throws Exception{

        final String fragmentChildId = "fragmentChild";
        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setHasValidationError(true);
        fragmentElement.setDimension(Map.of("md", 8));
        fragmentElement.setId(fragmentChildId);

        FragmentElement fragmentElementParent = new FragmentElement();
        fragmentElementParent.setHasValidationError(true);
        fragmentElementParent.setDimension(Map.of("md", 8));
        fragmentElementParent.setId("parentFragment");

        Fragment fragmentParent = aFragment()
                .withId("parentFragment")
                .withName("parentFragment")
                .with(fragmentElement)
                .build();
        fragmentParent.setHasValidationError(true);

        Page page = aPage()
                .withId("myPage")
                .withName("myPage")
                .with(fragmentElementParent)
                .build();
        page.setHasValidationError(true);
        when(pageRepository.get(page.getId())).thenReturn(page);

        when(fragmentRepository.get(fragmentParent.getId())).thenReturn(fragmentParent);
        when(pageRepository.findByObjectIds(singletonList(fragmentParent.getId()))).thenReturn(Map.of(fragmentParent.getId(), singletonList(page)));

        Fragment existingFragment = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(true).build();
        when(fragmentRepository.get(existingFragment.getId())).thenReturn(existingFragment);
        when(fragmentRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(fragmentParent)));
        when(pageRepository.findByObjectIds(singletonList(existingFragment.getId()))).thenReturn(Map.of(existingFragment.getId(), singletonList(page)));

        fragmentService = spy(fragmentService);
        doReturn(new MigrationStatusReport(true, false)).when(fragmentService).getStatus(any());

        Fragment fragmentToSave = aFragment().withId(fragmentChildId).withName(fragmentChildId).withHasValidationError(false).build();

        // When
        fragmentService.save(fragmentChildId, fragmentToSave);

        // Then
        assertThat(fragmentParent.getHasValidationError()).isFalse();
        assertThat(page.getHasValidationError()).isFalse();
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentToSave);
    }

}
