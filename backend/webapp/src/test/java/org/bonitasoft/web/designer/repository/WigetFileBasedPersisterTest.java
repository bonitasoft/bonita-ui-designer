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

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readAllLines;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class WigetFileBasedPersisterTest {

    private static final String DESIGNER_VERSION = "2.0.0";

    private Path repoDirectory;
    private JacksonObjectMapper objectMapper;
    private WidgetFileBasedPersister widgetPersister;

    @Mock
    private BeanValidator validator;

    @Before
    public void setUp() throws IOException {
        repoDirectory = Files.createTempDirectory("jsonrepository");
        objectMapper = spy(new DesignerConfig().objectMapperWrapper());
        widgetPersister = new WidgetFileBasedPersister(objectMapper, validator);

        ReflectionTestUtils.setField(widgetPersister, "version", DESIGNER_VERSION);
    }

    @After
    public void clean() {
        deleteQuietly(repoDirectory.toFile());
    }

    private Widget getFromRepository(String id) throws IOException {
        byte[] json = readAllBytes(repoDirectory.resolve(id + ".json"));
        return objectMapper.fromJson(json, Widget.class);
    }

    @Test
    public void should_serialize_an_object_and_save_it_to_a_file() throws Exception {
        String templateFileName = "input.tpl.html";
        String controllerFileName = "input.ctrl.js";
        String htmlContent = "<div></div>";
        String jsContent = "function ($scope) {}";
        Widget widgetToSave = aWidget().id("input").template(htmlContent).controller(jsContent).build();

        widgetPersister.save(repoDirectory, widgetToSave);

        Widget savedObject = getFromRepository("input");
        assertThat(savedObject.getTemplate()).isEqualTo("@" + widgetToSave.getId() + ".tpl.html");
        assertThat(savedObject.getController()).isEqualTo("@" + widgetToSave.getId() + ".ctrl.js");
        assertThat(readAllLines(repoDirectory.resolve(widgetToSave.getId() + ".ctrl.js"), StandardCharsets.UTF_8).get(0)).isEqualTo(jsContent);
        assertThat(readAllLines(repoDirectory.resolve(widgetToSave.getId() + ".tpl.html"), StandardCharsets.UTF_8).get(0)).isEqualTo(htmlContent);

    }

}
