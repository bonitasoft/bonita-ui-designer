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

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.controller.export.properties.FragmentPropertiesBuilder;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FragmentPropertiesExportStepTest {

    @Mock
    private Zipper zipper;

    @Mock
    private FragmentPropertiesBuilder fragmentPropertiesBuilder;

    @InjectMocks
    private FragmentPropertiesExportStep step;

    @Test
    public void should_add_page_properties_to_zip() throws Exception {
        Fragment fragment = aFragment().build();
        when(fragmentPropertiesBuilder.build(fragment)).thenReturn("foobar".getBytes());

        step.execute(zipper, fragment);

        verify(zipper).addToZip("foobar".getBytes(), "fragment.properties");
    }
}
