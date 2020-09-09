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
import javax.inject.Inject;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.controller.export.IncludeChildDirectoryPredicate;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

public class FragmentsExportStep<T extends AbstractPage> implements ExportStep<T> {

    private FragmentIdVisitor fragmentIdVisitor;
    private WorkspacePathResolver pathResolver;
    private FragmentPropertiesExportStep fragmentPropertiesExportStep;

    @Inject
    public FragmentsExportStep(FragmentIdVisitor fragmentIdVisitor, WorkspacePathResolver pathResolver, FragmentPropertiesExportStep fragmentPropertiesExportStep) {
        this.fragmentIdVisitor = fragmentIdVisitor;
        this.pathResolver = pathResolver;
        this.fragmentPropertiesExportStep = fragmentPropertiesExportStep;
    }

    @Override
    public void execute(Zipper zipper, T page) throws IOException {
        Path fragmentsRepositoryPath = pathResolver.getFragmentsRepositoryPath();
        if (page instanceof Fragment) {
            fragmentPropertiesExportStep.execute(zipper, (Fragment) page);
        }

        zipper.addDirectoryToZip(
                fragmentsRepositoryPath,
                new IncludeChildDirectoryPredicate(fragmentsRepositoryPath, fragmentIdVisitor.visit(page)),
                ALL_FILES,
                RESOURCES + "/fragments");
    }
}
