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
package org.bonitasoft.web.designer.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;

public class LanguagePackTest {

    @Rule
    public TemporaryFolder directory = new TemporaryFolder();

    private LanguagePackFactory languagePackFactory;

    @Before
    public void setUp() throws Exception {
        languagePackFactory = new LanguagePackFactory(new PoParser(), new JacksonJsonHandler(new ObjectMapper()));
    }

    @Test
    public void should_convert_translation_into_json() throws Exception {
        File poFile = directory.newFile();
        write(poFile.toPath(), readResource("/i18n/simple.po"));

        assertThat(new String(languagePackFactory.create(poFile).toJson()))
                .isEqualTo("{\"francais\":{\"A page\":\"Une page\"}}");
    }

    @Test
    public void should_convert_plural_translations_into_json() throws Exception {
        File poFile = directory.newFile();
        write(poFile.toPath(), readResource("/i18n/plural.po"));

        assertThat(new String(languagePackFactory.create(poFile).toJson()))
                .isEqualTo("{\"francais\":{\"A page\":[\"Une page\",\"Des pages\"]}}");
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_a_runtime_exception_if_the_po_file_does_not_contains_the_language() throws Exception {
        File poFile = directory.newFile();
        write(poFile.toPath(), new String(readResource("/i18n/simple.po")).replace("Language: francais", "").getBytes());

        assertThat(new String(languagePackFactory.create(poFile).toJson()))
                .isEqualTo("{\"francais\":{\"A page\":[\"Une page\",\"Des pages\"]}}");
    }

    private byte[] readResource(String path) throws Exception {
        return readAllBytes(Paths.get(getClass().getResource(path).toURI()));
    }
}
