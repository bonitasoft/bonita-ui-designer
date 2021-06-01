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
package org.bonitasoft.web.designer.workspace;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.Files.*;

/**
 * Copy all file located at the root of a directory to another directory.
 * Don't copy files already located in the target directory
 */
public class CopyContentIfNotExistsVisitor extends SimpleFileVisitor<Path> {

    private final Path targetDirectory;
    private final Path srcDirectory;

    public CopyContentIfNotExistsVisitor(Path srcDirectory, Path targetDirectory) {
        this.targetDirectory = targetDirectory;
        this.srcDirectory = srcDirectory;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        var target = targetDirectory.resolve(srcDirectory.relativize(dir));
        if (!exists(target)) {
            createDirectory(target);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        var target = targetDirectory.resolve(srcDirectory.relativize(file));
        if (!exists(target)) {
            copy(file, target);
        }
        return FileVisitResult.CONTINUE;
    }
}
