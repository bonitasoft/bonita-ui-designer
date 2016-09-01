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
package org.bonitasoft.web.designer.service;

import static java.util.Collections.singletonList;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetServiceTest {

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private BondsTypesFixer bondsTypesFixer;

    @InjectMocks
    private WidgetService widgetService;

    @Before
    public void setUp() throws Exception {
        widgetService = new WidgetService(widgetRepository, singletonList(bondsTypesFixer));
    }

    @Test
    public void should_fix_bonds_types_on_save() {
        Property constantTextProperty = aProperty().name("text").bond(CONSTANT).build();
        Property interpolationTextProperty = aProperty().name("text").bond(INTERPOLATION).build();
        Widget persistedWidget = aWidget().id("labelWidget").property(constantTextProperty).build();
        when(widgetRepository.get("labelWidget")).thenReturn(persistedWidget);

        widgetService.updateProperty("labelWidget", "text", interpolationTextProperty);

        verify(bondsTypesFixer).fixBondsTypes("labelWidget", singletonList(interpolationTextProperty));
    }
}