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
package org.bonitasoft.web.designer.rendering.angular;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WidgetBundleFile {

    private final WidgetRepository widgetRepository;
    private final WidgetIdVisitor widgetIdVisitor;
    private final WorkspaceProperties workspaceProperties;

    public WidgetBundleFile(WorkspaceProperties workspaceProperties, WidgetRepository widgetRepository,
                            WidgetIdVisitor widgetIdVisitor) {
        this.workspaceProperties= workspaceProperties;
        this.widgetRepository = widgetRepository;
        this.widgetIdVisitor = widgetIdVisitor;
    }

    public List<String> getWidgetsBundlePathUsedInArtifact(Previewable previewable) {
        return widgetRepository.getByIds(widgetIdVisitor.visit(previewable)).stream()
                .filter(widget -> !"pbContainer".equals(widget.getId()))
                .map(this::getBundlePath)
                .filter(this::isBundleFileExist)
                .collect(Collectors.toList());
    }

    private boolean isBundleFileExist(String bundlePath) {
        boolean isBundleFileExist = Files.exists(Paths.get(bundlePath));
        if(!isBundleFileExist) {
            //TODO This error should be manage to avoid broken page generation
            log.error("Widget bundle does not exist for {}", bundlePath);
        }
        return isBundleFileExist;
    }

    private String getBundlePath(Widget widget){
        return this.workspaceProperties.getWidgets().getDir().resolve(widget.getId()).resolve(widget.getJsBundle())
                .toAbsolutePath().toString().replace("\\", "/");
    }
}
