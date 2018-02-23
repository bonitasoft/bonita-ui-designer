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
package org.bonitasoft.web.designer.model.widget;

import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyTypeTest {

    @Test
    public void should_use_english_language_to_convert_to_uppercase_when_reading_JSON() throws Exception {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));

            PropertyType choicePropertyType = PropertyType.fromJson("choice");

            assertThat(choicePropertyType).isEqualTo(PropertyType.CHOICE);
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void should_use_english_language_to_convert_to_lowercase_when_generating_JSON() throws Exception {
        Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            PropertyType choicePropertyType = PropertyType.CHOICE;

            String choicePropertyTypeString = choicePropertyType.toJson();

            assertThat(choicePropertyTypeString).isEqualTo("choice");
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }
}
