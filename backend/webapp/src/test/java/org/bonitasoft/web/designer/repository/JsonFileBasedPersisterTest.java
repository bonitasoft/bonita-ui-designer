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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class JsonFileBasedPersisterTest {

    private static final String DESIGNER_VERSION = "2.0.0";

    private Path repoDirectory;
    private JacksonObjectMapper objectMapper;
    private JsonFileBasedPersister<SimpleDesignerArtifact> repository;

    @Mock
    private BeanValidator validator;

    @Before
    public void setUp() throws IOException {
        repoDirectory = Files.createTempDirectory("jsonrepository");
        objectMapper = spy(new DesignerConfig().objectMapperWrapper());
        repository = new JsonFileBasedPersister<>(objectMapper, validator);

        ReflectionTestUtils.setField(repository, "version", DESIGNER_VERSION);
    }

    @After
    public void clean() {
        deleteQuietly(repoDirectory.toFile());
    }

    private SimpleDesignerArtifact getFromRepository(String id) throws IOException {
        byte[] json = readAllBytes(repoDirectory.resolve(id + ".json"));
        return objectMapper.fromJson(json, SimpleDesignerArtifact.class);
    }

    private void addToRepository(String id, SimpleDesignerArtifact o) throws IOException {
        byte[] json = objectMapper.toJson(o);
        write(repoDirectory.resolve(id + ".json"), json);
    }

    @Test
    public void should_serialize_an_object_and_save_it_to_a_file() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("id", "aName", 2);

        repository.save(repoDirectory, "foo", expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject).isEqualTo(expectedObject);
    }

    @Test
    public void should_set_designer_version_while_saving_if_not_already_set() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("id", "aName", 2);

        repository.save(repoDirectory, "foo", expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getDesignerVersion()).isEqualTo(DESIGNER_VERSION);
    }

    @Test
    public void should_not_set_designer_version_while_saving_if_already_set() throws Exception {
        SimpleDesignerArtifact expectedObject = new SimpleDesignerArtifact("id", "aName", 2);
        expectedObject.setDesignerVersion("alreadySetVerion");

        repository.save(repoDirectory, "foo", expectedObject);

        SimpleDesignerArtifact savedObject = getFromRepository("foo");
        assertThat(savedObject.getDesignerVersion()).isEqualTo("alreadySetVerion");
    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_error_occurs_while_saving_a_object() throws Exception {
        doThrow(new RuntimeException()).when(objectMapper).toJson(anyObject(), any(Class.class));

        repository.save(repoDirectory, "foo", new SimpleDesignerArtifact());
    }

    @Test
    public void should_validate_beans_before_saving_them() throws Exception {
        doThrow(ConstraintValidationException.class).when(validator).validate(any(Object.class));

        try {
            repository.save(repoDirectory, "object1", new SimpleDesignerArtifact("object1", "object1", 1));
            failBecauseExceptionWasNotThrown(ConstraintValidationException.class);
        } catch (ConstraintValidationException e) {
            // should not have saved object1
            assertThat(repoDirectory.resolve("object1.json").toFile()).doesNotExist();
        }
    }
}
