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

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.livebuild.BuilderFileListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LanguagePackBuilderTest {

    @Mock
    Watcher watcher;

    LanguagePackBuilder builder;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        builder = new LanguagePackBuilder(watcher, new LanguagePackFactory(
                new PoParser(), new JacksonObjectMapper(new ObjectMapper())));
    }

    @Test
    public void should_build_all_language_pack_under_provided_directory() throws Exception {
        File frFile = folder.newFile("fr.po");
        folder.newFolder("i18n");
        File enFile = folder.newFile("i18n/en.po");
        write(frFile.toPath(), aSimplePoFile());
        write(enFile.toPath(), aSimplePoFile());

        builder.start(folder.getRoot().toPath());

        assertThat(resolveJson(frFile).exists()).isTrue();
        assertThat(resolveJson(enFile).exists()).isTrue();
    }

    @Test
    public void should_only_build_directives_files() throws Exception {
        Path path = folder.getRoot().toPath();

        builder.start(path);

        verify(watcher).watch(eq(path), any(BuilderFileListener.class));
    }

    @Test
    public void should_ignore_files_which_are_not_po_files() throws Exception {
        File poFile = folder.newFile("fr.po");
        folder.newFile("script.js");
        write(poFile.toPath(), aSimplePoFile());

        builder.start(folder.getRoot().toPath());

        assertThat(folder.getRoot().list()).containsOnly("fr.po", "fr.json", "script.js");
    }

    @Test
    public void should_replace_a_previous_build_with_new_one() throws Exception {
        File poFile = folder.newFile("file.po");
        File jsonFile = new File(poFile.getPath().replace(".po", ".json"));
        write(poFile.toPath(), aSimplePoFile());
        write(jsonFile.toPath(), "previous content".getBytes());

        builder.build(poFile.toPath());

        assertThat(read(jsonFile)).isEqualTo("{\"francais\":{\"A page\":\"Une page\"}}");
    }

    private String read(File file) throws IOException {
        return new String(readAllBytes(file.toPath()));
    }

    public File resolveJson(File poFile) {
        return new File(poFile.getPath().replace(".po", ".json"));
    }

    private byte[] aSimplePoFile() throws Exception {
        return readAllBytes(Paths.get(getClass().getResource("/i18n/simple.po").toURI()));
    }
}
