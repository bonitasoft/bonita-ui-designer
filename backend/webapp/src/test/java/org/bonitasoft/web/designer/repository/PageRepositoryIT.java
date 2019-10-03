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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.ApplicationConfig;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.inject.Inject;
import javax.validation.Validation;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationConfig.class, DesignerConfig.class })
@WebAppConfiguration("src/test/resources")
public class PageRepositoryIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    //The persister is not mocked
    private JsonFileBasedPersister<Page> persister;

    private JsonFileBasedLoader<Page> loader;

    private Path pagesPath;

    private PageRepository repository;

    @Inject
    private ObjectMapper objectMapper;

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
                mock(Watcher.class));
    }

    @Test
    public void should_save_variables_values_as_array_in_a_json_file_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");

        File pageFile = pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile();

        assertThat(pageFile).doesNotExist();
        repository.saveAll(Collections.singletonList(expectedPage));

        //A json file has to be created in the repository
        assertThat(pageFile).exists();

        JsonNode pageNode = objectMapper.readTree(pageFile);

        assertThat(pageNode.at("/variables/aVariable/value").isArray()).isTrue();
        assertThat(pageNode.at("/variables/aVariable/value").elements().next().asText()).isEqualTo("a value");
        assertThat(pageNode.at("/variables/aVariable").has("displayValue")).isFalse();
    }

    @Test
    public void should_not_contain_data_in_json_file_repository() throws Exception {
        Page expectedPage = aFilledPage("page-id");

        File pageFile = pagesPath.resolve(expectedPage.getId()).resolve(expectedPage.getId() + ".json").toFile();

        assertThat(pageFile).doesNotExist();
        repository.saveAll(Collections.singletonList(expectedPage));

        //A json file has to be created in the repository
        assertThat(pageFile).exists();

        JsonNode pageNode = objectMapper.readTree(pageFile);

        assertThat(pageNode.at("/data").asText()).isEqualTo("");
    }
}
