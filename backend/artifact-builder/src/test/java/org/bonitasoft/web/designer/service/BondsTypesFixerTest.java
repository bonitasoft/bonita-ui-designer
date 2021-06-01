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

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BondsTypesFixerTest {

    @Mock
    public PageRepository pageRepository;

    @InjectMocks
    public BondsTypesFixer bondsTypesFixer;

    @Test
    public void should_fix_bonds_types_in_all_pages() throws Exception {
        Component labelComponent = aComponent("labelComponent").withPropertyValue("text", "constant", "").build();
        Page page = aPage().with(labelComponent).build();
        when(pageRepository.findByObjectId("labelWidget")).thenReturn(singletonList(page));

        bondsTypesFixer.fixBondsTypes("labelWidget", singletonList(
                aProperty().name("text").bond(INTERPOLATION).build()));

        assertThat(labelComponent.getPropertyValues().get("text").getType()).isEqualTo(INTERPOLATION.toJson());
        verify(pageRepository).save(page);
    }
}
