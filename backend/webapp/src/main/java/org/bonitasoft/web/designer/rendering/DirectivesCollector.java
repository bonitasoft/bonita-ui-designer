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

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Benjamin Parisel
 */
@Named
public class DirectivesCollector {


    public static String JS_FOLDER = "js";
    private WorkspacePathResolver pathResolver;
    private DirectiveFileGenerator directiveFileGenerator;


    @Inject
    public DirectivesCollector(WorkspacePathResolver pathResolver, DirectiveFileGenerator directiveFileGenerator) {
        this.pathResolver = pathResolver;
        this.directiveFileGenerator = directiveFileGenerator;
    }

    public List<String> buildUniqueDirectivesFiles(Previewable previewable, String pageId) {
        String filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,
                getDestinationFolderPath(pathResolver.getTmpPagesRepositoryPath().resolve(pageId).resolve(JS_FOLDER)));
        return Arrays.asList(JS_FOLDER + "/" + filename);
    }

    protected Path getDestinationFolderPath(Path path) {
        if (exists(path)) {
            return path;
        }
        try {
            return createDirectories(path);
        } catch (IOException e) {
            throw new GenerationException("Error while create directories " + path.toString(), e);
        }
    }
}
