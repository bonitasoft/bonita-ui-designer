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

import static java.lang.String.valueOf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

public class BuilderFileListener implements FileListener {

    private AbstractLiveFileBuilder builder;
    private Watcher watcher;

    BuilderFileListener(AbstractLiveFileBuilder builder, Watcher watcher) {
        this.builder = builder;
        this.watcher = watcher;
    }

    @Override
    public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
        build(fileChangeEvent);
    }

    @Override
    public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
    }

    @Override
    public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
        build(fileChangeEvent);
    }

    private void build(FileChangeEvent fileChangeEvent) throws IOException, URISyntaxException {
        Path path = watcher.resolve(fileChangeEvent);
        if (builder.isBuildable(valueOf(path))) {
            builder.build(path);
        }
    }
}
