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

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.repository.RefreshingRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.apache.commons.lang3.StringUtils.contains;


public class LiveRepositoryUpdate<A extends DesignerArtifact> implements Comparable<LiveRepositoryUpdate<A>> {

    private final Repository<A> repository;
    private final List<Migration<A>> migrationList;

    public LiveRepositoryUpdate(Repository<A> repository, List<Migration<A>> migrationList) {
        this.repository = repository;
        this.migrationList = migrationList;
    }

    public void start() throws IOException {

        repository.walk(new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                refresh(repository, path.toFile().toPath());
                return CONTINUE;
            }
        });

        repository.watch(path -> refresh(repository, path));
    }

    public void migrate() throws IOException {
        repository.walk(new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (isArtifactDescriptor(path)) {
                    final var artifact = repository.get(path);
                    String formerArtifactVersion = artifact.getArtifactVersion();
                    for (Migration<A> migration : migrationList) {
                        migration.migrate(artifact);
                    }
                    if (!StringUtils.equals(formerArtifactVersion, artifact.getArtifactVersion())) {
                        artifact.setPreviousArtifactVersion(formerArtifactVersion);
                        repository.updateLastUpdateAndSave(artifact);
                    }
                }
                return CONTINUE;
            }
        });
    }

    private void refresh(Repository<A> repository, Path path) {
        if (repository instanceof RefreshingRepository && isArtifactDescriptor(path)) {
            final var page = repository.get(path);
            ((RefreshingRepository) repository).refresh(page.getId());
        }
    }


    private boolean isArtifactDescriptor(Path path) {
        return path.toString().endsWith(".json") &&
                !contains(path.toString(), File.separator + ".metadata" + File.separator)
                && !contains(path.toString(), File.separator + "assets" + File.separator);
    }

    @Override
    public int compareTo(LiveRepositoryUpdate o) {
        return repository instanceof WidgetRepository ? -1 : 1;
    }
}
