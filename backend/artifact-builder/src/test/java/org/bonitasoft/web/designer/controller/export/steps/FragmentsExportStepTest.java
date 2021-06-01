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

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.controller.export.IncludeChildDirectoryPredicate;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFragmentRepository;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.OutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FragmentsExportStepTest {

    private WorkspaceProperties workspaceProperties = new WorkspaceProperties();

    @Rule
    public TemporaryFragmentRepository repositoryFactory = new TemporaryFragmentRepository(workspaceProperties);

    private FragmentRepository fragmentRepository;

    private FragmentsExportStep step;

    @Mock
    private Zipper zipper;

    @Before
    public void beforeEach() {
        fragmentRepository = repositoryFactory.toRepository();
        step = new FragmentsExportStep(
                new FragmentIdVisitor(fragmentRepository),
                workspaceProperties.getFragments().getDir());
        zipper = spy(new Zipper(mock(OutputStream.class)));
    }

    @Test
    public void should_add_fragments_to_zip() throws Exception {
        Fragment fragment = aFragment().withId("fragment").build();
        fragmentRepository.save(fragment);

        step.execute(zipper, fragment);

        ArgumentCaptor<Path> importPathCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<IncludeChildDirectoryPredicate> directoryPredicateCaptor = ArgumentCaptor.forClass(IncludeChildDirectoryPredicate.class);
        ArgumentCaptor<String> destinationDirectoryNameCaptor = ArgumentCaptor.forClass(String.class);

        verify(zipper).addDirectoryToZip(importPathCaptor.capture(), directoryPredicateCaptor.capture(), eq(ALL_FILES), destinationDirectoryNameCaptor.capture());

        assertThat(importPathCaptor.getValue()).isEqualTo(repositoryFactory.toPath());
        assertThat(directoryPredicateCaptor.getValue().getSourceDirectory()).isEqualTo(repositoryFactory.toPath());
        assertThat(destinationDirectoryNameCaptor.getValue()).isEqualTo("resources/" + fragmentRepository.getComponentName() + "s");

    }

    @Test
    public void should_not_add_fragment_metadata_to_zip() throws Exception {
        Fragment fragment = aFragment().withId("fragment").build();
        fragmentRepository.save(fragment);

        step.execute(zipper, fragment);

        verify(zipper, never()).addToZip(repositoryFactory.resolveFragmentMetadata("fragment"), "resources/fragments/fragment/fragment.metadata.json");
    }

}
