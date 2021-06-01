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
import org.bonitasoft.web.designer.controller.export.properties.PagePropertiesBuilder;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PagePropertiesExportStepTest {

    @Mock
    private Zipper zipper;

    @Mock
    private PagePropertiesBuilder pagePropertiesBuilder;

    @InjectMocks
    private PagePropertiesExportStep step;

    @Test
    public void should_add_page_properties_to_zip() throws Exception {
        Page page = aPage().build();
        when(pagePropertiesBuilder.build(page)).thenReturn("foobar".getBytes());

        step.execute(zipper, page);

        verify(zipper).addToZip("foobar".getBytes(), "page.properties");
    }
}
