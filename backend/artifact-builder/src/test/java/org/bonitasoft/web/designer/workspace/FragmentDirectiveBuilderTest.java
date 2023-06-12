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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.anInput;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FragmentDirectiveBuilderTest {

    @Rule
    public TemporaryFolder repositoryDirectory = new TemporaryFolder();

    @Mock
    private Watcher watcher;

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Mock
    private HtmlBuilderVisitor htmlBuilderVisitor;

    private HtmlSanitizer htmlSanitizer = new HtmlSanitizer();

    private FragmentDirectiveBuilder fragmentDirectiveBuilder;

    private File fragmentFile;

    private Fragment fragment;

    private TemplateEngine htmlBuilder = new TemplateEngine("fragmentDirectiveTemplate.hbs.js");

    @Before
    public void setUp() throws Exception {
        fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler, htmlBuilderVisitor, htmlSanitizer, new WorkspaceUidProperties());
        fragmentFile = repositoryDirectory.newFile("fragment.json");
        fragment = aFragment()
                .withName("aUnicornFragment")
                .with(anInput().build())
                .build();

        write(fragmentFile.toPath(), jsonHandler.toJson(fragment));

        when(htmlBuilderVisitor.build(anyList())).thenReturn("");
    }

    @Test
    public void should_build_a_fragment_directive() throws Exception {
        when(htmlBuilderVisitor.build(anyList())).thenReturn("<p>content</p>");
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
    public void should_not_watch_given_directory_when_live_build_is_disabled() throws Exception {
        var workspaceUid = new WorkspaceUidProperties();
        workspaceUid.setLiveBuildEnabled(false);
        fragmentDirectiveBuilder = new FragmentDirectiveBuilder(watcher, jsonHandler, htmlBuilderVisitor, htmlSanitizer, workspaceUid);
        fragmentDirectiveBuilder.start(repositoryDirectory.getRoot().toPath());

        verify(watcher, never()).watch(eq(repositoryDirectory.getRoot().toPath()), any(PathListener.class));
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
