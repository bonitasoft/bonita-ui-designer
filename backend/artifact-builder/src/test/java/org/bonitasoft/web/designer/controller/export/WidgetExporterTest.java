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
package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WidgetExporterTest {

    @Rule
    public TemporaryFolder repositoryFolder = new TemporaryFolder();

    @Mock
    private WidgetService widgetService;

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private WidgetExporter exporter;

    private ByteArrayOutputStream artifactStream;

    @Before
    public void setUp() throws Exception {
        artifactStream = new ByteArrayOutputStream();
        exporter = new WidgetExporter(jsonHandler, widgetService, mock(ExportStep.class));
    }

    private Widget create(Widget widget) throws IOException {
        if (widget.getId() == null) {
            widget.setId("default-id");
        }
        when(widgetService.get(widget.getId())).thenReturn(widget);
        write(repositoryFolder.toPath().resolve(format("%s.json", widget.getId())), jsonHandler.toJson(widget, JsonViewPersistence.class));
        return widget;
    }

    private Path unzip(ByteArrayOutputStream artifactZipStream) throws IOException {
        return new Unzipper().unzipInTempDir(new ByteArrayInputStream(artifactZipStream.toByteArray()), "exportertest");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_null() throws Exception {

        exporter.handleFileExport(null, artifactStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_blank() throws Exception {

        exporter.handleFileExport(" ", artifactStream);
    }

    @Test
    public void should_throw_exception_when_artefact_to_export_is_not_found() throws Exception {
        NotFoundException cause = new NotFoundException("Widget not found");
        when(widgetService.get("unknown-id")).thenThrow(cause);

        Throwable throwable = catchThrowable(() ->
                exporter.handleFileExport("unknown-id", artifactStream)
        );

        assertThat(throwable)
                .isInstanceOf(ExportException.class)
                .hasCause(cause);

    }

    @Test
    public void should_export_template_and_controller_file_reference() throws Exception {
        Widget widget = create(WidgetBuilder.aWidget().withId("aWidget").custom().controller("function widgetCtrl(){}").template("<p>A widget label</p>").build());
        exporter.handleFileExport(widget.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        var res = "{\"id\":\"aWidget\",\"name\":\"aName\",\"type\":\"widget\",\"custom\":true,\"template\":\"@aWidget.tpl.html\",\"controller\":\"@aWidget.ctrl.js\",\"properties\":[],\"assets\":[],\"requiredModules\":[],\"webResources\":[],\"hasHelp\":false}";
        assertThat(readString(unzipped.resolve("resources/widget.json"))).isEqualTo(res);
    }

    @Test
    public void should_export_a_widget_without_template_and_controller() throws Exception {
        Widget widget = create(WidgetBuilder.aWidget().withId("aWidget").custom().build());
        widget.setTemplate(null);
        exporter.handleFileExport(widget.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        var res = "{\"id\":\"aWidget\",\"name\":\"aName\",\"type\":\"widget\",\"custom\":true,\"properties\":[],\"assets\":[],\"requiredModules\":[],\"webResources\":[],\"hasHelp\":false}";
        assertThat(readString(unzipped.resolve("resources/widget.json"))).isEqualTo(res);
    }
}
