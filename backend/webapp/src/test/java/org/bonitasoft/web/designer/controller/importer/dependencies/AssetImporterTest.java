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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
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
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssetImporterTest {

    @Mock
    AssetRepository<Page> pageAssetRepository;

    @InjectMocks
    AssetImporter<Page> pageAssetImporter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path zipBaseDir;

    @Before
    public void init(){
        zipBaseDir = Paths.get(temporaryFolder.getRoot().getPath());
    }

    public void createDirectoryAsset(String assetType) throws IOException {
        temporaryFolder.newFolderPath("assets");
        temporaryFolder.newFolderPath("assets/" + assetType);
    }

    @Test
    public void should_not_thrown_excpetion_when_page_has_no_asset() throws Exception{
        List<Asset> assets = pageAssetImporter.load(PageBuilder.aPage().build(), zipBaseDir);
        assertThat(assets).isEmpty();
    }

    @Test
    public void should_load_assets() throws Exception{
        Page page = PageBuilder.aPage().build();
        createDirectoryAsset("css");
        Files.write(zipBaseDir.resolve("assets").resolve("css").resolve("myfile.css"), "<style>.maclass1{}</style>".getBytes());

        List<Asset> assetsReturned = new ArrayList<>();
        assetsReturned.add(new Asset().setName("myfile.css"));

        when(pageAssetRepository.findAssetInPath(page, AssetType.CSS, zipBaseDir.resolve("assets").resolve("css"))).thenReturn(assetsReturned);

        List<Asset> assets = pageAssetImporter.load(page, zipBaseDir);

        assertThat(assets).isNotEmpty();
    }
}
