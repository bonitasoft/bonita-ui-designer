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
package org.bonitasoft.web.designer.controller.export.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;


@RunWith(MockitoJUnitRunner.class)
public class FragmentPropertiesBuilderTest {

    private static final String DESIGNER_VERSION = "1.12.1";

    @InjectMocks
    private FragmentPropertiesBuilder fragmentPropertiesBuilder;

    private Fragment fragment;

    @Before
    public void setUp() throws Exception {
        fragment = new Fragment();
        fragment.setName("myFragment");
    }

    @Test
    public void should_build_a_well_formed_page_property_file() throws Exception {
        fragment.setDesignerVersion("1.12.1");

        ReflectionTestUtils.setField(fragmentPropertiesBuilder, "uidVersion", DESIGNER_VERSION);

        byte[] a = fragmentPropertiesBuilder.build(fragment);
        String properties = new String(a);

        assertThat(properties).contains("contentType=fragment");
        assertThat(properties).contains("name=myFragment");
        assertThat(properties).contains("designerVersion=1.12.1");
    }

}
