/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.repository;

import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.SimpleObjectBuilder.aFilledSimpleObject;
import static org.bonitasoft.web.designer.controller.importer.exception.ImportExceptionMatcher.hasType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

import org.bonitasoft.web.designer.builder.SimpleObjectBuilder;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.controller.exception.ImportException;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.SimpleObject;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonFileBasedLoaderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path repoDirectory;
    private JacksonObjectMapper objectMapper;
    private JsonFileBasedLoader<SimpleObject> loader;

    @Mock
    private BeanValidator validator;

    @Before
    public void setUp() throws IOException {
        repoDirectory = temporaryFolder.newFolderPath("jsonrepository");
        objectMapper = spy(new DesignerConfig().objectMapperWrapper());
        loader = new JsonFileBasedLoader<>(objectMapper, SimpleObject.class);
    }

    private void addToRepository(SimpleObject... pages) throws Exception {
        for (SimpleObject page : pages) {
            //A page is in its own folder
            Path repo = temporaryFolder.newFolderPath("jsonrepository", page.getId());
            write(repo.resolve(page.getId() + ".json"), objectMapper.toJson(page));
        }
    }

    @Test
    public void should_get_an_object_from_a_json_file_and_deserialize_it() throws Exception {
        SimpleObject expectedObject = aFilledSimpleObject("id");
        addToRepository(expectedObject);

        assertThat(loader.get(repoDirectory, "id")).isEqualTo(expectedObject);
    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_error_occurs_while_getting_a_object() throws Exception {
        addToRepository(aFilledSimpleObject("id"));
        doThrow(new IOException()).when(objectMapper).fromJson(any(byte[].class), eq(SimpleObject.class));

        loader.get(repoDirectory, "foobar");
    }

    @Test(expected = NoSuchFileException.class)
    public void should_throw_NosuchFileException_when_while_getting_an_unexisting_object() throws Exception {
        loader.get(repoDirectory, "unexisting");
    }

    @Test
    public void should_get_all_objects_from_json_files_and_deserialize_them() throws Exception {
        SimpleObject object1 = aFilledSimpleObject("objet1");
        SimpleObject object2 = aFilledSimpleObject("objet2");
        addToRepository(object1, object2);

        List<SimpleObject> objects = loader.getAll(repoDirectory);
        assertThat(objects).containsOnly(object1, object2);
    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_error_occurs_while_getting_all_object() throws Exception {
        addToRepository(aFilledSimpleObject("objet1"));
        doThrow(new IOException()).when(objectMapper).fromJson(any(byte[].class), eq(SimpleObject.class));

        loader.getAll(repoDirectory);
    }

    @Test
    public void should_find_a_byte_array_in_an_another() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "exem".getBytes())).isEqualTo(4);
    }

    @Test
    public void should_find_a_byte_array_in_an_another_on_start_position() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "mon ex".getBytes())).isEqualTo(0);
    }

    @Test
    public void should_not_find_a_byte_array_in_an_another() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), "rex".getBytes())).isEqualTo(-1);
    }

    @Test
    public void should_not_find_null_in_byte_array() {
        assertThat(loader.indexOf("mon exemple complet".getBytes(), null)).isEqualTo(-1);
    }

    @Test
    public void should_not_find_occurence_in_byte_array_null() {
        assertThat(loader.indexOf(null, "search".getBytes())).isEqualTo(-1);
    }

    @Test
    public void should_find_one_object_included_in_another_and_deserialize_it() throws Exception {
        SimpleObject object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleObject object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1).build();

        addToRepository(object1, object2);

        List<SimpleObject> objects = loader.findByObjectId(repoDirectory, "objet1");

        assertThat(objects).containsOnly(object2);
    }

    @Test
    public void should_not_find_object_included_in_another() throws Exception {
        SimpleObject object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleObject object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1).build();

        addToRepository(object1, object2);

        List<SimpleObject> objects = loader.findByObjectId(repoDirectory, "objet2");
        assertThat(objects).isEmpty();
    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_error_occurs_on_finding_object_by_id() throws Exception {
        SimpleObject object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleObject object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1).build();

        addToRepository(object1, object2);

        doThrow(new IOException()).when(objectMapper).fromJson(any(byte[].class), eq(SimpleObject.class));
        loader.findByObjectId(repoDirectory, "objet1");
    }

    @Test
    public void should_find_object_included_in_another() throws Exception {
        SimpleObject object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleObject object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1).build();

        addToRepository(object1, object2);

        //In this test we see the object id in the file not in the name of the file. So we
        //consider that our id is object1
        assertThat(loader.contains(repoDirectory, "objet1")).isTrue();
    }

    @Test
    public void should_find_any_object_included_in_another() throws Exception {
        SimpleObject object1 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet1").build();
        //My second object contains the first
        SimpleObject object2 = SimpleObjectBuilder.aSimpleObjectBuilder().id("objet2").another(object1).build();

        addToRepository(object1, object2);


        //In this test we see the object id in the file not in the name of the file. So we
        //consider that our id is object1
        assertThat(loader.contains(repoDirectory, "object2")).isFalse();
    }

    @Test
    public void should_load_a_single_page_in_the_import_folder() throws Exception {
        SimpleObject object1 = aFilledSimpleObject("objet1");
        addToRepository(object1);

        SimpleObject loadedSimpleObject = loader.load(repoDirectory.resolve("objet1"), "objet1.json");

        assertThat(loadedSimpleObject).isEqualTo(object1);
    }

    @Test
    public void should_throw_import_exception_when_there_are_no_pages_in_import_folder() throws Exception {
        exception.expect(ImportException.class);
        exception.expect(hasType(ImportException.Type.PAGE_NOT_FOUND));

        loader.load(repoDirectory, "test");
    }

}
