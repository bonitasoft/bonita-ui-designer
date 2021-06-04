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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetDependencyImporterTest {

    @Mock
    AssetRepository<Page> pageAssetRepository;

    @InjectMocks
	AssetDependencyImporter<Page> pageAssetDependencyImporter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path zipBaseDir;

    @Before
    public void init() {
        zipBaseDir = Paths.get(temporaryFolder.getRoot().getPath());
    }

    public void createDirectoryAsset(String assetType) throws IOException {
        temporaryFolder.newFolder("assets");
        temporaryFolder.newFolder("assets/", assetType);
    }

    @Test
    public void should_not_thrown_exception_when_page_has_no_asset() throws Exception {
        List<Asset> assets = pageAssetDependencyImporter.load(PageBuilder.aPage().build(), zipBaseDir);
        assertThat(assets).isEmpty();
    }

    @Test
    public void should_load_assets() throws Exception {
        Page page = PageBuilder.aPage().build();
        createDirectoryAsset("css");
        Files.write(zipBaseDir.resolve("assets").resolve("css").resolve("myfile.css"), "<style>.maclass1{}</style>".getBytes());

        List<Asset> assetsReturned = new ArrayList<>();
        assetsReturned.add(new Asset().setName("myfile.css"));

        when(pageAssetRepository.findAssetInPath(page, AssetType.CSS, zipBaseDir.resolve("assets").resolve("css"))).thenReturn(assetsReturned);

        List<Asset> assets = pageAssetDependencyImporter.load(page, zipBaseDir);

        assertThat(assets).isNotEmpty();
    }

    @Test
    public void should_save_assets_from_zip_with_assets_folder_at_root() throws Exception {
        Asset cssAsset = AssetBuilder.anAsset().withScope("widget").withComponentId("widgetId").withId("aa").withType(AssetType.CSS).withName("style.css").build();
        createDirectoryAsset("css");
        byte[] content = "<style>.maclass1{}</style>".getBytes();
        Files.write(zipBaseDir.resolve("assets").resolve("css").resolve("style.css"), content);

        pageAssetDependencyImporter.save( Arrays.asList(cssAsset), zipBaseDir);

        verify(pageAssetRepository, times(1)).save(cssAsset, content);
    }

    @Test
    public void should_save_assets_from_zip_with_assets_folder_contains_in_folder_with_widgetId_as_name() throws Exception {
        Asset cssAsset = AssetBuilder.anAsset().withScope("widget").withComponentId("customWidget").withId("aa").withType(AssetType.CSS).withName("style.css").build();
        temporaryFolder.newFolder("customWidget", "assets", "css");
        byte[] content = "<style>.maclass1{}</style>".getBytes();
        Files.write(zipBaseDir.resolve("customWidget").resolve("assets").resolve("css").resolve("style.css"), content);

        pageAssetDependencyImporter.save(Arrays.asList(cssAsset), zipBaseDir);

        verify(pageAssetRepository, times(1)).save(cssAsset, content);
    }
}
