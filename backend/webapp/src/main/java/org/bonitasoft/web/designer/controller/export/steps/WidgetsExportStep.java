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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.controller.export.IncludeChildDirectoryPredicate;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.DirectiveFileGenerator;
import org.bonitasoft.web.designer.rendering.Minifier;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

public class WidgetsExportStep<T extends AbstractPage> implements ExportStep<T> {

    private WorkspacePathResolver pathResolver;
    private WidgetIdVisitor widgetIdVisitor;
    private DirectiveFileGenerator directiveFileGenerator;

    @Inject
    public WidgetsExportStep(WorkspacePathResolver pathResolver, WidgetIdVisitor widgetIdVisitor,
                             DirectiveFileGenerator directiveFileGenerator) {
        this.pathResolver = pathResolver;
        this.widgetIdVisitor = widgetIdVisitor;
        this.directiveFileGenerator = directiveFileGenerator;
    }

    @Override
    public void execute(Zipper zipper, T page) throws IOException {
        Path widgetsRepositoryPath = pathResolver.getWidgetsRepositoryPath();
        zipper.addDirectoryToZip(
                widgetsRepositoryPath,
                new IncludeChildDirectoryPredicate(widgetsRepositoryPath, widgetIdVisitor.visit(page)),
                ALL_FILES,
                RESOURCES + "/widgets");

        // Export widgets.js
        List<Path> files = directiveFileGenerator.getWidgetsFilesUsedInPage(page);
        byte[] content = directiveFileGenerator.concatenate(files);
        content = Minifier.minify(content);
        zipper.addToZip(content, RESOURCES + "/js/widgets-" + DigestUtils.sha1Hex(content) + ".min.js");
    }
}
