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

import org.bonitasoft.web.designer.controller.export.IncludeChildDirectoryPredicate;
import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;

import java.io.IOException;
import java.nio.file.Path;

import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;

public class FragmentsExportStep <T extends AbstractPage> implements ExportStep<T> {

    private final FragmentIdVisitor fragmentIdVisitor;
    private final Path fragmentsPath;

    public FragmentsExportStep(FragmentIdVisitor fragmentIdVisitor, Path fragmentsPath) {
        this.fragmentIdVisitor = fragmentIdVisitor;
        this.fragmentsPath = fragmentsPath;
    }

    @Override
    public void execute(Zipper zipper, T artifact) throws IOException {
        zipper.addDirectoryToZip(
                fragmentsPath,
                new IncludeChildDirectoryPredicate(fragmentsPath, fragmentIdVisitor.visit(artifact)),
                ALL_FILES,
                RESOURCES + "/fragments");
    }
}
