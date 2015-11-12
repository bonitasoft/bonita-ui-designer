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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Sets;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.migration.LiveMigration;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class WidgetRepositoryTest {

    private static final String DESIGNER_VERSION = "2.0.0";

    private WidgetRepository widgetRepository;

    private JacksonObjectMapper objectMapper;

    private Path widgetDirectory;

    private JsonFileBasedPersister<Widget> jsonFileRepository;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private LiveMigration<Widget> liveMigration;

    @Before
    public void setUp() throws IOException {
        widgetDirectory = Paths.get(temporaryFolder.getRoot().getPath());
        jsonFileRepository = new DesignerConfig().widgetFileBasedPersister();
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        // spying objectMapper to be able to simulate a json convertion error
        objectMapper = spy(new DesignerConfig().objectMapperWrapper());
        widgetRepository = new WidgetRepository(
                widgetDirectory,
                jsonFileRepository,
                new WidgetLoader(objectMapper),
                new BeanValidator(validatorFactory.getValidator()),
                new Watcher());

        ReflectionTestUtils.setField(jsonFileRepository, "version", DESIGNER_VERSION);
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget expectedWidget = aWidget().id("input").build();
        Widget notExpectedWidget = aWidget().id("label").build();
        addToRepository(expectedWidget, notExpectedWidget);

        Widget widget = widgetRepository.get("input");

        assertThat(widget).isEqualTo(expectedWidget);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_trying_to_get_an_unexisting_widget() throws Exception {

        widgetRepository.get("notExistingWidget");
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_when_error_occurs_when_getting_a_widget() throws Exception {
        doThrow(new IOException()).when(objectMapper).fromJson(any(byte[].class), eq(Widget.class));
        addToRepository(aWidget().id("input").build());

        widgetRepository.get("input");
    }

    @Test
    public void should_retrieve_all_widgets() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").build();
        addToRepository(input, label);

        List<Widget> widgets = widgetRepository.getAll();

        assertThat(widgets).containsOnly(input, label);
    }

    @Test(expected = RepositoryException.class)
    public void should_throw_RepositoryException_if_error_occurs_while_getting_all_widgets() throws Exception {
        doThrow(new IOException()).when(objectMapper).fromJson(any(byte[].class), eq(Widget.class));
        addToRepository(aWidget().id("input").build());

        widgetRepository.getAll();
    }

    @Test
    public void should_save_a_custom_widget() throws Exception {
        Widget customLabel = aWidget().custom().id("customLabel").build();

        createDirectories(widgetDirectory.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(jsonFile(customLabel)).exists();
        // last update field should be the current time
        assertThat(customLabel.getLastUpdate()).isGreaterThan(Instant.now().minus(5000));
    }

    @Test
    public void should_save_a_page_without_updating_last_update_date() throws Exception {
        Widget widget = widgetRepository.updateLastUpdateAndSave(aWidget().id("customLabel").name("theWidgetName").build());
        Instant lastUpdate = widget.getLastUpdate();

        widget.setName("newName");
        widgetRepository.save(widget);

        Widget fetchedWidget = widgetRepository.get(widget.getId());
        assertThat(fetchedWidget.getLastUpdate()).isEqualTo(lastUpdate);
        assertThat(fetchedWidget.getName()).isEqualTo("newName");
    }

    @Test
    public void should_save_a_list_of_custom_widgets() throws Exception {
        Widget customLabel = aWidget().custom().id("customLabel").build();
        Widget customInput = aWidget().custom().id("customInput").build();

        //For the first widget a directory is present... for the second it will be created during the saving
        createDirectories(widgetDirectory.resolve("customLabel"));
        widgetRepository.saveAll(asList(customInput, customLabel));

        assertThat(jsonFile(customLabel)).exists();
        assertThat(jsonFile(customInput)).exists();
    }

    @Test
    public void should_set_designer_version_while_saving_if_not_already_set() throws Exception {
        Widget customLabel = aWidget().custom().id("customLabel").build();

        createDirectories(widgetDirectory.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(customLabel.getDesignerVersion()).isEqualTo(DESIGNER_VERSION);
    }

    @Test
    public void should_not_set_designer_version_while_saving_if_already_set() throws Exception {
        Widget customLabel = aWidget().custom().id("customLabel").build();
        customLabel.setDesignerVersion("alreadySetVersion");
        createDirectories(widgetDirectory.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);

        assertThat(customLabel.getDesignerVersion()).isEqualTo("alreadySetVersion");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_while_saving_a_custom_widget_with_no_id_set() throws Exception {
        Widget aWidget = aWidget().id(null).custom().build();

        widgetRepository.updateLastUpdateAndSave(aWidget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_save_a_widget_without_name() throws Exception {
        Widget widget = aWidget().name(" ").build();

        widgetRepository.updateLastUpdateAndSave(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_save_a_widget_with_name_containing_non_alphanumeric_chars() throws Exception {
        Widget widget = aWidget().name("héllo").build();

        widgetRepository.create(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_save_as_widget_with_an_invalid_property() throws Exception {
        Widget widget = aWidget().property(aProperty().name("ze invalid name")).custom().build();

        widgetRepository.create(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_allow_to_save_a_custom_widget_with_name_containing_space() throws Exception {
        Widget widget = aWidget().name("hello world").custom().build();

        widgetRepository.create(widget);
    }

    @Test
    public void should_allow_to_save_a_widget_with_name_containing_space() throws Exception {
        Widget widget = aWidget().name("hello world").build();
        createDirectories(widgetDirectory.resolve("anId"));
        widgetRepository.updateLastUpdateAndSave(widget);
    }

    @Test
    public void should_delete_a_custom_widget() throws Exception {
        Widget customLabel = aWidget().id("customLabel").build();
        customLabel.setController("$scope.hello = 'Hello'");
        customLabel.setTemplate("<div>{{ hello + 'there'}}</div>");
        customLabel.setCustom(true);
        createDirectories(widgetDirectory.resolve("customLabel"));
        widgetRepository.updateLastUpdateAndSave(customLabel);
        // emulate js generation
        write(widgetDirectory.resolve("customLabel").resolve("customLabel.js"), objectMapper.toJson(""));

        widgetRepository.delete("customLabel");

        assertThat(jsonFile(customLabel)).doesNotExist();
        assertThat(jsFile(customLabel)).doesNotExist();
    }

    @Test(expected = NotAllowedException.class)
    public void should_not_allow_to_delete_a_pb_widget() throws Exception {
        Widget pbLabel = aWidget().id("pbLabel").build();
        pbLabel.setCustom(false);
        addToRepository(pbLabel);

        widgetRepository.delete("pbLabel");
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_create_a_widget_without_name() throws Exception {
        Widget widget = aWidget().name(" ").build();

        widgetRepository.create(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_create_a_widget_with_name_containing_non_alphanumeric_chars() throws Exception {
        Widget widget = aWidget().name("héllo").build();

        widgetRepository.create(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_allow_to_create_a_custom_widget_with_name_containing_non_space() throws Exception {
        Widget widget = aWidget().name("hello world").custom().build();

        widgetRepository.create(widget);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_allow_create_a_widget_with_name_containing_space_for_normal_widget() throws Exception {
        Widget widget = aWidget().name("hello world").build();

        widgetRepository.create(widget);
    }

    @Test
    public void should_create_a_widget_and_set_his_id() throws Exception {
        Widget expectedWidget = aWidget().name("aName").build();
        Widget createdWidget = widgetRepository.create(expectedWidget);

        expectedWidget.setId("customAName");
        expectedWidget.setCustom(true);
        assertThat(expectedWidget).isEqualTo(createdWidget);
        assertThat(jsonFile(createdWidget)).exists();
    }

    @Test(expected = NotAllowedException.class)
    public void should_not_allow_to_create_a_widget_with_an_already_existing_name() throws Exception {
        Widget widget = aWidget().name("existingName").id("customExistingName").build();
        addToRepository(widget);

        widgetRepository.create(aWidget().name("existingName").build());
    }

    @Test
    public void should_verify_that_a_widget_exists_in_the_repository() throws Exception {
        write(temporaryFolder.newFolder("pbInput").toPath().resolve("pbInput.json"), "contents".getBytes());

        assertThat(widgetRepository.exists("pbInput")).isTrue();
        assertThat(widgetRepository.exists("pbLabel")).isFalse();
    }

    @Test
    public void should_save_a_new_property() throws Exception {
        Widget aWidget = addToRepository(aWidget().custom().build());
        Property expectedProperty = aProperty().build();

        widgetRepository.addProperty(aWidget.getId(), expectedProperty);

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).contains(expectedProperty);
    }

    @Test(expected = NotAllowedException.class)
    public void should_not_allow_to_save_a_new_property_when_property_with_same_name_already_exists() throws Exception {
        Property alreadyAddedProperty = aProperty().build();
        Widget aWidget = addToRepository(aWidget().custom().property(alreadyAddedProperty).build());

        widgetRepository.addProperty(aWidget.getId(), alreadyAddedProperty);
    }

    @Test
    public void should_update_an_existing_property() throws Exception {
        Property initialParam = aProperty().name("propertyName").label("propertyLabel").build();
        Property updatedParam = aProperty().name("newName").label("newLablel").build();
        Widget aWidget = addToRepository(aWidget().custom().property(initialParam).build());

        widgetRepository.updateProperty(aWidget.getId(), initialParam.getName(), updatedParam);

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).contains(updatedParam);
        assertThat(widget.getProperties()).doesNotContain(initialParam);
    }

    @Test(expected = NotFoundException.class)
    public void should_fail_when_trying_to_update_a_not_existing_property() throws Exception {
        Widget expectedWidget = addToRepository(aWidget().custom().build());

        widgetRepository.updateProperty(expectedWidget.getId(), "notExistingProperty", new Property());
    }

    @Test
    public void should_delete_a_widget_property() throws Exception {
        Property aProperty = aProperty().name("aParam").build();
        Widget aWidget = addToRepository(aWidget().property(aProperty).property(aProperty().name("anotherParam")).build());

        List<Property> properties = widgetRepository.deleteProperty(aWidget.getId(), "aParam");

        Widget widget = getFromRepository(aWidget.getId());
        assertThat(widget.getProperties()).doesNotContain(aProperty);
        assertThat(properties).containsOnlyElementsOf(widget.getProperties());
    }

    @Test(expected = NotFoundException.class)
    public void should_fail_when_trying_to_delete_a_property_on_an_unknown_widget() throws Exception {
        widgetRepository.deleteProperty("unknownWidget", "aParam");
    }

    @Test(expected = NotFoundException.class)
    public void should_fail_when_trying_to_delete_an_unknown_property() throws Exception {
        Widget aWidget = addToRepository(aWidget().build());

        widgetRepository.deleteProperty(aWidget.getId(), "unknownPrameter");
    }

    @Test
    public void should_find_widget_which_use_another_widget() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").template("use <input>").build();
        addToRepository(input, label);

        //input is used by label
        assertThat(widgetRepository.findByObjectId("input")).extracting("id").containsExactly("label");
    }

    @Test
    public void should_find_widget_which_not_use_another_widget() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").template("use <input>").build();
        addToRepository(input, label);

        //label is used by noone
        assertThat(widgetRepository.findByObjectId("label")).isEmpty();
    }

    @Test
    public void should_find_widget_by_id() throws Exception {
        Widget input = aWidget().id("input").build();
        Widget label = aWidget().id("label").build();
        addToRepository(input, label);

        //input is used by label
        assertThat(widgetRepository.getByIds(Sets.newHashSet("input", "label"))).hasSize(2)
                .extracting("id").containsOnly("input", "label");
    }

    @Test
    public void should_walk_widget_repository() throws Exception {
        File file = temporaryFolder.newFile("file");
        final List<Path> visitedPaths = new ArrayList<>();

        widgetRepository.walk(new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                visitedPaths.add(file);
                return super.visitFile(file, attrs);
            }
        });

        assertThat(visitedPaths).containsExactly(file.toPath());
    }

    @Test
    public void should_watch_widget_repository() throws Exception {

        final List<Path> createdPaths = new ArrayList<>();

        widgetRepository.watch(new PathListener() {

            @Override
            public void pathCreated(Path path) {
                createdPaths.add(path);
            }

            @Override
            public void pathDeleted(Path path) {
            }

            @Override
            public void pathChanged(Path path) {
            }
        });

        File file = temporaryFolder.newFile("file");

        Thread.sleep(100); // Needed as we need DefaultFileMonitor.class to kick in

        assertThat(createdPaths).containsExactly(file.toPath());
    }

    @Test
    public void should_mark_a_widget_as_favorite() throws Exception {
        Widget widget = addToRepository(aWidget().notFavorite());

        widgetRepository.markAsFavorite(widget.getId());

        Widget fetchedWidget = getFromRepository(widget.getId());
        assertThat(fetchedWidget.isFavorite()).isTrue();

    }

    @Test
    public void should_unmark_a_widget_as_favorite() throws Exception {
        Widget widget = addToRepository(aWidget().favorite());

        widgetRepository.unmarkAsFavorite(widget.getId());

        Widget fetchedWidget = getFromRepository(widget.getId());
        assertThat(fetchedWidget.isFavorite()).isFalse();
    }

    private void addToRepository(Widget... widgets) throws Exception {
        for (Widget widget : widgets) {
            addToRepository(widget);
        }
    }

    private Widget addToRepository(WidgetBuilder widget) throws Exception {
        return addToRepository(widget.build());
    }

    private Widget addToRepository(Widget widget) throws Exception {
        Path widgetDir = createDirectory(widgetDirectory.resolve(widget.getId()));
        writeWidgetMetadataInFile(widget, widgetDir.resolve(widget.getId() + ".json"));
        return getFromRepository(widget.getId());
    }

    private Widget getFromRepository(String widgetId) {
        return widgetRepository.get(widgetId);
    }

    private void writeWidgetMetadataInFile(Widget widget, Path path) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer();
        writer.writeValue(path.toFile(), widget);
    }

    private File jsonFile(Widget widget) {
        return jsonFile(widget.getId());
    }

    private File jsonFile(String widgetId) {
        return widgetDirectory.resolve(widgetId).resolve(widgetId + ".json").toFile();
    }

    private File jsFile(Widget widget) {
        return widgetDirectory.resolve(widget.getId()).resolve(widget.getId() + ".js").toFile();
    }
}
