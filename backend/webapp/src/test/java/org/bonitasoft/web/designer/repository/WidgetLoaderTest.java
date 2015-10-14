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

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class WidgetLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path widgetDirectory;
    private WidgetLoader widgetLoader;

    @Before
    public void setUp() {
        widgetDirectory = Paths.get(temporaryFolder.getRoot().getPath());
        widgetLoader = new WidgetLoader(new DesignerConfig().objectMapperWrapper());
    }

    private void addToDirectory(Path directory, Widget... widgets) throws Exception {
        for (Widget widget : widgets) {
            Path widgetDir = createDirectory(directory.resolve(widget.getId()));
            writeWidgetMetadataInFile(widget, widgetDir.resolve(widget.getId() + ".json"));
        }
    }

    private void writeWidgetMetadataInFile(Widget widget, Path path) throws IOException {
        ObjectWriter writer = new ObjectMapper().writer();
        writer.writeValue(path.toFile(), widget);
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        Widget expectedWidget = WidgetBuilder.aWidget().id("input").build();
        Widget notExpectedWidget = WidgetBuilder.aWidget().id("label").build();
        addToDirectory(widgetDirectory, expectedWidget, notExpectedWidget);

        Widget widget = widgetLoader.get(widgetDirectory, "input");

        assertThat(widget).isEqualTo(expectedWidget);
    }

    @Test
    public void should_retrieve_all_widgets() throws Exception {
        Widget input = WidgetBuilder.aWidget().id("input").build();
        Widget label = WidgetBuilder.aWidget().id("label").build();
        addToDirectory(widgetDirectory, input, label);

        List<Widget> widgets = widgetLoader.getAll(widgetDirectory);

        assertThat(widgets).containsOnly(input, label);
    }

    @Test
    public void should_retrieve_all_custom_widgets() throws Exception {
        Widget input = WidgetBuilder.aWidget().id("input").build();
        Widget custom1 = WidgetBuilder.aWidget().id("custom1").custom().build();
        Widget custom2 = WidgetBuilder.aWidget().id("custom2").custom().build();
        addToDirectory(widgetDirectory, input, custom1, custom2);

        List<Widget> widgets = widgetLoader.getAllCustom(widgetDirectory);

        assertThat(widgets).containsOnly(custom1, custom2);
    }

    @Test
    public void should_find_widget_which_use_another_widget() throws Exception {
        Widget input = WidgetBuilder.aWidget().id("input").build();
        Widget label = WidgetBuilder.aWidget().id("label").template("use <input>").build();
        addToDirectory(widgetDirectory, input, label);

        //input is used by label
        assertThat(widgetLoader.findByObjectId(widgetDirectory, "input")).extracting("id").contains("label");
    }

    @Test
    public void should_find_widget_which_not_use_another_widget() throws Exception {
        Widget input = WidgetBuilder.aWidget().id("input").build();
        Widget label = WidgetBuilder.aWidget().id("label").template("use <input>").build();
        addToDirectory(widgetDirectory, input, label);

        //label is used by noone
        assertThat(widgetLoader.findByObjectId(widgetDirectory, "label")).isEmpty();
    }

    @Test
    public void should_load_a_single_page_in_the_import_folder() throws Exception {
        Widget input = WidgetBuilder.aWidget().id("input").build();
        addToDirectory(widgetDirectory, input);

        Widget widget = widgetLoader.load(widgetDirectory.resolve("input"), "input.json");

        assertThat(widget).isEqualTo(input);
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_notfound_exception_when_there_are_no_pages_in_folder() throws Exception {
        widgetLoader.load(widgetDirectory, "test");
    }

    @Test(expected = JsonReadException.class)
    public void should_throw_json_read_exception_when_loaded_file_is_not_valid_json() throws Exception {
        write(widgetDirectory.resolve("wrongjson.json"), "notJson".getBytes());

        widgetLoader.load(widgetDirectory, "wrongjson.json");
    }
}
