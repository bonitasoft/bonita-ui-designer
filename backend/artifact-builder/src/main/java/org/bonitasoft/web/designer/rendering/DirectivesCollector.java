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
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;

/**
 * @author Benjamin Parisel
 */
public class DirectivesCollector {

    public static final String JS_FOLDER = "js";

    private static final Logger logger = LoggerFactory.getLogger(DirectivesCollector.class);
    private final JsonHandler jsonHandler;

    private final FragmentRepository fragmentRepository;
    private final FragmentIdVisitor fragmentIdVisitor;
    private final WorkspaceUidProperties workspaceUidProperties;
    private final DirectiveFileGenerator directiveFileGenerator;

    public DirectivesCollector(JsonHandler jsonHandler, WorkspaceUidProperties workspaceUidProperties, DirectiveFileGenerator directiveFileGenerator,
                               FragmentIdVisitor fragmentIdVisitor, FragmentRepository fragmentRepository) {
        this.jsonHandler = jsonHandler;
        this.workspaceUidProperties = workspaceUidProperties;
        this.directiveFileGenerator = directiveFileGenerator;
        this.fragmentRepository = fragmentRepository;
        this.fragmentIdVisitor = fragmentIdVisitor;
    }

    public List<String> buildUniqueDirectivesFiles(Previewable previewable, String pageId) {
        if (previewable instanceof Fragment) {
            var filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable, getDestinationFolderPath(workspaceUidProperties.getTmpFragmentsRepositoryPath().resolve(pageId)));
            return List.of(filename);
        } else {
            var filename = directiveFileGenerator.generateAllDirectivesFilesInOne(previewable,
                    getDestinationFolderPath(workspaceUidProperties.getTmpPagesRepositoryPath().resolve(pageId).resolve(JS_FOLDER)));
            var directives = new ArrayList<String>();
            directives.add(JS_FOLDER + "/" + filename);
            directives.addAll(collectFragment(previewable));
            return directives;
        }
    }

    protected Path getDestinationFolderPath(Path path) {
        if (exists(path)) {
            return path;
        }
        try {
            return createDirectories(path);
        } catch (IOException e) {
            throw new GenerationException("Error while create directories " + path, e);
        }
    }

    private List<String> collectFragment(Previewable previewable) {
        return fragmentRepository.getByIds(fragmentIdVisitor.visit(previewable)).stream()
                .map(fragment -> format("fragments/%s/%s.js?hash=%s", fragment.getId(), fragment.getId(), getHash(fragment)))
                .collect(Collectors.toList());
    }

    private String getHash(Fragment fragment) {
        try {
            byte[] content = this.jsonHandler.toJson(fragment);
            return DigestUtils.sha1Hex(content);
        } catch (Exception e) {
            logger.warn("Failure to generate hash for fragment " + fragment.getName(), e);
            return UUID.randomUUID().toString();
        }
    }
}
