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

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Benjamin Parisel
 */
public class DirectiveFileGenerator {

    private final Path widgetPath;
    private final WidgetRepository widgetRepository;
    private final WidgetIdVisitor widgetIdVisitor;

    public DirectiveFileGenerator(WorkspaceProperties workspaceProperties, WidgetRepository widgetRepository,
                                  WidgetIdVisitor widgetIdVisitor) {
        this.widgetPath = workspaceProperties.getWidgets().getDir();
        this.widgetRepository = widgetRepository;
        this.widgetIdVisitor = widgetIdVisitor;
    }

    public List<Path> getWidgetsFilesUsedInPage(Previewable previewable) {
        return widgetRepository.getByIds(widgetIdVisitor.visit(previewable)).stream()
                .filter(widget -> !"pbContainer".equals(widget.getId()))
                .map(w -> Paths.get(w.getId()).resolve(w.getId() + ".js"))
                .map(widgetPath::resolve)
                .collect(Collectors.toList());
    }

    public byte[] concatenate(List<Path> paths) {
        return FilesConcatenator.concat(paths);
    }

    public byte[] minify(byte[] content) {
        return Minifier.minify(content);
    }

    public String generateAllDirectivesFilesInOne(Previewable previewable, Path path) {
        var filename = getWidgetsFilesUsedInPage(previewable);
        byte[] content = concatenate(filename);
        content = minify(content);
        String fileHash = DigestUtils.sha1Hex(content);
        WidgetFileHelper.deleteOldConcatenateFiles(path, fileHash);
        Path file = WidgetFileHelper.writeFile(content, path, fileHash + ".min");
        return file.getFileName().toString();
    }
}
