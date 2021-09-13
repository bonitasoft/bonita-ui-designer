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
package org.bonitasoft.web.designer.visitor.angular;

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;

import static java.lang.String.format;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.assertThatHtmlBody;
import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.toBody;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AngularBuilderVisitorTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Rule
    public TestResource testResource = new TestResource(HtmlGenerator.class);


    @Mock
    private WidgetService widgetService;

    private AngularBuilderVisitor visitor;

    @Before
    public void setUp() throws Exception {
        var workspaceProperties =  new WorkspaceProperties();
        workspaceProperties.getWidgets().setDir(tempDir.toPath());
        visitor = new AngularBuilderVisitor(workspaceProperties, widgetService);

    }

    @Test
    public void should_build_a_component_html_when_visiting_a_component() throws Exception {
        var uidInput = tempDir.newFolderPath("uid-input");
        Widget w = WidgetBuilder.aWidgetWc().withId("uid-input").withHtmlBundle("uidInput.html").build();

        write(uidInput.resolve("uidInput.html"), "<uid-input [label]=\"{{properties.label}}\" [value]=\"properties.value\"></uid-input>".getBytes(StandardCharsets.UTF_8));

        when(widgetService.get("uid-input")).thenReturn(w);


        assertThatHtmlBody(visitor.visit(anInput().withWidgetId("uid-input")
                .withReference("component-reference")
                .withDimension(12)
                .withPropertyValue("label", "Default label")
                .withPropertyValue("value", "variable","myValue")
                .build())).isEqualToBody(testResource.load("angular/component.html"));
    }


    @Test
    public void should_add_elements_to_the_container_rows() throws Exception {
        var uidLabelPath = tempDir.newFolderPath("uid-label");
        var customLabelPath = tempDir.newFolderPath("customLabel");

        write(uidLabelPath.resolve("uid-label.html"), "<uid-label [label]=\"{{properties.label}}\"></uid-label>".getBytes(StandardCharsets.UTF_8));
        write(customLabelPath.resolve("customLabel.html"), "<customLabel></customLabel>".getBytes(StandardCharsets.UTF_8));

        Widget uidLabel = WidgetBuilder.aWidgetWc().withId("uid-label").withHtmlBundle("uid-label.html").build();
        Widget customLabel = WidgetBuilder.aWidgetWc().withId("customLabel").withHtmlBundle("customLabel.html").build();
        when(widgetService.get("uid-label")).thenReturn(uidLabel);
        when(widgetService.get("customLabel")).thenReturn(customLabel);

        // we should have two div.col-xs-12 with two div.row containing added components
        Elements rows = toBody(visitor.visit(aContainer().with(
                aRow().with(
                        aComponent().withWidgetId("uid-label").withPropertyValue("label", "Default label").build()),
                aRow().with(
                        aComponent().withWidgetId("customLabel").build()))
                .build())).select(".row");

        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.first().select("uid-label").outerHtml()).isEqualTo("<uid-label [label]=\"{{properties.label}}\"></uid-label>");
        assertThat(rows.last().select("customLabel").outerHtml()).isEqualTo("<customLabel></customLabel>");
    }
}
