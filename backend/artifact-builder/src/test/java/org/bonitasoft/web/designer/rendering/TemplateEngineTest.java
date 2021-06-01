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
package org.bonitasoft.web.designer.rendering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TemplateEngineTest {

    @Test
    public void should_load_template_from_classpath() throws GenerationException {
        TemplateEngine template = new TemplateEngine("template.html");

        assertThat(template.build(singletonMap("variable", "foobar"))).isEqualTo("<div>foobar</div>");
    }

    @Test
    public void should_allow_using_json_in_templates() throws GenerationException {
        TemplateEngine template = new TemplateEngine("json-template.html");
        Bar bar = new Bar();
        bar.setVariable("qux");

        assertThat(template.build(singletonMap("variable", bar))).isEqualTo("<div>{\"variable\":\"qux\"}</div>");
    }

    @Test
    public void should_allow_using_object_properties_in_templates() throws GenerationException {
        TemplateEngine template = new TemplateEngine("template.html");
        Bar bar = new Bar();
        bar.setVariable("baz");

        assertThat(template.build(bar)).isEqualTo("<div>baz</div>");
    }

    @Test
    public void should_let_add_extra_variable_to_the_template_context() throws GenerationException {
        TemplateEngine template = new TemplateEngine("template.html");

        assertThat(template.with("variable", "bazqux").build(null))
                .isEqualTo("<div>bazqux</div>");
    }

    @Test
    public void should_displayData_when_ifequal_value_is_true() throws GenerationException {
        TemplateEngine template = new TemplateEngine("ifequal-template.html");

        assertThat(template.with("variable", "JAVASCRIPT").build(null)).isEqualTo("JAVASCRIPT");
    }

    @Test
    public void should_not_displayData_when_ifequal_value_is_false() throws GenerationException {
        TemplateEngine template = new TemplateEngine("ifequal-template.html");

        assertThat(template.with("variable", "PAJAVASCRIPT").build(null)).isEmpty();
    }

    class Bar {

        private String variable;

        public void setVariable(String variable) {
            this.variable = variable;
        }

        public String getVariable() {
            return variable;
        }
    }
}
