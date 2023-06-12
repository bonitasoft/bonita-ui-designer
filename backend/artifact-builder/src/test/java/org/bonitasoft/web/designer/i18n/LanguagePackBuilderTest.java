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
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.PathListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LanguagePackBuilderTest {

    @Mock
    private Watcher watcher;

    private LanguagePackBuilder builder;
    private WorkspaceUidProperties workspaceUidProperties;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        newLanguagePackBuilder(true);
    }

    private void newLanguagePackBuilder(boolean liveBuildEnabled) {
        workspaceUidProperties = new WorkspaceUidProperties();
        workspaceUidProperties.setPath(folder.getRoot().toPath());
        workspaceUidProperties.setLiveBuildEnabled(liveBuildEnabled);
        builder = new LanguagePackBuilder(watcher, new LanguagePackFactory(
                new PoParser(), new JacksonJsonHandler(new ObjectMapper())), workspaceUidProperties);
    }

    @Test
    public void should_build_all_language_pack_under_provided_directory() throws Exception {
        File frFile = folder.newFile("fr.po");
        folder.newFolder("appClassPath");
        File enFile = folder.newFile("appClassPath/en.po");
        write(frFile.toPath(), aSimplePoFile());
        write(enFile.toPath(), aSimplePoFile());

        builder.start(folder.getRoot().toPath());
        assertThat(workspaceUidProperties.getTmpI18nPath().resolve("fr.json")).exists();
        assertThat(resolveJson(frFile)).exists();
        assertThat(resolveJson(enFile)).exists();
    }

    @Test
    public void should_watch_directives_files() throws Exception {
        Path path = workspaceUidProperties.getTmpI18nPath();

        builder.start(path);

        verify(watcher).watch(eq(path), any(PathListener.class));
    }
    
    @Test
    public void should_not_watch_directives_files() throws Exception {
        newLanguagePackBuilder(false);
        Path path = workspaceUidProperties.getTmpI18nPath();

        builder.start(path);

        verify(watcher, never()).watch(eq(path), any(PathListener.class));
    }

    @Test
    public void should_ignore_files_which_are_not_po_files() throws Exception {
        File poFile = folder.newFile("fr.po");
        folder.newFile("script.js");
        write(poFile.toPath(), aSimplePoFile());

        builder.start(folder.getRoot().toPath());

        List<String> jsonFiles = Files.walk(workspaceUidProperties.getTmpI18nPath()).filter(Files::isRegularFile).map(entry -> entry.getFileName().toString()).collect(Collectors.toList());
        assertThat(jsonFiles).containsOnly("fr.json");
    }

    @Test
    public void should_replace_a_previous_build_with_new_one() throws Exception {
        File poFile = folder.newFile("file.po");
        write(poFile.toPath(), aSimplePoFile());

        builder.build(poFile.toPath());

        assertThat(read(workspaceUidProperties.getTmpI18nPath().resolve("file.json").toFile())).isEqualTo("{\"francais\":{\"A page\":\"Une page\"}}");
    }

    private String read(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public File resolveJson(File poFile) throws IOException {
        return new File(workspaceUidProperties.getTmpI18nPath().resolve(poFile.getName().replace(".po", ".json")).toString());
    }

    private byte[] aSimplePoFile() throws Exception {
        return readAllBytes(Paths.get(getClass().getResource("/i18n/simple.po").toURI()));
    }
}
