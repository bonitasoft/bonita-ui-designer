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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * @author Benjamin Parisel
 */
@Named
public class DirectivesCollector {


    public static String JS_FOLDER = "js";
    private WorkspacePathResolver pathResolver;
    private DirectiveFileGenerator directiveFileGenerator;
    private final FragmentRepository fragmentRepository;
    private final FragmentIdVisitor fragmentIdVisitor;


    @Inject
    public DirectivesCollector(WorkspacePathResolver pathResolver, DirectiveFileGenerator directiveFileGenerator,
                               FragmentIdVisitor fragmentIdVisitor, FragmentRepository fragmentRepository) {
        this.pathResolver = pathResolver;
        this.directiveFileGenerator = directiveFileGenerator;
        this.fragmentRepository = fragmentRepository;
        this.fragmentIdVisitor = fragmentIdVisitor;
    }

    public List<String> buildUniqueDirectivesFiles(Previewable previewable, String pageId) {
        if (previewable instanceof Fragment) {
            String filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable, getDestinationFolderPath(pathResolver.getTmpFragmentsRepositoryPath().resolve(pageId)));
            return Arrays.asList(filename);
        } else {
            String filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,
                    getDestinationFolderPath(pathResolver.getTmpPagesRepositoryPath().resolve(pageId).resolve(JS_FOLDER)));
            return newArrayList(concat(Arrays.asList(JS_FOLDER + "/" + filename), collectFragment(previewable)));
        }
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

    private List<String> collectFragment(Previewable previewable) {
        return fragmentRepository.getByIds(fragmentIdVisitor.visit(previewable)).stream()
                .map(fragment -> format("fragments/%s/%s.js", fragment.getId(), fragment.getId()))
                .collect(Collectors.toList());
    }
}
