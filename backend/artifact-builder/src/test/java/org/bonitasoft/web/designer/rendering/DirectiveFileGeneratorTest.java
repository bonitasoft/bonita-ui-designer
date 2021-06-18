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
package org.bonitasoft.web.designer.rendering;

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

/**
 * @author Benjamin Parisel
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectiveFileGeneratorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectiveFileGenerator generator;

    private WorkspaceProperties workspaceProperties = new WorkspaceProperties();

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    @Before
    public void beforeEach() {
        workspaceProperties.getPages().setDir(Paths.get(temporaryFolder.toPath().toString()));
        workspaceProperties.getWidgets().setDir(Paths.get(temporaryFolder.toPath().toString()));
        generator = new DirectiveFileGenerator(workspaceProperties, widgetRepository, widgetIdVisitor);
    }

    @Test
    public void should_list_all_widgets_except_container_used_in_page() throws Exception {
        Page page = aPage().with(aContainer().build()).build();
        mockWidgetIdVisitorAndWidgetRepository(page, "pbLabel", "paragraph", "pbContainer");

        List<Path> widgesFiles = generator.getWidgetsFilesUsedInPage(page);

        assertThat(widgesFiles).containsOnly(temporaryFolder.toPath().resolve("pbLabel").resolve("pbLabel.js"), temporaryFolder.toPath().resolve("paragraph")
                .resolve("paragraph.js"));
        assertThat(widgesFiles).doesNotContain(temporaryFolder.toPath().resolve("pbContainer").resolve("pbContainer.js"));
    }

    @Test
    public void should_get_file_name_of_concatenation_widgets_directives_file_who_was_use_in_page() throws Exception {
        Page page = aPage().build();
        initWidgetsFileWhoUsedInPage(page);
        mockWidgetIdVisitorAndWidgetRepository(page, "pbLabel", "paragraph");
        Path pagePath = temporaryFolder.toPath().resolve("pages").resolve(page.getId());

        String filename = generator.generateAllDirectivesFilesInOne(page, pagePath);

        assertThat(filename).isEqualTo("widgets-f66d6f4cc38b2259ec29fa16e9f171251cd3d346.min.js");
    }

    @Test
    public void should_concatenate_and_minify_widgets_directives_file_who_was_use_in_page() throws Exception {
        Page page = aPage().build();
        initFilesForConcatAndMinify(page);
        mockWidgetIdVisitorAndWidgetRepository(page, "pbLabel", "paragraph");
        Path path = temporaryFolder.toPath().resolve("pages").resolve(page.getId());
        String expected = "\nList<String>filename=getWidgetsFilesUsedInPage(previewable);" +
                "byte[]content=getConcatenationWidgetsDirectives(filename);file2";

        String filename = generator.generateAllDirectivesFilesInOne(page, path);

        assertThat(readString(path.resolve(filename))).isEqualTo(expected);
        assertThat(filename).isEqualTo("widgets-0f2d4ba1fa1992794df3dca7a9b8e4ec735b4746.min.js");
    }

    private void initWidgetsFileWhoUsedInPage(Page page) throws IOException {
        temporaryFolder.newFolder("pbLabel");
        temporaryFolder.newFolder("paragraph");
        temporaryFolder.newFolder("pages", page.getId());
        write(temporaryFolder.newFile("pbLabel/pbLabel.js").toPath(), "file1".getBytes());
        write(temporaryFolder.newFile("paragraph/paragraph.js").toPath(), "file2".getBytes());
    }


    private void mockWidgetIdVisitorAndWidgetRepository(Page page, String... ids) {
        HashSet<String> widgetIds = new HashSet<>(asList(ids));
        List<Widget> widgets = Arrays.asList(ids).stream()
                .map(id -> aWidget().withId(id).build())
                .collect(Collectors.toList());
        when(widgetIdVisitor.visit(page)).thenReturn(widgetIds);
        when(widgetRepository.getByIds(widgetIds))
                .thenReturn(widgets);
    }

    private void initFilesForConcatAndMinify(Page page) throws IOException {
        temporaryFolder.newFolder("pbLabel");
        temporaryFolder.newFolder("paragraph");
        temporaryFolder.newFolder("pages", page.getId());
        write(temporaryFolder.newFile("pbLabel/pbLabel.js").toPath(),
                (" List<String> filename = getWidgetsFilesUsedInPage(previewable);\n byte[] content = " +
                        "getConcatenationWidgetsDirectives(filename);").getBytes());
        write(temporaryFolder.newFile("paragraph/paragraph.js").toPath(), "file2".getBytes());
    }
}
