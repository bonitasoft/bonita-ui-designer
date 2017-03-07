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

public class BuilderFileListener implements PathListener {

    private AbstractLiveFileBuilder builder;

    BuilderFileListener(AbstractLiveFileBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void pathCreated(Path path) throws IOException, URISyntaxException {
        build(path);
    }

    @Override
    public void pathDeleted(Path path) {

    }

    @Override
    public void pathChanged(Path path) throws IOException, URISyntaxException {
        build(path);
    }

    private void build(Path path) throws IOException, URISyntaxException {
        if (builder.isBuildable(valueOf(path))) {
            builder.build(path);
        }
    }
}
