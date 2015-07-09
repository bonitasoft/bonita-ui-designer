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

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.VFS;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BuilderFileListenerTest {

    @Mock
    private AbstractLiveFileBuilder builder;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_call_build_on_file_change() throws Exception {
        given(builder.isBuildable(anyString())).willReturn(true);
        BuilderFileListener builderFileListener = new BuilderFileListener(builder, new Watcher());
        File file = folder.newFile();

        builderFileListener.fileChanged(new FileChangeEvent(VFS.getManager().resolveFile(file.getPath())));

        verify(builder).build(file.toPath());
    }

    @Test
    public void should_not_call_build_on_a_changing_file_filtered_by_the_extension_passed_through() throws Exception {
        given(builder.isBuildable(anyString())).willReturn(false);
        BuilderFileListener builderFileListener = new BuilderFileListener(builder, new Watcher());
        File file = folder.newFile("test - file.js");

        builderFileListener.fileChanged(new FileChangeEvent(VFS.getManager().resolveFile(file.getPath())));

        verify(builder, never()).build(any(Path.class));
    }

    @Test
    public void should_call_build_on_file_creation() throws Exception {
        given(builder.isBuildable(anyString())).willReturn(true);
        BuilderFileListener builderFileListener = new BuilderFileListener(builder, new Watcher());
        File file = folder.newFile("test - file.js");

        builderFileListener.fileChanged(new FileChangeEvent(VFS.getManager().resolveFile(file.getPath())));

        verify(builder).build(file.toPath());
    }

    @Test
    public void should_not_call_build_on_created_file_filtered_by_the_extension_passed_through() throws Exception {
        given(builder.isBuildable(anyString())).willReturn(false);
        BuilderFileListener builderFileListener = new BuilderFileListener(builder, new Watcher());
        File file = folder.newFile("test - file.js");

        builderFileListener.fileCreated(new FileChangeEvent(VFS.getManager().resolveFile(file.getPath())));

        verify(builder, never()).build(any(Path.class));
    }
}
