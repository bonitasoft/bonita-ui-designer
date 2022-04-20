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
package org.bonitasoft.web.designer.controller.asset;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static junitparams.JUnitParamsRunner.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class AssetServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Repository<Page> repository;

    @Mock
    private AssetRepository<Page> assetRepository;

    @Mock
    private AssetDependencyImporter<Page> assetDependencyImporter;

    private AssetService assetService;

    @Before
    public void setUp() throws Exception {
        assetService = new AssetService(repository, assetRepository, assetDependencyImporter);
    }


    @Test
    public void should_return_error_when_adding_asset_with_name_null() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            assetService.save(aPage().withId("page-id").build(), anAsset().withName(null).build());
        });
        assertThat(exception.getMessage()).isEqualTo("Asset URL is required");
    }

    @Test
    public void should_return_error_when_adding_asset_with_name_empty() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            //We construct a mockfile (the first arg is the name of the property expected in the controller
            assetService.save(aPage().withId("page-id").build(), anAsset().withName("").build());
        });
        assertThat(exception.getMessage()).isEqualTo("Asset URL is required");
    }

    @Test
    public void should_return_error_when_adding_asset_with_type_invalid() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            assetService.save(aPage().withId("page-id").build(), anAsset().withName("http://mycdn.com/myasset.js").withType(null).build());
        });
        assertThat(exception.getMessage()).isEqualTo("Asset type is required");
    }

    @Test
    public void should_save_new_asset_and_populate_its_id() throws Exception {
        Page page = aPage().build();

        Asset asset = assetService.save(page, anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build());

        verify(repository).updateLastUpdateAndSave(page);
        assertThat(page.getAssets()).contains(asset);
        assertThat(asset.getId()).isNotNull();
    }

    @Test
    public void should_compute_order_while_saving_a_new_asset() throws Exception {
        Page page = aPage().build();

        Asset firstAsset = assetService.save(page, anAsset().withName("http://mycdn.com/first.js").build());
        Asset secondtAsset = assetService.save(page, anAsset().withName("http://mycdn.com/second.js").build());

        assertThat(firstAsset.getOrder()).isEqualTo(1);
        assertThat(secondtAsset.getOrder()).isEqualTo(2);
    }

    @Test
    public void should_update_existing_local_asset() throws Exception {
        Asset existingAsset = anAsset().withId("existingAsset").withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).active().build();
        Asset updatedAsset = anAsset().withId("existingAsset").withName("http://mycdn.com/newName.js").withType(CSS).unactive().build();

        Page page = aPage().withAsset(existingAsset).build();

        assetService.save(page, updatedAsset);

        verify(repository).updateLastUpdateAndSave(page);
        assertThat(page.getAssets().iterator().next()).isEqualTo(updatedAsset);
    }

    @Test
    public void should_return_error_when_error_onsave() throws Exception {
        Page page = aPage().build();
        doThrow(RepositoryException.class).when(repository).updateLastUpdateAndSave(page);

        assertThrows(RepositoryException.class, () ->
                assetService.save(page, anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build()));
    }

    @Test
    public void should_not_return_error_when_adding_existing_asset_witherror_on_delete() throws Exception {
        Asset asset = anAsset().withId("anAsset").build();
        Page page = aPage().withAsset(asset).build();
        doThrow(IOException.class).when(assetRepository).delete(asset);
        var save = assetService.save(page, asset);

        assertThat(asset).isNotNull();
    }

    protected Object[] invalidArgsForDuplicate() throws Exception {
        Path tempPath = Files.createTempDirectory("test");
        return $(
                $(null, tempPath, "src-page-id", "page-id", "source page path is required"),
                $(tempPath, null, "src-page-id", "page-id", "target page path is required"),
                $(tempPath, tempPath, null, "page-id", "source page id is required"),
                $(tempPath, tempPath, "src-page-id", null, "target page id is required"));
    }

    @Parameters(method = "invalidArgsForDuplicate")
    @Test
    public void should_not_duplicate_asset_when_arg_invalid(Path artifactSourcePath, Path artifactTargetPath, String sourceArtifactId, String targetArtifactId,
                                                            String expectedErrorMessage) throws Exception {
        when(repository.getComponentName()).thenReturn("page");
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assetService.duplicateAsset(artifactSourcePath, artifactTargetPath, sourceArtifactId, targetArtifactId)
        );
        assertThat(exception.getMessage()).isEqualTo(expectedErrorMessage);
    }

    @Test
    public void should_duplicate_asset() throws Exception {
        Page page = new Page();
        List<Asset> assets = List.of(anAsset().withId("UUID").withName("myfile.js").build());
        Path tempPath = Files.createTempDirectory("test");
        when(repository.get("src-page-id")).thenReturn(page);
        when(assetDependencyImporter.load(page, tempPath)).thenReturn(assets);

        assetService.duplicateAsset(tempPath, tempPath, "src-page-id", "page-id");

        verify(assetDependencyImporter).save(assets, tempPath);
    }

    @Test
    public void should_return_error_when_deleting_asset_with_name_empty() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                //We construct a mockfile (the first arg is the name of the property expected in the controller
                assetService.delete(aPage().withId("page-id").build(), null)
        );
        assertThat(exception.getMessage()).isEqualTo("Asset id is required");
    }

    @Test
    public void should_delete_existing_asset() throws Exception {
        Page page = aFilledPage("page-id");
        Asset asset = anAsset().withId("UIID").withName("myfile.js").withType(JAVASCRIPT).build();
        page.getAssets().add(asset);

        assetService.delete(page, "UIID");

        verify(assetRepository).delete(asset);
    }

    @Test
    public void should_remove_asset_in_inactive_list_when_delete_is_called() throws Exception {
        Page page = aFilledPage("page-id");
        Asset asset = anAsset().withId("UIID").withName("myfile.js").withType(JAVASCRIPT).unactive().build();
        page.getInactiveAssets().add("UIID");
        page.getAssets().add(asset);

        Assert.assertEquals(page.getInactiveAssets().size(),1);

        assetService.delete(page, "UIID");

        verify(assetRepository).delete(asset);
        Assert.assertEquals(page.getInactiveAssets().size(),0);
    }

    @Test
    public void should_not_delete_file_for_existing_external_asset() throws Exception {
        Page page = aFilledPage("page-id");
        Asset asset = anAsset().withId("UIID").withName("http://mycdn.com/myasset.js").withExternal(true).withType(JAVASCRIPT).build();
        page.getAssets().add(asset);

        assetService.delete(page, "UIID");

        //We must'nt call the delete method for an external resource
        verifyNoMoreInteractions(assetRepository);
    }

    @Test
    public void should_throw_IllegalArgument_when_sorting_asset_component_with_no_name() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                assetService.changeAssetOrderInComponent(aPage().build(), null, DECREMENT)
        );
        assertThat(exception.getMessage()).isEqualTo("Asset id is required");
    }

    private Asset[] getSortedAssets() {
        return new Asset[]{
                anAsset().withId("asset1").withName("asset1").withOrder(1).build(),
                anAsset().withId("asset2").withName("asset2").withOrder(2).build(),
                anAsset().withId("asset3").withName("asset3").withOrder(3).build()
        };
    }

    @Test
    public void should_increment_asset_order_in_component() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[1].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset2", INCREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset2");
        assertThat(assets[0].getOrder()).isEqualTo(1);
        assertThat(assets[1].getOrder()).isEqualTo(3);
        assertThat(assets[2].getOrder()).isEqualTo(2);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_decrement_asset_order_in_component() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[1].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset2", DECREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset2");
        assertThat(assets[0].getOrder()).isEqualTo(2);
        assertThat(assets[1].getOrder()).isEqualTo(1);
        assertThat(assets[2].getOrder()).isEqualTo(3);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_not_increment_asset_order_in_component_when_asset_is_the_last() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[2].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset3", INCREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset3");
        assertThat(assets[0].getOrder()).isEqualTo(1);
        assertThat(assets[1].getOrder()).isEqualTo(2);
        assertThat(assets[2].getOrder()).isEqualTo(3);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_decrement_asset_order_in_component_when_asset_is_the_last() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[2].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset3", DECREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset3");
        assertThat(assets[0].getOrder()).isEqualTo(1);
        assertThat(assets[1].getOrder()).isEqualTo(3);
        assertThat(assets[2].getOrder()).isEqualTo(2);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_not_decrement_asset_order_in_component_when_asset_is_the_first() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[0].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset1", DECREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset1");
        assertThat(assets[0].getOrder()).isEqualTo(1);
        assertThat(assets[1].getOrder()).isEqualTo(2);
        assertThat(assets[2].getOrder()).isEqualTo(3);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_increment_asset_order_in_component_when_asset_is_the_first() throws Exception {
        Asset[] assets = getSortedAssets();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(assets).build();
        when(repository.get("page-id")).thenReturn(page);

        assets[0].setComponentId("page-id");
        Asset assetReturned = assetService.changeAssetOrderInComponent(page, "asset1", INCREMENT);

        assertThat(assetReturned.getName()).isEqualTo("asset1");
        assertThat(assets[0].getOrder()).isEqualTo(2);
        assertThat(assets[1].getOrder()).isEqualTo(1);
        assertThat(assets[2].getOrder()).isEqualTo(3);
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_not_change_asset_state_in_previewable_when_asset_is_already_inactive() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withName("my-page")
                .withInactiveAsset("assetUIID")
                .build();
        when(repository.get("page-id")).thenReturn(page);

        Asset assetSent = anAsset().withId("assetUIID").withComponentId("page-id").withName("myasset.js").build();

        assetService.changeAssetStateInPreviewable(page, "assetUIID", false);

        assertThat(page.getInactiveAssets()).isNotEmpty().contains("assetUIID");
    }

    @Test
    public void should_change_asset_state_in_previewable_when_asset_state_is_inactive() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withName("my-page")
                .build();
        when(repository.get("page-id")).thenReturn(page);

        Asset assetSent = anAsset().withId("assetUIID").withComponentId("page-id").withName("myasset.js").build();

        assetService.changeAssetStateInPreviewable(page, "assetUIID", false);

        assertThat(page.getInactiveAssets()).isNotEmpty().contains("assetUIID");
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_reactive_asset_in_previable_when_asset_is_inactive_in_previewable() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withName("my-page")
                .withInactiveAsset("assetUIID")
                .build();
        when(repository.get("page-id")).thenReturn(page);

        Asset assetSent = anAsset().withId("assetUIID").withComponentId("page-id").withName("myasset.js").build();

        assetService.changeAssetStateInPreviewable(page, "assetUIID", true);

        assertThat(page.getInactiveAssets()).isEmpty();
        verify(repository).updateLastUpdateAndSave(page);
    }

    @Test
    public void should_load_default_assets() {
        Page page = aPage().build();

        assetService.loadDefaultAssets(page);

        verify(assetRepository).refreshAssets(page);
    }

    @Test
    public void should_read_asset_content() throws IOException {
        Asset myAsset = anAsset().withType(CSS).withName("style.css").build();
        Page page = aPage()
                .withDesignerVersion("1.7.9").withAsset(myAsset).build();

        when(assetRepository.readAllBytes(page.getId(), myAsset)).thenReturn("myContentOfAsset".getBytes());

        String content = assetService.getAssetContent(page, myAsset);
        assertThat(content).isEqualTo("myContentOfAsset");

    }
}
