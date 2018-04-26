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

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.apache.commons.lang3.StringUtils.contains;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AbstractLoader;
import org.bonitasoft.web.designer.repository.RefreshingRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetRepository;


public class LiveRepositoryUpdate<A extends DesignerArtifact> implements Comparable<LiveRepositoryUpdate> {

    private AbstractLoader<A> loader;
    private Repository<A> repository;
    private final List<Migration<A>> migrationList;

    public LiveRepositoryUpdate(Repository<A> repository,
                                AbstractLoader<A> loader,
                                List<Migration<A>> migrationList) {
        this.loader = loader;
        this.repository = repository;
        this.migrationList = migrationList;
    }

    public void start() throws IOException {

        repository.walk(new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                refresh(repository, path.toFile().toPath());
                return CONTINUE;
            }
        });

        repository.watch(path -> refresh(repository, path));
    }

    public void migrate() throws IOException {
        repository.walk(new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (isArtifactDescriptor(path)) {
                    final A artifact = loader.get(path);
                    String formerArtifactVersion = artifact.getDesignerVersion();
                    for (Migration<A> migration : migrationList) {
                        migration.migrate(artifact);
                    }
                    if (!StringUtils.equals(formerArtifactVersion, artifact.getDesignerVersion())) {
                        artifact.setPreviousDesignerVersion(formerArtifactVersion);
                        repository.updateLastUpdateAndSave(artifact);
                    }
                }
                return CONTINUE;
            }
        });

    }

    private void refresh(Repository<A> repository, Path path) {
        if (repository instanceof RefreshingRepository && isArtifactDescriptor(path)) {
            final A page = loader.get(path);
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
        return  repository instanceof WidgetRepository ? -1 : 1;
    }
}
