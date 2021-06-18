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
package org.bonitasoft.web.designer.repository;

import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.web.designer.builder.AssetBuilder.aFilledAsset;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.repository.AssetRepository.COMPONENT_ID_REQUIRED;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Path pagesPath;

    @Mock
    private BeanValidator validator;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private AssetRepository<Page> assetRepository;

    @Before
    public void setUp() throws Exception {
        pagesPath = Paths.get(temporaryFolder.getRoot().getPath());
    }

    @Test
    public void should_resolveAssetPath() {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);

        Path path = assetRepository.resolveAssetPath(asset);

        assertThat(path.toUri()).isEqualTo(pagesPath.resolve("assets").resolve("js").resolve(asset.getName()).toUri());
    }

    @Test
    public void should_not_resolveAssetPath_when_asset_invalid() {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        exception.expect(ConstraintValidationException.class);
        doThrow(ConstraintValidationException.class).when(validator).validate(asset);

        assetRepository.resolveAssetPath(asset);
    }

    @Test
    public void should_save_asset() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        assertThat(fileExpected.toFile()).doesNotExist();
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.save(asset, "My example with special characters réè@# \ntest".getBytes(UTF_8));

        //A json file has to be created in the repository
        assertThat(fileExpected.toFile()).exists();
        assertThat(Files.readAllLines(fileExpected, UTF_8).get(0)).isEqualTo("My example with special characters réè@# ");
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_NullPointerException_when_deleting_asset_componentId_null() throws Exception {
        assetRepository.delete(new Asset());
    }

    @Test
    public void should_delete_asset() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        createDirectories(pagesPath.resolve("assets").resolve("js"));
        temporaryFolder.newFilePath("assets/js/" + asset.getName());
        assertThat(fileExpected.toFile()).exists();
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.delete(asset);

        assertThat(fileExpected.toFile()).doesNotExist();
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_deleting_inexisting_page() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.delete(asset);
    }

    @Test
    public void readAllBytes_for_null_id_should_throw_ex() throws Exception {
        assertThatThrownBy(() -> assetRepository.readAllBytes(null, mock(Asset.class)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(COMPONENT_ID_REQUIRED);
    }

    @Test
    public void should_readAllBytes_asset() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        Path fileExpected = pagesPath.resolve("assets").resolve("js").resolve(asset.getName());
        createDirectories(pagesPath.resolve("assets").resolve("js"));
        temporaryFolder.newFilePath("assets/js/" + asset.getName());
        assertThat(fileExpected.toFile()).exists();
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assertThat(assetRepository.readAllBytes(asset)).isNotNull().isEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_NullPointerException_when_reading_asset_with_component_id_null() throws Exception {
        assetRepository.readAllBytes(new Asset());
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_reading_inexisting_page() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        lenient().when(pageRepository.get(asset.getComponentId())).thenReturn(page);

        assetRepository.readAllBytes(asset);
    }

    @Test
    public void should_find_asset_path_used_by_a_component() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        System.out.println(asset.getName());
        pagesPath.resolve("assets").resolve("js").resolve(asset.getName());

        createDirectories(pagesPath.resolve("assets").resolve("js"));
        temporaryFolder.newFilePath("assets/js/" + asset.getName());
        when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        when(pageRepository.get("page-id")).thenReturn(
                aPage().withAsset(asset).build());

        assertThat(assetRepository.findAssetPath("page-id", "myasset.js", AssetType.JAVASCRIPT).toFile()).exists();
    }

    @Test
    public void should_throw_NotAllowedException_when_find_external_asset() throws Exception {
        exception.expect(NotAllowedException.class);
        exception.expectMessage("We can't load an external asset. Use the link http://mycdnserver.myasset.js");
        Page page = aPage().withId("page-id").build();

        Asset asset = aFilledAsset(page);
        asset.setName("http://mycdnserver.myasset.js");
        asset.setExternal(true);

        when(pageRepository.get("page-id")).thenReturn(
                aPage().withAsset(asset).build());

        assetRepository.findAssetPath(

                "page-id", "http://mycdnserver.myasset.js", AssetType.JAVASCRIPT);
    }

    @Test
    public void should_throw_NullPointerException_when_find_asset_with_filename_null() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("Filename is required");

        assetRepository.findAssetPath("page-id", null, AssetType.JAVASCRIPT);
    }

    @Test
    public void should_throw_NullPointerException_when_find_asset_path_with_type_null() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("Asset type is required");

        assetRepository.findAssetPath("page-id", "myfile.js", null);
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_NoSuchElementException_when_finding_inexistant_asset() throws Exception {
        Page page = aPage().withId("page-id").build();
        Asset asset = aFilledAsset(page);
        pagesPath.resolve(asset.getName());
        temporaryFolder.newFilePath(asset.getName());
        lenient().when(pageRepository.resolvePathFolder("page-id")).thenReturn(pagesPath);
        when(pageRepository.get("page-id")).thenReturn(aPage().withAsset(asset).build());

        assetRepository.findAssetPath("page-id", "inexistant.js", AssetType.JAVASCRIPT);
    }

    @Test
    public void should_findAssetInPath_asset() throws Exception {
        Page page = aPage().withId("page-id").build();
        write(pagesPath.resolve("file1.css"), "<style>.maclass1{}</style>".getBytes());
        write(pagesPath.resolve("file2.css"), "<style>.maclass2{}</style>".getBytes());

        List<Asset> assets = assetRepository.findAssetInPath(page, AssetType.CSS, pagesPath);

        assertThat(assets).hasSize(2);
        assertThat(assets).extracting("name").contains("file1.css", "file2.css");
    }

    @Test
    public void should_findAssetInPath_asset_when_noone_is_present() throws Exception {
        Page page = aPage().withId("page-id").build();

        List<Asset> assets = assetRepository.findAssetInPath(page, AssetType.CSS, pagesPath);

        assertThat(assets).isEmpty();
    }

    @Test
    public void should_refresh_component_assets_from_disk() throws Exception {
        Page page = aPage().withId("page-id")
                .withAsset(anAsset().withName("existing-asset.js")).build();
        temporaryFolder.newFolder("page-id", "assets", "css");
        write(pagesPath.resolve("page-id/assets/css/file1.css"), "<style>.maclass1{}</style>".getBytes());
        write(pagesPath.resolve("page-id/assets/css/file2.css"), "<style>.maclass2{}</style>".getBytes());
        when(pageRepository.resolvePath("page-id")).thenReturn(pagesPath.resolve("page-id"));

        assetRepository.refreshAssets(page);

        assertThat(page.getAssets()).hasSize(3);
        assertThat(page.getAssets()).extracting("name").contains("file1.css", "file2.css", "existing-asset.js");
        verify(pageRepository).updateLastUpdateAndSave(page);
    }
}
