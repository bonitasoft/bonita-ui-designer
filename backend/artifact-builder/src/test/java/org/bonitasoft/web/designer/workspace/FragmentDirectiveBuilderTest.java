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
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.angularJS.AngularJsBuilderVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class FragmentDirectiveBuilderTest {

    @Rule
    public TemporaryFolder repositoryDirectory = new TemporaryFolder();

    @Mock
    private Watcher watcher;

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
    private AngularJsBuilderVisitor angularJsHtmlBuilderVisitor;

    private HtmlSanitizer htmlSanitizer = new HtmlSanitizer();

    @InjectMocks
    private FragmentDirectiveBuilder fragmentDirectiveBuilder;

    private File fragmentFile;

    private Fragment fragment;

    private TemplateEngine htmlBuilder = new TemplateEngine("fragmentDirectiveTemplate.hbs.js");

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler, angularJsHtmlBuilderVisitor, htmlSanitizer);
        fragmentFile = repositoryDirectory.newFile("fragment.json");
        fragment = aFragment()
                .withName("aUnicornFragment")
                .with(anInput().build())
                .build();

        write(fragmentFile.toPath(), jsonHandler.toJson(fragment));

        when(angularJsHtmlBuilderVisitor.build(anyList())).thenReturn("");
    }

    @Test
    public void should_build_a_fragment_directive() throws Exception {
        when(angularJsHtmlBuilderVisitor.build(anyList())).thenReturn("<p>content</p>");
        fragmentDirectiveBuilder.build(fragmentFile.toPath());

        String directive = new String(readAllBytes(get(repositoryDirectory.getRoot().toString(), "fragment.js")));

        assertThat(directive).isEqualTo(generateDirective(fragment, "<p>content</p>"));
    }

    @Test
    public void should_watch_given_directory_to_build_directives_on_change() throws Exception {

        fragmentDirectiveBuilder.start(repositoryDirectory.getRoot().toPath());

        verify(watcher).watch(eq(repositoryDirectory.getRoot().toPath()), any(PathListener.class));
    }

    @Test
    public void should_exclude_metadata_from_the_build() throws Exception {

        boolean isBuildable = fragmentDirectiveBuilder.isBuildable(".metadata/123.json");

        assertThat(isBuildable).isFalse();
    }

    /**
     * Generate directive
     *
     * @param fragment from which the directive is generated
     * @return the directive as a string
     * @throws IOException
     */
    private String generateDirective(Fragment fragment, String content) throws IOException {
        return htmlBuilder.with("rowsHtml", content).build(fragment);
    }
}
