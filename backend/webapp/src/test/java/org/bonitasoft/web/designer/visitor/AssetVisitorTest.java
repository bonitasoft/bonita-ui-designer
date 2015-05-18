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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabBuilder.aTab;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssetVisitorTest {

    @Mock
    private WidgetRepository widgetRepository;
    @InjectMocks
    private AssetVisitor assetVisitor;
    
    @Test
    public void should_return_empty_set_when_components_use_no_asset() throws Exception {
        Component component = mockComponentFor(aWidget(), null);

        Page page = aPage().with(component).build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).isEmpty();
    }

    @Test
    public void should_return_list_of_asset_used_by_widgets() throws Exception {
        Component component = mockComponentFor(
                aWidget(),
                anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT),
                anAsset().withName("http://mycdn.com/myfile.js").withType(AssetType.JAVASCRIPT)
        );

        Page page = aPage().with(component).build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js", "http://mycdn.com/myfile.js");
    }

    @Test
    public void should_return_list_of_asset_used_by_one_page() throws Exception {

        Page page = aPage().withAsset(anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT).buildAsset()).build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js");
    }

    @Test
    public void should_return_list_of_distinct_asset_used_by_page_and_widgets() throws Exception {
        Component component = mockComponentFor(
                aWidget(),
                anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT),
                anAsset().withName("http://mycdn.com/myfile.js").withType(AssetType.JAVASCRIPT)
        );

        Page page = aPage()
                .with(component)
                .withAsset(anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT).buildAsset())
                .build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js", "http://mycdn.com/myfile.js");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_container() throws Exception {
        Component component1 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));

        Set<Asset> assets = assetVisitor.visit(aContainer().with(component1, component2).build());

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_formcontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), anAsset().withName("myfile.css").withType(AssetType.CSS));

        Set<Asset> assets = assetVisitor.visit(aFormContainer().with(
                aContainer().with(component1, component2)).build());

        assertThat(assets).extracting("name").containsOnly("myfile.js", "myfile.css");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_tabscontainer_plus_uibootstrap_which_is_needed_by_tabscontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), anAsset().withName("myfile.css").withType(AssetType.CSS));

        Set<Asset> assets = assetVisitor.visit(aTabsContainer().with(
                aTab().with(aContainer().with(component1)),
                aTab().with(aContainer().with(component2))).build());

        assertThat(assets).extracting("name").containsOnly("myfile.js", "myfile.css");
        assertThat(assets.iterator().next().getComponentId()).isNotEmpty();

    }

    private Component mockComponentFor(WidgetBuilder widgetBuilder, AssetBuilder... assetBuilders) throws Exception {
        Widget widget = widgetBuilder.id(UUID.randomUUID().toString()).assets(assetBuilders).build();
        Component component = aComponent().withWidgetId(widget.getId()).build();
        when(widgetRepository.get(component.getId())).thenReturn(widget);
        return component;
    }

}