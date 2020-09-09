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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.*;

import java.io.OutputStream;

import org.bonitasoft.web.designer.utils.rule.TemporaryFragmentRepository;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FragmentsExportStepTest {

    private WorkspacePathResolver pathResolver = mock(WorkspacePathResolver.class);

    @Rule
    public TemporaryFragmentRepository repository = new TemporaryFragmentRepository(pathResolver);

    private FragmentsExportStep step;

    @Mock
    private FragmentPropertiesExportStep fragmentPropertiesExportStep;

    @Mock
    private Zipper zipper;

    @Before
    public void beforeEach() {
        step = new FragmentsExportStep(new FragmentIdVisitor(repository.toRepository()), pathResolver, fragmentPropertiesExportStep);
        zipper = spy(new Zipper(mock(OutputStream.class)));
    }

    @Test
    public void should_add_fragments_to_zip() throws Exception {
        Page page = aPage().with(aFragmentElement().withFragmentId("fragment")).build();
        repository.addFragment(aFragment().id("fragment"));

        step.execute(zipper, page);

        verify(zipper).addToZip(repository.resolveFragmentJson("fragment"), "resources/fragments/fragment/fragment.json");
    }

    public void should_not_add_fragment_metadata_to_zip() throws Exception {
        Page page = aPage().with(aFragmentElement().withFragmentId("fragment")).build();
        repository.addFragment(aFragment().id("fragment"));

        step.execute(zipper, page);

        verify(zipper, never()).addToZip(repository.resolveFragmentMetadata("fragment"), "resources/fragments/fragment/fragment.metadata.json");
    }
}
