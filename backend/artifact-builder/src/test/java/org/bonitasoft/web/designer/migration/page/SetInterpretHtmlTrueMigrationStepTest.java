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

package org.bonitasoft.web.designer.migration.page;

import org.bonitasoft.web.designer.builder.RowBuilder;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.*;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;

@RunWith(MockitoJUnitRunner.class)
public class SetInterpretHtmlTrueMigrationStepTest {

    @Mock
    private FragmentRepository fragmentRepository;

    SetInterpretHtmlTrueMigrationStep<Page> setInterpretHtmlTrueMigrationStep;

    @Before
    public void setUp() throws Exception {
        setInterpretHtmlTrueMigrationStep = new SetInterpretHtmlTrueMigrationStep<>(new ComponentVisitor(fragmentRepository));
    }

    @Test
    public void should_migrate_page_with_added_AllowHTML() throws Exception {
        RowBuilder row = aRow().with(aComponent("pbButton"));
        Page pageWithButton = aPage().withId("pageWithButton").withModelVersion("2.2").with(row.build().toArray(new Element[0])).build();

        setInterpretHtmlTrueMigrationStep.migrate(pageWithButton);

        checkAllowHtmlTrue(pageWithButton);
    }

    @Test
    public void should_migrate_fragment_with_added_AllowHTML() throws Exception {
        RowBuilder row = aRow().with(aComponent("pbCheckbox"));
        Fragment fragmentWithCheckbox = aFragment().withId("fragmentWithCheckbox").withModelVersion("2.2").with(row.build().toArray(new Element[0])).build();

        setInterpretHtmlTrueMigrationStep.migrate(fragmentWithCheckbox);

        checkAllowHtmlTrue(fragmentWithCheckbox);
    }

    private void checkAllowHtmlTrue(AbstractPage abstractPage) {
        Map<String, PropertyValue> propertyValues  = abstractPage.getRows().get(0).get(0).getPropertyValues();
        assertThat(propertyValues.containsKey("allowHTML")).isTrue();
        assertThat(propertyValues.get("allowHTML").getValue()).isEqualTo(Boolean.TRUE);
    }
}
