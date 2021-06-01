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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragmentDependencyImporterTest {

    @Mock
    private FragmentRepository fragmentRepository;
    @InjectMocks
    private FragmentDependencyImporter fragmentImporterDependencyImporter;

    @Test
    public void should_verify_that_a_fragment_exists_in_repository() throws Exception {
        when(fragmentRepository.exists("existingFragment")).thenReturn(true);

        boolean exists = fragmentImporterDependencyImporter.exists(aFragment().withId("existingFragment").build());

        assertThat(exists).isTrue();
    }

    @Test
    public void should_verify_that_a_widget_does_not_exists_in_repository() throws Exception {
        when(fragmentRepository.exists("unknownFragment")).thenReturn(false);

        boolean exists = fragmentImporterDependencyImporter.exists(aFragment().withId("unknownFragment").build());

        assertThat(exists).isFalse();
    }
}
