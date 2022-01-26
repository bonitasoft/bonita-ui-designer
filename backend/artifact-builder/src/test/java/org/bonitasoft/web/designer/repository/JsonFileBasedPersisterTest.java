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

import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.bonitasoft.web.designer.builder.SimpleObjectBuilder.aSimpleObjectBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class JsonFileBasedPersisterTest {

    private static final String DESIGNER_VERSION = "1.0.0";
    private static final String MODEL_VERSION = "2.0";

    private Path repoDirectory;
    private JsonHandler jsonHandler;
    private JsonFileBasedPersister<SimpleDesignerArtifact> jsonFileBasedPersister;

    @Mock
    private BeanValidator validator;
    private UiDesignerProperties uiDesignerProperties;

    @Before
    public void setUp() throws IOException {
        repoDirectory = Files.createTempDirectory("jsonrepository");
        uiDesignerProperties = new UiDesignerProperties();
        uiDesignerProperties.setVersion(DESIGNER_VERSION);
        uiDesignerProperties.setModelVersion(MODEL_VERSION);
        jsonHandler = spy(new JsonHandlerFactory().create());
        jsonFileBasedPersister = new JsonFileBasedPersister<>(jsonHandler, validator, uiDesignerProperties);
    }

    @After
    public void clean() {
        deleteQuietly(repoDirectory.toFile());
    }

    private SimpleDesignerArtifact getFromRepository(String id) throws IOException {
        byte[] json = readAllBytes(repoDirectory.resolve(id + ".json"));
        return jsonHandler.fromJson(json, SimpleDesignerArtifact.class);
    }

    @Test
    public void should_serialize_an_object_and_save_it_to_a_file() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject).isEqualTo(expectedObject);
    }

    @Test
    public void should_not_set_model_version_while_saving_if_uid_version_does_not_support_model_version() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isEqualTo(null);
    }

    @Test
    public void should_set_model_version_while_saving_if_not_already_set() throws Exception {
        uiDesignerProperties.setVersion("1.12.0");
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isEqualTo(MODEL_VERSION);

    }

    @Test
    public void should_not_set_model_version_while_saving_if_already_set() throws Exception {
        uiDesignerProperties.setVersion("1.12.0");

        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("foo", "aName", 2);
        expectedObject.setModelVersion("alreadySetModelVersion");

        jsonFileBasedPersister.save(repoDirectory, expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getModelVersion()).isEqualTo("alreadySetModelVersion");

    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_error_occurs_while_saving_a_object() throws Exception {
        doThrow(new RuntimeException()).when(jsonHandler).toJson(any(), any(Class.class));

        jsonFileBasedPersister.save(repoDirectory, new SimpleDesignerArtifact());
    }

    @Test
    public void should_validate_beans_before_saving_them() throws Exception {
        doThrow(ConstraintValidationException.class).when(validator).validate(any(Object.class));

        try {
            jsonFileBasedPersister.save(repoDirectory, new SimpleDesignerArtifact("object1", "object1", 1));
            failBecauseExceptionWasNotThrown(ConstraintValidationException.class);
        } catch (ConstraintValidationException e) {
            // should not have saved object1
            assertThat(repoDirectory.resolve("object1.json").toFile()).doesNotExist();
        }
    }

    @Test
    public void should_persist_metadata_in_a_seperate_file() throws Exception {
        SimpleDesignerArtifact artifact = aSimpleObjectBuilder()
                .id("baz")
                .metadata("foobar")
                .build();

        jsonFileBasedPersister.save(repoDirectory, artifact);

        assertThat(new String(readAllBytes(repoDirectory.getParent().resolve(".metadata/baz.json"))))
                .isEqualTo("{\"favorite\":false,\"metadata\":\"foobar\"}");
    }

    @Test
    public void should_support_parrelel_index_saves() throws Exception {
        Page page1 = PageBuilder.aPage().withUUID("baz-uuid").withName("baz").withId("baz-id").build();
        Page page2 = PageBuilder.aPage().withUUID("foo-uuid").withName("foo").withId("foo-id").build();
        JsonFileBasedPersister<Page> pageRepository = new JsonFileBasedPersister<Page>(jsonHandler, validator, uiDesignerProperties);
        Path metadataFolder = repoDirectory.resolve(".metadata");
        new Thread(() -> {
            try {
                pageRepository.saveInIndex(metadataFolder, page2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                pageRepository.saveInIndex(metadataFolder, page1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    String index = new String(readAllBytes(metadataFolder.resolve(".index.json")));
                    assertThat(index).contains("\"baz-uuid\":\"baz-id\"").contains("\"foo-uuid\":\"foo-id\"");
                });
    }

    @Test
    public void should_persist_all_artifact_id_in_index_when_refresh_indexing_is_called() throws Exception {
        List<Page> pages = new ArrayList<>();
        Page page = PageBuilder.aPage().withUUID("baz-uuid").withId("page1").build();
        Page page2 = PageBuilder.aPage().withUUID("foo-uuid").withId("page2").withName("page2").build();
        pages.add(page);
        pages.add(page2);
        JsonFileBasedPersister<Page> pageRepository = new JsonFileBasedPersister<>(jsonHandler, validator, uiDesignerProperties);
        Path metadataFolder = repoDirectory.resolve(".metadata");
        new Thread(() -> {
            try {
                pageRepository.refreshIndexing(metadataFolder, pages);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Awaitility.await()
                .atMost(3, TimeUnit.SECONDS)
                .untilAsserted(new ThrowingRunnable() {

                    @Override
                    public void run() throws Throwable {
                        String index = new String(readAllBytes(metadataFolder.resolve(".index.json")));
                        assertThat(index).contains("\"baz-uuid\":\"page1\"").contains("\"foo-uuid\":\"page2\"");
                    }
                });
    }

}
