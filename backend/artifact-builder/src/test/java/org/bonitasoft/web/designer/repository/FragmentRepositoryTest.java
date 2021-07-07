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
package org.bonitasoft.web.designer.repository;


import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import java.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.Validation;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFilledFragment;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.UiDesignerPropertiesTestBuilder.aUiDesignerProperties;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class FragmentRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    //The persister is not mocked
    private JsonFileBasedPersister<Fragment> persister;

    private JsonFileBasedLoader<Fragment> loader;

    private WorkspaceProperties workspaceProperties;

    private FragmentRepository repository;

    @Before
    public void setUp() throws Exception {
        final UiDesignerProperties uiDesignerProperties = aUiDesignerProperties(temporaryFolder.toPath());
        final BeanValidator validator = new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator());
        JsonHandler jsonHandler = new JsonHandlerFactory().create();
        persister = Mockito.spy(new JsonFileBasedPersister<>(jsonHandler, validator, uiDesignerProperties));
        loader = Mockito.spy(new JsonFileBasedLoader<>(jsonHandler, Fragment.class));
        workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getFragments().setDir(Paths.get(temporaryFolder.getRoot().getPath()));
        repository = new FragmentRepository(
                workspaceProperties,
                new WorkspaceUidProperties(),
                persister,
                loader,
                validator,
                mock(Watcher.class));
    }

    private Fragment addToRepository(Fragment fragment) throws Exception {
        //A fragment is in its own folder
        Path repo = temporaryFolder.newFolderPath(fragment.getId());
        persister.save(repo, fragment);
        return fragment;
    }

    private Fragment addToRepository(FragmentBuilder fragment) throws Exception {
        return addToRepository(fragment.build());
    }

    @Test
    public void should_get_a_fragment_from_a_json_file_repository() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        addToRepository(expectedFragment);

        Fragment fetchedFragment = repository.get(expectedFragment.getId());

        assertThat(fetchedFragment).isEqualTo(expectedFragment);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_getting_an_inexisting_fragment() throws Exception {
        repository.get("fragment-id-unknown");
    }

    @Test
    public void should_get_all_fragment_from_repository_empty() throws Exception {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    public void should_get_all_fragment_from_repository() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        addToRepository(expectedFragment);

        List<Fragment> fetchedFragments = repository.getAll();

        assertThat(fetchedFragments).containsExactly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_itself() throws Exception {
        Fragment itself = addToRepository(aFragment().withId("aFragment"));
        Fragment expectedFragment = addToRepository(aFragment().withId("anotherFragment"));

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_those_which_are_using_it() throws Exception {
        Fragment expectedFragment = addToRepository(aFragment().withId("anotherFragment").build());
        Fragment itself = addToRepository(aFragment().withId("aFragment"));
        addToRepository(aFragment().with(itself).build());

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_get_all_fragment_except_those_which_are_using_a_fragment_that_use_itself() throws Exception {
        Fragment expectedFragment = addToRepository(aFragment().withId("anotherFragment").build());
        Fragment itself = addToRepository(aFragment().withId("aFragment").build());
        Fragment container = addToRepository(aFragment().with(itself));
        Fragment container2 = addToRepository(aFragment().with(container));
        addToRepository(aFragment().with(container2));

        List<Fragment> fetchedFragments = repository.getAllNotUsingElement(itself.getId());

        assertThat(fetchedFragments).containsOnly(expectedFragment);
    }

    @Test
    public void should_save_a_fragment_in_a_json_file_repository() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");

        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
        repository.updateLastUpdateAndSave(expectedFragment);

        //A json file has to be created in the repository
        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).exists();
        Assertions.assertThat(expectedFragment.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    public void should_save_a_page_without_updating_last_update_date() throws Exception {
        Fragment fragment = repository.updateLastUpdateAndSave(aFragment().withId("customLabel").withName("theFragmentName").build());
        Instant lastUpdate = fragment.getLastUpdate();

        fragment.setName("newName");
        repository.save(fragment);

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.getLastUpdate()).isEqualTo(lastUpdate.truncatedTo(ChronoUnit.MILLIS));
        assertThat(fetchedFragment.getName()).isEqualTo("newName");
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_while_saving_a_fragment() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        doThrow(new IOException()).when(persister).save(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()), expectedFragment);

        repository.updateLastUpdateAndSave(expectedFragment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_while_saving_a_fragment_with_no_id_set() throws Exception {
        Fragment expectedFragment = aFragment().withId(null).build();

        repository.updateLastUpdateAndSave(expectedFragment);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_throw_ConstraintValidationException_while_saving_a_fragment_with_bad_name() throws Exception {
        Fragment expectedFragment = aFragment().withId("fragment-id").withName("éé&é&z").build();
        repository.updateLastUpdateAndSave(expectedFragment);
    }

    @Test
    public void should_save_all_fragment_in_a_json_file_repository() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
        repository.saveAll(Arrays.asList(expectedFragment));
        //A json file has to be created in the repository
        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).exists();
        Assertions.assertThat(expectedFragment.getLastUpdate()).isAfter(Instant.now().minus(5000, ChronoUnit.MILLIS));
    }

    @Test
    public void should_not_thrown_NPE_on_save_all_fragment_when_list_null() {
        repository.saveAll(null);
    }

    @Test
    public void should_delete_a_fragment_with_his_json_file_repository() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        addToRepository(expectedFragment);
        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).exists();
        repository.delete(expectedFragment.getId());
        assertThat(workspaceProperties.getFragments().getDir().resolve(expectedFragment.getId()).resolve(expectedFragment.getId() + ".json").toFile()).doesNotExist();
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_deleting_inexisting_fragment() throws Exception {
        repository.delete("foo");
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_on_object_included_search_list() throws Exception {
        Fragment expectedFragment = aFilledFragment("fragment-id");
        doThrow(new IOException()).when(loader).findByObjectId(workspaceProperties.getFragments().getDir(), expectedFragment.getId());
        repository.findByObjectId(expectedFragment.getId());
    }

    @Test
    public void should_mark_a_widget_as_favorite() throws Exception {
        Fragment fragment = addToRepository(aFragment().notFavorite());

        repository.markAsFavorite(fragment.getId());

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.isFavorite()).isTrue();

    }

    @Test
    public void should_unmark_a_widget_as_favorite() throws Exception {
        Fragment fragment = addToRepository(aFragment().favorite());

        repository.unmarkAsFavorite(fragment.getId());

        Fragment fetchedFragment = repository.get(fragment.getId());
        assertThat(fetchedFragment.isFavorite()).isFalse();
    }

    @Test
    public void should_keep_fragment_name_id_if_there_is_no_fragment_with_same_id() throws Exception {
        String newFragmentId = repository.getNextAvailableId("newFragmentId");

        assertThat(newFragmentId).isEqualTo("newFragmentId");
    }
}
