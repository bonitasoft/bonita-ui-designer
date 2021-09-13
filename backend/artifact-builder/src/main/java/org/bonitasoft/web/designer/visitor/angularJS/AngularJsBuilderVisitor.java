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
package org.bonitasoft.web.designer.visitor.angularJS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.rendering.AssetHtmlBuilder;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.model.widget.Widget.spinalCase;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
@RequiredArgsConstructor
@Slf4j
public class AngularJsBuilderVisitor implements ElementVisitor<String>, AbstractBuilderVisitor<String> {

    private final List<PageFactory> pageFactories;
    private final RequiredModulesVisitor requiredModulesVisitor;
    private final DirectivesCollector directivesCollector;
    private final FragmentRepository fragmentRepository;
    private final AssetHtmlBuilder assetHtmlBuilder;

    @Override
    public String visit(FragmentElement fragmentElement) {

        try {
            var fragment = fragmentRepository.get(fragmentElement.getId());
            return new TemplateEngine("fragment.hbs.html")
                    .with("reference", fragmentElement.getReference())
                    .with("dimensionAsCssClasses", fragmentElement.getDimensionAsCssClasses())
                    .with("tagName", spinalCase(fragment.getDirectiveName()))
                    .build(fragment);

        } catch (RepositoryException | NotFoundException e) {
            throw new GenerationException("Error while generating html for fragment " + fragmentElement.getId(), e);
        }
    }

    @Override
    public String visit(Container container) {
        return new TemplateEngine("container.hbs.html")
                .with("rowsHtml", build(container.getRows()))
                .build(container);
    }

    @Override
    public String visit(FormContainer formContainer) {
        return new TemplateEngine("formContainer.hbs.html")
                .with("content", formContainer.getContainer().accept(this))
                .build(formContainer);
    }

    @Override
    public String visit(TabsContainer tabsContainer) {

        var tabTemplates = new ArrayList<TabContainerTemplate>();
        for (var tab : tabsContainer.getTabList()) {
            tabTemplates.add(new TabContainerTemplate(tab.accept(this)));
        }

        return new TemplateEngine("tabsContainer.hbs.html")
                .with("tabTemplates", tabTemplates)
                .build(tabsContainer);
    }


    @Override
    public String visit(TabContainer tabContainer) {
        return new TemplateEngine("tabContainer.hbs.html")
                .with("content", tabContainer.getContainer().accept(this))
                .build(tabContainer);
    }

    @Override
    public String visit(ModalContainer modalContainer) {
        return new TemplateEngine("modalContainer.hbs.html")
                .with("content", modalContainer.getContainer().accept(this))
                .with("modalidHtml", modalContainer.getPropertyValues().get("modalId").getValue())
                .build(modalContainer);
    }

    @Override
    public String visit(Component component) {
        return new TemplateEngine("component.hbs.html")
                .with("template", "<" + Widget.spinalCase(component.getId()) + "></" + Widget.spinalCase(component
                        .getId()) + ">")
                .build(component);
    }

    @Override
    public String visit(Previewable previewable) {
        throw new ArtifactBuilderException("Can't build previewable html by visiting it. Need to call " +
                "HtmlBuilderVisitor#build.");
    }

    /**
     * Build a previewable HTML, based on the given list of widgets
     * TODO: once resourceContext remove we can merge this method with HtmlBuilderVisitor#visit(Previewable)
     *
     * @param previewable     to build
     * @param resourceContext the URL context can change on export or preview...
     */
    @Override
    public <P extends Previewable & Identifiable> String build(final P previewable, String resourceContext) {
        var sortedAssets = assetHtmlBuilder.getSortedAssets(previewable);
        var template = new TemplateEngine("page.hbs.html")
                .with("resourceContext", resourceContext == null ? "" : resourceContext)
                .with("directives", directivesCollector.buildUniqueDirectivesFiles(previewable, previewable.getId()))
                .with("rowsHtml", build(previewable.getRows()))
                .with("jsAsset", assetHtmlBuilder.getAssetHtmlSrcList(previewable.getId(),AssetType.JAVASCRIPT, sortedAssets))
                .with("cssAsset", assetHtmlBuilder.getAssetHtmlSrcList(previewable.getId(), AssetType.CSS, sortedAssets))
                .with("factories", pageFactories.stream().map(factory -> factory.generate(previewable)).collect(toList()));

        var modules = requiredModulesVisitor.visit(previewable);
        if (!modules.isEmpty()) {
            template = template.with("modules", modules);
        }
        return template.build(previewable);
    }

    public String build(List<List<Element>> rows) {
        return new TemplateEngine("rows.hbs.html")
                .with("rows",
                        rows.stream()
                                .map(elements -> elements.stream()
                                        .map(element -> element.accept(AngularJsBuilderVisitor.this))
                                        .collect(joining(""))
                                ).collect(toList())
                )
                .build(new Object());
    }

    static class TabContainerTemplate {

        private final String content;

        public TabContainerTemplate(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }
}
