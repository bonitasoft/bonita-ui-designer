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

package org.bonitasoft.web.designer.workspace;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.livebuild.AbstractLiveFileBuilder;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;

@Named
public class FragmentDirectiveBuilder extends AbstractLiveFileBuilder {

    private JacksonObjectMapper objectMapper;
    private HtmlBuilderVisitor htmlBuilderVisitor;
    private HtmlSanitizer htmlSanitizer;
    private TemplateEngine htmlBuilder = new TemplateEngine("fragmentDirectiveTemplate.hbs.js");

    @Inject
    public FragmentDirectiveBuilder(Watcher watcher,
                                    JacksonObjectMapper objectMapper,
                                    HtmlBuilderVisitor htmlBuilderVisitor,
                                    HtmlSanitizer htmlSanitizer) {
        super(watcher);
        this.objectMapper = objectMapper;
        this.htmlBuilderVisitor = htmlBuilderVisitor;
        this.htmlSanitizer = htmlSanitizer;
    }

    /**
     * Build directive corresponding to the fragment descriptive json file which has changed.
     * Resulting js file is created in the same directory than the json file overriding previous build.
     *
     * @param jsonPath is the path to the widget file to build.
     * @throws IOException
     */
    @Override
    public void build(Path jsonPath) throws IOException {
        String path = jsonPath.toString();
        Fragment fragment = objectMapper.fromJson(readAllBytes(get(path)), Fragment.class);
        write(
                get(path.replace(".json", ".js")),
                htmlBuilder
                        .with("rowsHtml", htmlSanitizer.escapeSingleQuotesAndNewLines(htmlBuilderVisitor.build(fragment.getRows())))
                        .build(fragment).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean isBuildable(String path) {
        return path.endsWith(".json") && !path.contains(".metadata");
    }
}
