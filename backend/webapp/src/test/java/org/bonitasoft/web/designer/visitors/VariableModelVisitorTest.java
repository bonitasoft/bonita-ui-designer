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
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.VariableModelVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VariableModelVisitorTest {

    @InjectMocks
    private VariableModelVisitor variableModelVisitor;

    private Variable variable;

    @Before
    public void setUp() throws Exception {
        variable = aConstantVariable().value("bar").build();
    }

    @Test
    public void should_not_retrieve_any_variable_model_when_visiting_a_component() throws Exception {
        assertThat(variableModelVisitor.visit(aComponent().build())).isEmpty();
    }

    @Test
    public void should_retrieve_variable_model_from_page() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withVariable("foo", variable)
                .build();

        assertThat(variableModelVisitor.visit(page)).containsExactly(entry("page-id", singletonMap("foo", variable)));
    }

    @Test
    public void should_generate_a_factory_based_on_model_found_in_the_page() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withVariable("foo", variable)
                .build();

        assertThat(variableModelVisitor.generate(page)).isEqualTo(new TemplateEngine("factory.hbs.js")
                .with("name", "variableModel")
                .with("resources", singletonMap("page-id", singletonMap("foo", variable))).build(this));
    }
}
