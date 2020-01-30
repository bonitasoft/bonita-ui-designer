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

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private DirectiveFileGenerator directiveFileGenerator;

    @Before
    public void beforeEach() throws IOException {
        when(pathResolver.getPagesRepositoryPath()).thenReturn(temporaryFolder.toPath());
    }

    @Test
    public void should_build_directives_from_the_preview() throws IOException {
        Page page = aPage().build();
        when( pathResolver.getTmpPagesRepositoryPath()).thenReturn(temporaryFolder.toPath());
        Path assets = temporaryFolder.toPath().resolve(page.getId()).resolve("js");
        when(directiveFileGenerator.generateAllDirectivesFilesInOne(page, assets)).thenReturn("widgets-123456.min.js");

        List<String> imports = collector.buildUniqueDirectivesFiles(page, page.getId());

        Assertions.assertThat(imports).containsOnly("js/widgets-123456.min.js");
    }
}
