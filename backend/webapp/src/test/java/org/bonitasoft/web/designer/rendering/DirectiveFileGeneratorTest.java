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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.io.Files;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Benjamin Parisel
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectiveFileGeneratorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private DirectiveFileGenerator generator;
    @Mock
    private WorkspacePathResolver pathResolver;
    @Mock
    private WidgetRepository widgetRepository;
    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    @Before
    public void beforeEach() {
        generator = new DirectiveFileGenerator(pathResolver, widgetRepository, widgetIdVisitor);
        when(pathResolver.getPagesRepositoryPath()).thenReturn(Paths.get(temporaryFolder.toPath().toString()));
        when(pathResolver.getWidgetsRepositoryPath()).thenReturn(Paths.get(temporaryFolder.toPath().toString()));
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

        assertThat(filename).isEqualTo("widgets-f8b2ef17808cccb95dbf0973e7745cd53c29c684.js");


    }

    private void initWidgetsFileWhoUsedInPage(Page page) throws IOException {
        temporaryFolder.newFolder("pbLabel");
        temporaryFolder.newFolder("paragraph");
        temporaryFolder.newFolder("pages", page.getId());
        Files.write("file1".getBytes(), temporaryFolder.newFile("pbLabel/pbLabel.js"));
        Files.write("file2".getBytes(), temporaryFolder.newFile("paragraph/paragraph.js"));
    }


    private void mockWidgetIdVisitorAndWidgetRepository(Page page, String... ids) {
        HashSet<String> widgetIds = new HashSet<>(asList(ids));
        List<Widget> widgets = Arrays.asList(ids).stream()
                .map(id -> aWidget().id(id).build())
                .collect(Collectors.toList());
        when(widgetIdVisitor.visit(page)).thenReturn(widgetIds);
        when(widgetRepository.getByIds(widgetIds))
                .thenReturn(widgets);
    }


}
