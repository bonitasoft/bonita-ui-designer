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
package org.bonitasoft.web.designer.controller.export.steps;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;

import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_DIRECTORIES;
import static org.bonitasoft.web.designer.controller.export.Zipper.ALL_FILES;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AssetExportStepTest {

    @Mock
    private AssetRepository<Page> pageAssetRepository;

    @Mock
    private Zipper zipper;

    @Mock
    private Path assetPath;

    @InjectMocks
    private AssetExportStep assetExportStep;

    @Test
    public void should_call_zipper_when_page_has_no_asset() throws Exception {

        assetExportStep.execute(zipper, aPage().build());

        Mockito.verifyNoInteractions(zipper);
    }

    @Test
    public void should_export_asset_when_page_has_asset() throws Exception {
        when(pageAssetRepository.findAssetPath("pageId", "myfile.css", AssetType.CSS)).thenReturn(assetPath);
        Page page = aPage().withId("pageId").withAsset(
                anAsset().withName("myfile.css").withType(AssetType.CSS)).build();

        assetExportStep.execute(zipper, page);

        verify(zipper).addDirectoryToZip(assetPath, ALL_DIRECTORIES, ALL_FILES, "resources/assets/css/myfile.css");
    }

    @Test
    public void should_not_export_external_assets() throws Exception {
        Page page = aPage().withAsset(
                anAsset().withName("http://external.asset").withExternal(true)).build();

        assetExportStep.execute(zipper, page);

        verifyNoInteractions(zipper);
    }
}
