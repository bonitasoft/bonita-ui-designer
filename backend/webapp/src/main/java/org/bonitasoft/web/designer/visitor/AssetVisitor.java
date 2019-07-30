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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AssetVisitor implements ElementVisitor<Set<Asset>> {

    private WidgetRepository widgetRepository;

    public AssetVisitor(WidgetRepository widgetRepository) {
        this.widgetRepository = widgetRepository;
    }

    @Override
    public Set<Asset> visit(Container container) {
        Set<Asset> assets = new HashSet<>();
        assets.addAll(linkAssetToComponent(container.getId()));
        assets.addAll(visitRows(container.getRows()));
        return assets;
    }

    @Override
    public Set<Asset> visit(FormContainer formContainer) {
        Set<Asset> assets = new HashSet<>();
        assets.addAll(linkAssetToComponent(formContainer.getId()));

        assets.addAll(formContainer.getContainer().accept(this));
        return assets;
    }

    @Override
    public Set<Asset> visit(TabsContainer tabsContainer) {
        Set<Asset> assets = new HashSet<>();
        assets.addAll(linkAssetToComponent(tabsContainer.getId()));
        for (TabContainer tabContainer : tabsContainer.getTabList()) {
            assets.addAll(tabContainer.accept(this));
        }
        return assets;
    }

    @Override
    public Set<Asset> visit(TabContainer tabContainer) {
        Set<Asset> assets = new HashSet<>();
        assets.addAll(tabContainer.getContainer().accept(this));
        return assets;
    }

    @Override
    public Set<Asset> visit(Component component) {
        Widget widget = widgetRepository.get(component.getId());
        //Component id and scope are not persisted
        for (Asset asset : widget.getAssets()) {
            asset.setComponentId(widget.getId());
            asset.setScope(AssetScope.WIDGET);
        }
        return widget.getAssets();
    }

    public Set<Asset> visit(Widget widget){
        //Scope is not persisted
        for (Asset asset : widget.getAssets()) {
            asset.setScope(AssetScope.WIDGET);
        }
        return widget.getAssets();
    }

    @Override
    public Set<Asset> visit(ModalContainer modalContainer) {
        Set<Asset> assets = new HashSet<>();
        assets.addAll(linkAssetToComponent(modalContainer.getId()));
        assets.addAll(modalContainer.getContainer().accept(this));
        return assets;
    }

    private List<Asset> linkAssetToComponent(String id) {
        Widget widget = widgetRepository.get(id);
        return widget.getAssets().stream().map(asset -> {
            asset.setComponentId(widget.getId());
            asset.setScope(AssetScope.WIDGET);
            return asset;

        }).collect(Collectors.toList());
    }

    @Override
    public <P extends Previewable & Identifiable> Set<Asset> visit(P previewable) {
        Set<Asset> assets = new HashSet<>();

        if (previewable instanceof Assetable) {
            Set<Asset> pageAssets = ((Assetable) previewable).getAssets();
            assets.addAll(Collections2.transform(pageAssets, new Function<Asset, Asset>() {

                @Override
                public Asset apply(Asset asset) {
                    return asset.setScope(AssetScope.PAGE);
                }
            }));
            assets.addAll(visitRows(previewable.getRows()));

            //User can exclude assets or specify a specific order in the page
            for (Asset asset : assets) {
                if (asset.getId() != null) {
                    asset.setActive(!previewable.getInactiveAssets().contains(asset.getId()));
                } else {
                    asset.setActive(!previewable.getInactiveAssets().contains(asset.getName()));
                }
            }
        }

        return assets;
    }

    protected Set<Asset> visitRows(List<List<Element>> rows) {
        Set<Asset> assets = new HashSet<>();
        for (List<Element> elements : rows) {
            for (Element element : elements) {
                assets.addAll(element.accept(this));
            }
        }
        return assets;
    }

}
