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
package org.bonitasoft.web.designer.visitor;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByComponentId;
import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByOrder;
import static org.bonitasoft.web.designer.model.widget.Widget.spinalCase;

/**
 * An element visitor which traverses the tree of elements recursively to collect html parts of a page
 */
@RequiredArgsConstructor
public class HtmlBuilderVisitor implements ElementVisitor<String> {

    private static final Logger logger = LoggerFactory.getLogger(HtmlBuilderVisitor.class);

    private final AssetVisitor assetVisitor;
    //Localizationfactory, EmptyPagefactory, ModelPropertiesVisitor, PropertyValuesVisitor, VariableModelVisitor
    private final List<PageFactory> pageFactories;
    private final RequiredModulesVisitor requiredModulesVisitor;
    private final DirectivesCollector directivesCollector;
    private final AssetRepository<Page> pageAssetRepository;
    private final AssetRepository<Widget> widgetAssetRepository;
    private final FragmentRepository fragmentRepository;
    private final String modelVersion;

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
    public <P extends Previewable & Identifiable> String build(final P previewable, String resourceContext) {
        var sortedAssets = getSortedAssets(previewable);
        var template = new TemplateEngine("page.hbs.html")
                .with("resourceContext", resourceContext == null ? "" : resourceContext)
                .with("uidModelVersion", modelVersion)
                .with("directives", directivesCollector.buildUniqueDirectivesFiles(previewable, previewable.getId()))
                .with("rowsHtml", build(previewable.getRows()))
                .with("jsAsset", getAssetHtmlSrcList(previewable.getId(), AssetType.JAVASCRIPT, sortedAssets))
                .with("cssAsset", getAssetHtmlSrcList(previewable.getId(), AssetType.CSS, sortedAssets))
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
                                        .map(element -> element.accept(HtmlBuilderVisitor.this))
                                        .collect(joining(""))
                                ).collect(toList())
                )
                .build(new Object());
    }

    /**
     * Return the list of the previewable assets sorted with only active assets
     */
    protected <P extends Previewable & Identifiable> List<Asset> getSortedAssets(P previewable) {
        return assetVisitor.visit(previewable).stream().filter(Asset::isActive)
                .sorted(getComparatorByComponentId().thenComparing(getComparatorByOrder())
                ).collect(Collectors.toList());
    }

    private List<String> getAssetHtmlSrcList(String previewableId, AssetType assetType, List<Asset> sortedAssets) {
        var assetsSrc = new ArrayList<String>();
        sortedAssets.stream()
                .filter(asset -> assetType.equals(asset.getType()))
                .forEach(asset -> {
                    var widgetPrefix = "";
                    if (asset.isExternal()) {
                        assetsSrc.add(asset.getName());
                    } else {
                        String assetHash;
                        if (AssetScope.WIDGET.equals(asset.getScope())) {
                            widgetPrefix = String.format("widgets/%s/", asset.getComponentId());
                            assetHash = getHash(asset, widgetAssetRepository, previewableId);
                        } else {
                            assetHash = getHash(asset, pageAssetRepository, previewableId);
                        }
                        if (!assetsSrc.contains(asset.getName())) {
                            assetsSrc.add(String.format("%sassets/%s/%s?hash=%s", widgetPrefix, asset.getType().getPrefix(), asset.getName(), assetHash));
                        }
                    }
                });
        return assetsSrc;
    }

    private String getHash(Asset asset, AssetRepository<?> assetRepository, String previewableId) {
        try {
            var content = asset.getComponentId() == null ? assetRepository.readAllBytes(previewableId, asset) : assetRepository.readAllBytes(asset);
            return DigestUtils.sha1Hex(content);
        } catch (Exception e) {
            logger.warn("Failure to generate hash for asset {}", asset.getName(), e);
            return UUID.randomUUID().toString();
        }
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
