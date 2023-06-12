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

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetFileBasedPersister;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    UiDesignerProperties uiDesignerProperties = new UiDesignerProperties("1.13.0", "2.0");
    
    JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Before
    public void setup() throws Exception {
        uiDesignerProperties.getWorkspaceUid().setLiveBuildEnabled(true);
        widgetDirectiveBuilder = new WidgetDirectiveBuilder(uiDesignerProperties, watcher, new WidgetFileBasedLoader(jsonHandler), htmlSanitizer);

        WidgetFileBasedLoader widgetLoader = new WidgetFileBasedLoader(jsonHandler);
        WorkspaceProperties workspaceProperties = new WorkspaceProperties();
        workspaceProperties.getWidgets().setDir(Paths.get(widgetRepositoryDirectory.getRoot().getPath()));
        WidgetRepository repository = new WidgetRepository(
                workspaceProperties,
                new WorkspaceUidProperties(),
                new WidgetFileBasedPersister(jsonHandler,validator,uiDesignerProperties),
                widgetLoader,
                validator,
                mock(Watcher.class),
                mock(UiDesignerProperties.class));

        pbInput = aWidget().withId("pbInput").build();
        pbInput.setCustom(true);
        createDirectories(repository.resolvePath(pbInput.getId()));
        repository.updateLastUpdateAndSave(pbInput);

        pbButton = aWidget().withId("pbButton").build();
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
        //
        widgetRepositoryDirectory.newFile("whatever.txt");

        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        //assert that we do not create a whatever.js file ?
        assertThat(widgetRepositoryDirectory.getRoot().list()).containsOnly(".metadata", "pbButton", "whatever.txt", "pbInput");
    }

    @Test
    public void should_watch_given_directory_to_build_directives_on_change() throws Exception {

        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        verify(watcher).watch(eq(widgetRepositoryDirectory.getRoot().toPath()), any(PathListener.class));
    }
    
    @Test
    public void should_note_watch_given_directory_when_live_build_is_disabled() throws Exception {
        uiDesignerProperties.getWorkspaceUid().setLiveBuildEnabled(false);
        widgetDirectiveBuilder = new WidgetDirectiveBuilder(uiDesignerProperties, watcher, new WidgetFileBasedLoader(jsonHandler), htmlSanitizer);
        widgetDirectiveBuilder.start(widgetRepositoryDirectory.getRoot().toPath());

        verify(watcher, never()).watch(eq(widgetRepositoryDirectory.getRoot().toPath()), any(PathListener.class));
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
        return new String(readAllBytes(getDirectivePath(widgetId)));
    }

    private Path getDirectivePath(String widgetId) {
        return resolve(widgetId + "/" + widgetId + ".js");
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
