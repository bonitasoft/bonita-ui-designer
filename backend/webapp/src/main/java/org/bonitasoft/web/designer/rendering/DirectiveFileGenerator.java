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
package org.bonitasoft.web.designer.rendering;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

/**
 * @author Benjamin Parisel
 */
public class DirectiveFileGenerator {

    private WorkspacePathResolver pathResolver;
    private WidgetRepository widgetRepository;
    private WidgetIdVisitor widgetIdVisitor;

    @Inject
    public DirectiveFileGenerator(WorkspacePathResolver pathResolver, WidgetRepository widgetRepository,
                WidgetIdVisitor widgetIdVisitor) {
        this.pathResolver = pathResolver;
        this.widgetRepository = widgetRepository;
        this.widgetIdVisitor = widgetIdVisitor;
    }

    public List<Path> getWidgetsFilesUsedInPage(Previewable previewable) {
        return widgetRepository.getByIds(widgetIdVisitor.visit(previewable)).stream()
                .filter(widget -> !"pbContainer".equals(widget.getId()))
                .map(w -> Paths.get(w.getId()).resolve(w.getId()+".js"))
                .map(file -> pathResolver.getWidgetsRepositoryPath().resolve(file))
                .collect(Collectors.toList());
    }

    public byte[] concatenate(List<Path> paths) {
        return FilesConcatenator.concat(paths);
    }

    public byte[] minify(byte[] content) {
        return Minifier.minify(content);
    }

    public String generateAllDirectivesFilesInOne(Previewable previewable, Path path) {
        List<Path> filename = getWidgetsFilesUsedInPage(previewable);
        byte[] content = concatenate(filename);
        content = minify(content);
        String fileHash = DigestUtils.sha1Hex(content);
        WidgetFileHelper.deleteOldConcatenateFiles(path, fileHash);
        Path file = WidgetFileHelper.writeFile(content, path, fileHash + ".min");
        return file.getFileName().toString();
    }
}
