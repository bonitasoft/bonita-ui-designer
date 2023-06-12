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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.bonitasoft.web.designer.rendering.GenerationException;

public abstract class AbstractLiveFileBuilder {

    private final Watcher watcher;
    private boolean isLiveBuildEnabled;

    protected AbstractLiveFileBuilder(Watcher watcher, boolean isLiveBuildEnabled) {
        this.watcher = watcher;
        this.isLiveBuildEnabled = isLiveBuildEnabled;
    }

    public void start(final Path root) throws IOException {
        walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                buildIfNeeded(path);
                return CONTINUE;
            }
        });
        if(isLiveBuildEnabled) {
            // now on build on change
            watcher.watch(root, this::buildIfNeeded);
        }
    }

    private void buildIfNeeded(Path path) {
        if (isBuildable(path.toFile().getPath())) {
            try {
                build(path);
            } catch (Exception ex) {
                throw new GenerationException("Build error for " + path.getFileName(), ex);
            }
        }
    }

    public abstract void build(Path path) throws IOException;

    public abstract boolean isBuildable(String path);
}
