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
package org.bonitasoft.web.designer.workspace;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.designer.config.AppProperties;
import org.bonitasoft.web.designer.config.AppProperties.DesignerProperties;
import org.bonitasoft.web.designer.config.AppProperties.UidProperties;
import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetDirectiveBuilderTest {

    @Rule
    public TemporaryFolder widgetRepositoryDirectory = new TemporaryFolder();

    @Mock
    Watcher watcher;

    @Mock
    BeanValidator validator;

    WidgetDirectiveBuilder widgetDirectiveBuilder;

    Widget pbInput;

    Widget pbButton;

    HtmlSanitizer htmlSanitizer = new HtmlSanitizer();

    TemplateEngine htmlBuilder = new TemplateEngine("widgetDirectiveTemplate.hbs.js");

    @Before
    public void setup() throws Exception {
        JacksonObjectMapper jacksonObjectMapper = new DesignerConfig().objectMapperWrapper();
        widgetDirectiveBuilder = new WidgetDirectiveBuilder(watcher, new WidgetFileBasedLoader(jacksonObjectMapper), htmlSanitizer);

        WidgetFileBasedLoader widgetLoader = new WidgetFileBasedLoader(jacksonObjectMapper);

        WidgetRepository repository = new WidgetRepository(
                Paths.get(widgetRepositoryDirectory.getRoot().getPath()),
                new DesignerConfig().widgetFileBasedPersister(),
                widgetLoader,
                validator,
                mock(Watcher.class),
                new UidProperties());

        pbInput = aWidget().id("pbInput").build();
        pbInput.setCustom(true);
        createDirectories(repository.resolvePath(pbInput.getId()));
        repository.updateLastUpdateAndSave(pbInput);

        pbButton = aWidget().id("pbButton").build();
        pbButton.setCustom(true);
        createDirectories(repository.resolvePath(pbButton.getId()));
        repository.updateLastUpdateAndSave(pbButton);
    }

    @Test
    public void should_build_directives_of_a_given_directory() throws Exception {
        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        assertThat(readDirective("pbInput")).isEqualTo(generateDirective(pbInput));
        assertThat(readDirective("pbButton")).isEqualTo(generateDirective(pbButton));
    }

    @Test
    public void should_only_build_directives_files() throws Exception {
        widgetRepositoryDirectory.newFile("whatever.txt");

        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        assertThat(widgetRepositoryDirectory.getRoot().list()).containsOnly(".metadata", "pbButton", "whatever.txt", "pbInput");
    }

    @Test
    public void should_watch_given_directory_to_build_directives_on_change() throws Exception {

        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        verify(watcher).watch(eq(widgetRepositoryDirectory.getRoot().toPath()), any(PathListener.class));
    }

    @Test
    public void should_build_directive_even_if_it_already_exist() throws Exception {
        writeDirective("pbInput", "previous content".getBytes());

        widgetDirectiveBuilder.build(resolve("pbInput/pbInput.json"));

        assertThat(readDirective("pbInput")).isEqualTo(generateDirective(pbInput));
    }

    @Test
    public void should_exclude_metadata_from_the_build() throws Exception {

        boolean isBuildable = widgetDirectiveBuilder.isBuildable(".metadata/123.json");

        assertThat(isBuildable).isFalse();
    }

    /**
     * Read directive content found at widgetId/widgetId.js
     *
     * @param widgetId id of the widget
     * @return the directive as a string
     * @throws IOException
     */
    private String readDirective(String widgetId) throws IOException {
        return new String(readAllBytes(resolve(widgetId + "/" + widgetId + ".js")));
    }

    /**
     * Generate directive
     *
     * @param widget from which the directive is generated
     * @return the directive as a string
     * @throws IOException
     */
    private String generateDirective(Widget widget) throws IOException {
        return htmlBuilder.with("escapedTemplate", widget.getTemplate()).build(widget);
    }

    /**
     * Write contents into widgetId/widgetId.js file.
     *
     * @param widgetId id of the widget
     * @param contents contents of the file
     * @throws IOException
     */
    private void writeDirective(String widgetId, byte[] contents) throws IOException {
        write(resolve(widgetId + "/" + widgetId + ".js"), contents);
    }

    /**
     * Resolve path from widgetRepositoryDirectory
     *
     * @param path to resolve
     * @return resolved path
     */
    private Path resolve(String path) {
        return widgetRepositoryDirectory.getRoot().toPath().resolve(path);
    }

}
