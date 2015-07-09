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

package org.bonitasoft.web.designer.migration;

import static java.lang.String.format;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.walkFileTree;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.Versioned;

public class LiveMigration {

    private final Watcher watcher;
    private JacksonObjectMapper objectMapper;

    public LiveMigration(Watcher watcher, JacksonObjectMapper objectMapper) {
        this.watcher = watcher;
        this.objectMapper = objectMapper;
    }

    public <A extends Versioned> void start(final Path root, final Class<A> type, final List<Migration<A>> migrationList) throws IOException {

        walkFileTree(root, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                migrate(path.toFile().toPath(), migrationList, type);
                return CONTINUE;
            }
        });

        watcher.watch(root, new FileListener() {

            @Override
            public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
                migrate(watcher.resolve(fileChangeEvent), migrationList, type);
            }

            @Override
            public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
                migrate(watcher.resolve(fileChangeEvent), migrationList, type);
            }

            @Override
            public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
            }
        });
    }

    private <A extends Versioned> void migrate(Path path, List<Migration<A>> migrationList, Class<A> type) {
        if (path.toString().endsWith(".json")) {
            try {
                final A artifact = objectMapper.fromJson(readAllBytes(path), type);
                for (Migration<A> migration : migrationList) {
                    migration.migrate(artifact);
                }
            } catch (IOException e) {
                throw new RuntimeException(format("Error while reading <%s> as a <%s>", path, type), e);
            }
        }
    }
}
