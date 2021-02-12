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

package org.bonitasoft.web.designer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.controller.Predicates.propertyEqualTo;

import org.junit.Test;

public class PredicatesTest {

    class Person {

        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void should_return_true_when_property_values_are_equal() throws Exception {
        assertThat(propertyEqualTo("name", "Vincent").apply(new Person("Vincent"))).isTrue();
    }

    @Test
    public void should_return_false_when_property_values_are_not_equal() throws Exception {
        assertThat(propertyEqualTo("name", "Vincent").apply(new Person("Colin"))).isFalse();
    }

    @Test
    public void should_return_false_when_property_does_not_exist() throws Exception {
        assertThat(propertyEqualTo("age", 28).apply(new Person("Vincent"))).isFalse();
    }
}
