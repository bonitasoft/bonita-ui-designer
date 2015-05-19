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
package org.bonitasoft.web.designer.visitors;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.DataBuilder.aConstantData;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.DataModelVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataModelVisitorTest {

    @InjectMocks
    private DataModelVisitor dataModelVisitor;

    private Data data;

    @Before
    public void setUp() throws Exception {
        data = aConstantData().value("bar").build();
    }

    @Test
    public void should_not_retrieve_any_data_model_when_visiting_a_component() throws Exception {
        assertThat(dataModelVisitor.visit(aComponent().build())).isEmpty();
    }

    @Test
    public void should_retrieve_data_model_from_page() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withData("foo", data)
                .build();

        assertThat(dataModelVisitor.visit(page)).containsExactly(entry("page-id", singletonMap("foo", data)));
    }

    @Test
    public void should_generate_a_factory_based_on_model_found_in_the_page() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withData("foo", data)
                .build();

        assertThat(dataModelVisitor.generate(page)).isEqualTo(new TemplateEngine("factory.hbs.js")
                .with("name", "dataModel")
                .with("resources", singletonMap("page-id", singletonMap("foo", data))).build(this));
    }
}
