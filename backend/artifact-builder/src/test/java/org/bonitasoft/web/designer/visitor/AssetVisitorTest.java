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

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetVisitorTest {

    @Mock
    private WidgetRepository widgetRepository;
    @Mock
    private FragmentRepository fragmentRepository;
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
                UUID.randomUUID().toString(),
                anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT),
                anAsset().withName("http://mycdn.com/myfile.js").withType(AssetType.JAVASCRIPT)
        );

        Page page = aPage().with(component).build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js", "http://mycdn.com/myfile.js");
    }

    @Test
    public void should_return_list_of_asset_used_by_one_page() throws Exception {

        Page page = aPage().withAsset(anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT).build()).build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").containsOnlyOnce("myfile.js");
    }

    @Test
    public void should_return_list_of_distinct_asset_used_by_page_and_widgets() throws Exception {
        Component component = mockComponentFor(
                aWidget(),
                "id1",
                anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT),
                anAsset().withName("http://mycdn.com/myfile.js").withType(AssetType.JAVASCRIPT)
        );

        Page page = aPage()
                .with(component)
                .withAsset(anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT).build())
                .build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").contains("myfile.js", "myfile.js", "http://mycdn.com/myfile.js");
        assertThat(assets).extracting("componentId").contains(null, "id1", "id1");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_container() throws Exception {
        Component component1 = mockComponentFor(aWidget(), "id1", anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), "id2", anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));

        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());
        Set<Asset> assets = assetVisitor.visit(aContainer().with(component1, component2).build());

        assertThat(assets).extracting("name").containsExactly("myfile.js", "myfile.js");
        assertThat(assets).extracting("componentId").contains("id2", "id1");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_container_who_have_an_asset() throws Exception {
        Component component1 = mockComponentFor(aWidget(), "id1", anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), "id2", anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));

        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().assets(anAsset().withName("container.min.js").withType(AssetType.JAVASCRIPT)).build());
        Set<Asset> assets = assetVisitor.visit(aContainer().with(component1, component2).build());

        assertThat(assets).extracting("name").containsExactly("container.min.js", "myfile.js", "myfile.js");
        assertThat(assets).extracting("componentId").contains("id2", "id1");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_formcontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.css").withType(AssetType.CSS));
        when(widgetRepository.get("pbFormContainer")).thenReturn(aWidget().build());
        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());
        Set<Asset> assets = assetVisitor.visit(aFormContainer().with(
                aContainer().with(component1, component2)).build());

        assertThat(assets).extracting("name").containsOnly("myfile.js", "myfile.css");
    }

    public void should_return_list_of_asset_needed_by_widgets_in_formcontainer_who_have_an_asset() throws Exception {
        Component component1 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.css").withType(AssetType.CSS));
        when(widgetRepository.get("pbFormContainer")).thenReturn(aWidget().assets(anAsset().withName("formContainer.min.js").withType(AssetType.JAVASCRIPT)).build());
        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());
        Set<Asset> assets = assetVisitor.visit(aFormContainer().with(
                aContainer().with(component1, component2)).build());

        assertThat(assets).extracting("name").containsOnly("myfile.js", "myfile.css", "formContainer.min.js");
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_tabscontainer_plus_uibootstrap_which_is_needed_by_tabscontainer() throws Exception {
        Component component1 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.css").withType(AssetType.CSS));
        when(widgetRepository.get("pbTabsContainer")).thenReturn(aWidget().assets(anAsset().withName("bootstrap.min.js").withType(AssetType.JAVASCRIPT)).build());
        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());
        Set<Asset> assets = assetVisitor.visit(aTabsContainer().with(
                aTabContainer().withId("Tab 1").with(aContainer().with(component1)),
                aTabContainer().withId("Tab 2").with(aContainer().with(component2))).build());

        assertThat(assets).extracting("name").containsOnly("myfile.js", "myfile.css", "bootstrap.min.js");
        assertThat(assets.iterator().next().getComponentId()).isNotEmpty();

    }

    @Test
    public void should_update_inactive_indicator_when_asset_is_inactive_in_a_page() throws Exception {
        Page page = aPage()
                .withAsset(
                        anAsset().withId("assetUIID1").withName("myfile.js").withType(AssetType.JAVASCRIPT).build(),
                        anAsset().withId("assetUIID2").withName("myfile.css").withType(AssetType.CSS).build()
                )
                .withInactiveAsset("assetUIID2")
                .build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").contains("myfile.js", "myfile.css");
        assertThat(assets).extracting("active").contains(true, false);
    }

    @Test
    public void should_return_list_of_asset_needed_by_widgets_in_modal_container() throws Exception {
        Component component1 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), UUID.randomUUID().toString(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));

        when(widgetRepository.get("pbModalContainer")).thenReturn(aWidget().build());
        when(widgetRepository.get("pbContainer")).thenReturn(aWidget().build());
        Set<Asset> assets = assetVisitor.visit(aModalContainer().with(aContainer().with(component1, component2)).build());

        assertThat(assets).extracting("name").containsExactly("myfile.js", "myfile.js");
    }

    @Test
    public void should_good_list_widget_asset__if_one_is_inactive() throws Exception {
        Component component = mockComponentFor(
                aWidget(),
                "id1",
                anAsset().withName("myfileBis.js").withType(AssetType.JAVASCRIPT),
                anAsset().withName("http://mycdn.com/myfile.js").withType(AssetType.JAVASCRIPT)
        );

        Page page = aPage().with(component)
                .withAsset(
                        anAsset().withId("assetUIID1").withName("myfile.js").withType(AssetType.JAVASCRIPT).build(),
                        anAsset().withId("assetUIID2").withName("myfile.css").withType(AssetType.CSS).build()
                )
                .withInactiveAsset("assetUIID2", "myfileBis.js")
                .build();

        Set<Asset> assets = assetVisitor.visit(page);

        assertThat(assets).extracting("name").contains("myfile.js", "myfile.css", "myfileBis.js", "http://mycdn.com/myfile.js");
        assertThat(assets).extracting("active").contains(true, false, true, false);
    }

    @Test
    public void should_return_list_of_module_needed_by_widgets_in_fragment() throws Exception {
        Component component1 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        Component component2 = mockComponentFor(aWidget(), anAsset().withName("myfile.js").withType(AssetType.JAVASCRIPT));
        FragmentElement fragmentElement = aFragmentElement().withFragmentId("my-fragment").build();
        Fragment fragment = aFragment().withId("my-fragment").with(component1, component2).build();
        lenient().when(fragmentRepository.get(fragmentElement.getId())).thenReturn(fragment);

        Set<Asset> assets = assetVisitor.visit(fragment);

        assertThat(assets).extracting("name").containsOnly("myfile.js");
    }

    private Component mockComponentFor(WidgetBuilder widgetBuilder, String id, AssetBuilder... assetBuilders) throws Exception {
        Widget widget = widgetBuilder.withId(id).assets(assetBuilders).build();
        Component component = aComponent().withWidgetId(widget.getId()).build();
        when(widgetRepository.get(component.getId())).thenReturn(widget);
        return component;
    }

    private Component mockComponentFor(WidgetBuilder widgetBuilder, AssetBuilder... assetBuilders) throws Exception {
        Widget widget = widgetBuilder.withId(UUID.randomUUID().toString()).assets(assetBuilders).build();
        Component component = aComponent().withWidgetId(widget.getId()).build();
        when(widgetRepository.get(component.getId())).thenReturn(widget);
        return component;
    }

}
