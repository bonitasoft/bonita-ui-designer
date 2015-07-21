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

import static java.nio.file.Paths.get;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import javax.inject.Named;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.local.LocalFile;

@Named
public class Watcher {

    public void watch(Path path, FileListener fileListener) throws FileSystemException {
        final DefaultFileMonitor monitor = new DefaultFileMonitor(fileListener);
        monitor.setRecursive(true);
        monitor.addFile(VFS.getManager().resolveFile(path.toUri().toString()));
        monitor.start();
    }

    public Path resolve(FileChangeEvent fileChangeEvent) throws FileSystemException, URISyntaxException {
        return get(new URI(UriParser.encode(fileChangeEvent.getFile().getName().getFriendlyURI(), new char[]{' '})));
    }
}
