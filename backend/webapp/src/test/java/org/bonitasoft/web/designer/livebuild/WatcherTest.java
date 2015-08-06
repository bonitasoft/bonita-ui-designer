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
package org.bonitasoft.web.designer.livebuild;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.VFS;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WatcherTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    Watcher watcher = new Watcher();

    @Test
    public void should_resolve_file_path() throws Exception {
        folder.newFolder("repertoire vers un");
        File file = folder.newFile("repertoire vers un/fichier quelconque.txt");

        FileChangeEvent fileChangeEvent = new FileChangeEvent(VFS.getManager().resolveFile(file.getPath()));

        assertThat(watcher.resolve(fileChangeEvent).toString()).isEqualTo(file.getPath());
    }

    @Test
    @Ignore("Test ignored until we decide to support path with URI-not-supported characters other than spaces")
    public void should_resolve_file_path_with_utf8_chars() throws Exception {
        folder.newFolder("répertoire vers un");
        File file = folder.newFile("répertoire vers un/fichier quelconque.txt");

        FileChangeEvent fileChangeEvent = new FileChangeEvent(VFS.getManager().resolveFile(file.getPath()));

        assertThat(watcher.resolve(fileChangeEvent).toString()).isEqualTo(file.getPath());
    }
}
