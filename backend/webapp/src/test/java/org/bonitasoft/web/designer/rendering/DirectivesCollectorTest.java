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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * @author Benjamin Parisel
 */
@RunWith(MockitoJUnitRunner.class)
public class DirectivesCollectorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @InjectMocks
    private DirectivesCollector collector;

    @Mock
    private WorkspacePathResolver pathResolver;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private FragmentIdVisitor fragmentIdVisitor;

    @Mock
    private DirectiveFileGenerator directiveFileGenerator;

    @Before
    public void beforeEach() throws IOException {
        lenient().when(pathResolver.getPagesRepositoryPath()).thenReturn(temporaryFolder.toPath());
        collector = new DirectivesCollector(pathResolver, directiveFileGenerator, fragmentIdVisitor, fragmentRepository);

        when(pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("pages"));
        when(pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("pages"));
        lenient().when(pathResolver.getTmpFragmentsRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("fragments"));

        lenient().when(pathResolver.getFragmentsRepositoryPath())
                .thenReturn(Paths.get(temporaryFolder.toPath().toString(),
                        "fragments"));

        when(pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("pages"));
        when(pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("pages"));
        when(pathResolver.getTmpFragmentsRepositoryPath()).thenReturn(temporaryFolder.toPath().resolve("fragments"));
    }

    @Test
    public void should_build_directives_from_the_preview() throws IOException {
        Page page = aPage().build();
        when(pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath());
        Path assets = temporaryFolder.toPath().resolve(page.getId()).resolve("js");
        when(directiveFileGenerator.generateAllDirectivesFilesInOne(page, assets)).thenReturn("widgets-123456.min.js");

        List<String> imports = collector.buildUniqueDirectivesFiles(page, page.getId());

        Assertions.assertThat(imports).containsOnly("js/widgets-123456.min.js");
    }

    @Test
    public void should_collect_widgets_directives_from_the_fragment_preview() throws Exception {
        Fragment fragment = FragmentBuilder.aFragment().build();
        when(directiveFileGenerator.generateAllDirectivesFilesInOne(fragment, temporaryFolder.toPath().resolve("fragments")
                .resolve(fragment.getId())))
                .thenReturn("widgets-654321");

        List<String> imports = collector.buildUniqueDirectivesFiles(fragment, fragment.getId());

        assertThat(imports).containsOnly("widgets-654321");
    }

    @Test
    public void should_collect_widget_file_directive_and_fragment_file_when_fragment_will_be_use_in_page() throws IOException {
        Page page = PageBuilder.aPage().build();
        Fragment fragment = FragmentBuilder.aFragment().build();
        initFileAndMockForPageWhoHasFragment(page, fragment);
        List<String> expected = asList("js/widgets-123456.js",
                "fragments/" + fragment.getId() + "/" + fragment.getId() + ".js");

        List<String> imports = collector.buildUniqueDirectivesFiles(page, page.getId());

        assertThat(imports).isEqualTo(expected);
    }

    private void initFileAndMockForPageWhoHasFragment(Page page, Fragment fragment) throws IOException {
        Path pagePathAsset = temporaryFolder.toPath().resolve("pages").resolve(page.getId()).resolve("js");
        Path fragmentsPath = temporaryFolder.toPath().resolve("fragments").resolve(fragment.getId());

        lenient().when(directiveFileGenerator.generateAllDirectivesFilesInOne(page, pagePathAsset)).thenReturn("widgets-123456.js");
        lenient().when(directiveFileGenerator.generateAllDirectivesFilesInOne(fragment, fragmentsPath)).thenReturn("widgets-654321.js");

        HashSet<String> fragmentIds = new HashSet<>(asList(fragment.getId()));
        when(fragmentIdVisitor.visit(page)).thenReturn(fragmentIds);
        when(fragmentRepository.getByIds(fragmentIds)).thenReturn(asList(fragment));
    }
}
