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

import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import javax.validation.Validation;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.LiveMigration;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private LiveMigration<Page> liveMigration;

    //The persister is not mocked
    private JsonFileBasedPersister<Page> persister;

    private JsonFileBasedLoader<Page> loader;

    private Path pagesPath;

    private PageRepository repository;

    @Before
    public void setUp() throws Exception {
        pagesPath = Paths.get(temporaryFolder.getRoot().getPath());
        persister = spy(new DesignerConfig().pageFileBasedPersister());
        loader = spy(new DesignerConfig().pageFileBasedLoader());

        repository = new PageRepository(
                pagesPath,
                persister,
                loader,
                new BeanValidator(Validation.buildDefaultValidatorFactory().getValidator()),
                new Watcher());
    }

    private Page addToRepository(PageBuilder page) throws Exception {
        return addToRepository(page.build());
    }

    private Page addToRepository(Page page) throws Exception {
        Path repo = temporaryFolder.newFolderPath(page.getId());
        persister.save(repo, page.getId(), page);
        return page;
    }

    @Test
    public void should_get_a_page_from_a_json_file_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        addToRepository(expectedPage);

        Page fetchedPage = repository.get(expectedPage.getId());

        assertThat(fetchedPage).isEqualTo(expectedPage);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_getting_an_inexisting_page() throws Exception {
        repository.get("page-id-unknown");
    }

    @Test
    public void should_get_all_page_from_repository_empty() throws Exception {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    public void should_get_all_page_from_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        addToRepository(expectedPage);

        List<Page> fetchedPages = repository.getAll();

        assertThat(fetchedPages).containsExactly(expectedPage);
    }

    @Test
    public void should_save_a_page_in_a_json_file_repository() throws Exception {
        Page page = aFilledPage("page-id");
        assertThat(pagesPath.resolve(page.getId()).resolve(page.getId() + ".json").toFile()).doesNotExist();

        repository.updateLastUpdateAndSave(page);

        //A json file has to be created in the repository
        assertThat(pagesPath.resolve(page.getId()).resolve(page.getId() + ".json").toFile()).exists();
        assertThat(page.getLastUpdate()).isGreaterThan(Instant.now().minus(5000));
        assertThat(exists(Paths.get(repository.resolvePath(page.getId()).toString(), "assets", "css", "style.css"))).isTrue();
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_while_saving_a_page() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        doThrow(new IOException()).when(persister).save(pagesPath.resolve(expectedPage.getId()), expectedPage.getId(), expectedPage);

        repository.updateLastUpdateAndSave(expectedPage);
    }

    @Test
    public void should_save_a_page_without_updating_last_update_date() throws Exception {
        Page page = repository.updateLastUpdateAndSave(aPage().withId("page-id").withName("thePageName").build());
        Instant lastUpdate = page.getLastUpdate();

        page.setName("newName");
        repository.save(page);

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.getLastUpdate()).isEqualTo(lastUpdate);
        assertThat(fetchedPage.getName()).isEqualTo("newName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_while_saving_a_page_with_no_id_set() throws Exception {
        Page expectedPage = aPage().withId(null).build();

        repository.updateLastUpdateAndSave(expectedPage);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_throw_ConstraintValidationException_while_saving_a_page_with_bad_name() throws Exception {
        Page expectedPage = aPage().withId("page-id").withName("éé&é&z").build();

        repository.updateLastUpdateAndSave(expectedPage);
    }

    @Test
    public void should_save_all_page_in_a_json_file_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");

        assertThat(pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).doesNotExist();
        repository.saveAll(Collections.singletonList(expectedPage));

        //A json file has to be created in the repository
        assertThat(pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).exists();
        assertThat(expectedPage.getLastUpdate()).isGreaterThan(Instant.now().minus(5000));
    }

    @Test
    public void should_delete_a_page_with_his_json_file_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        addToRepository(expectedPage);

        assertThat(pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).exists();
        repository.delete(expectedPage.getId());
        assertThat(pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile()).doesNotExist();
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_deleting_inexisting_page() throws Exception {
        repository.delete("foo");
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_on_object_included_search() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        doThrow(new IOException()).when(loader).contains(pagesPath, expectedPage.getId());

        repository.containsObject(expectedPage.getId());
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_on_object_included_search_list() throws Exception {
        Page expectedPage = aFilledPage("page-id");
        doThrow(new IOException()).when(loader).findByObjectId(pagesPath, expectedPage.getId());

        repository.findByObjectId(expectedPage.getId());
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {
        Page page = addToRepository(aPage().notFavorite());

        repository.markAsFavorite(page.getId());

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.isFavorite()).isTrue();

    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {
        Page page = addToRepository(aPage().favorite());

        repository.unmarkAsFavorite(page.getId());

        Page fetchedPage = repository.get(page.getId());
        assertThat(fetchedPage.isFavorite()).isFalse();
    }

}
