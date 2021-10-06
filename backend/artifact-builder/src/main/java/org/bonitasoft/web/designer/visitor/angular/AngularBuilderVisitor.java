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
package org.bonitasoft.web.designer.visitor.angular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.AbstractBuilderVisitor;
import org.bonitasoft.web.designer.visitor.ElementVisitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
@RequiredArgsConstructor
@Slf4j
public class AngularBuilderVisitor implements ElementVisitor<String>, AbstractBuilderVisitor<String> {

    private final WorkspaceProperties workspaceProperties;
    private final WidgetService widgetService;

    @Override
    public <P extends Previewable & Identifiable> String build(final P previewable, String resourceContext) {
        var template = new TemplateEngine(Paths.get("angular/page.hbs.html").toString())
                .with("rowsHtml", build(previewable.getRows()));
        return template.build(previewable);
    }

    @Override
    public String visit(FragmentElement fragmentElement) {
        return "";
    }

    @Override
    public String visit(Container container) {
        return new TemplateEngine("angular/container.hbs.html")
                .with("rowsHtml", build(container.getRows()))
                .build(container);
    }

    @Override
    public String visit(FormContainer formContainer) {
        return "";
    }

    @Override
    public String visit(TabsContainer tabsContainer) {
        //Todo
        return "";
    }

    @Override
    public String visit(TabContainer tabContainer) {
        //Todo
        return "";
    }

    @Override
    public String visit(ModalContainer modalContainer) {
        //Todo
        return "";
    }

    @Override
    public String visit(Component component) {
        // Load widget from component
        var widget = widgetService.get(component.getId());

        Path htmlTemplate = workspaceProperties.getWidgets().getDir().resolve(widget.getId()).resolve(widget.getHtmlBundle());
        try {
            // Write content in template
            return new TemplateEngine(Paths.get("angular/component.hbs.html").toString())
                    .with("template", Files.readString(htmlTemplate))
                    .build(component);
        } catch (IOException e) {
            throw new GenerationException(String.format("Error on %s component generation template", component.getId()),e);
        }
    }

    public String build(List<List<Element>> rows) {
        return new TemplateEngine(Paths.get("angular/rows.hbs.html").toString())
                .with("rows",
                        rows.stream()
                                .map(elements -> elements.stream()
                                        .map(element -> element.accept(AngularBuilderVisitor.this))
                                        .collect(joining(""))
                                ).collect(toList())
                )
                .build(new Object());
    }


    @Override
    public String visit(Previewable previewable) {
        throw new ArtifactBuilderException("Can't build previewable html by visiting it. Need to call " +
                "HtmlBuilderVisitor#build.");
    }
}
