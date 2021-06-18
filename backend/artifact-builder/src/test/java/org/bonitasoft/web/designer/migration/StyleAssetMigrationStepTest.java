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
package org.bonitasoft.web.designer.migration;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.utils.FakePageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StyleAssetMigrationStepTest {

    @Mock
    private AssetRepository<Page> assetRepository;

    private final PageRepository pageRepository = new FakePageRepository();

    @InjectMocks
    private StyleAssetMigrationStep step;

    @Before
    public void setUp() throws Exception {
        AssetService<Page> pageAssetService = new AssetService<>(pageRepository, assetRepository, null);
        var uiDesignerProperties = new UiDesignerPropertiesBuilder()
                .workspacePath(Path.of("target/workspace/"))
                .build();
        uiDesignerProperties.getWorkspaceUid().setExtractPath(Path.of("src/test/resources"));
        step = new StyleAssetMigrationStep(uiDesignerProperties,pageAssetService);
    }

    private Asset expectedAsset(String name) {
        return anAsset().withType(CSS).withName(name).build();
    }

    private byte[] expectedAssetContent() throws IOException {
        return IOUtils.toByteArray(
                this.getClass().getClassLoader().getResourceAsStream("templates/page/assets/css/style.css"));
    }

    @Test
    public void should_add_new_style_asset_to_migrated_pages() throws Exception {
        Page page = aPage().withDesignerVersion("1.4.7").build();

        step.migrate(page);

        Page migratedPage = pageRepository.get(page.getId());
        assertThat(migratedPage.getAssets()).contains(expectedAsset("style.css"));
        verify(assetRepository).save(page.getId(), expectedAsset("style.css"), expectedAssetContent());
    }

    @Test
    public void should_add_new_style_asset_with_different_name_while_already_existing() throws Exception {
        Page page = aPage().withDesignerVersion("1.4.7")
                .withAsset(anAsset().withType(CSS).withName("style.css"))
                .withAsset(anAsset().withType(CSS).withName("style1.css")).build();

        step.migrate(page);

        Page migratedPage = pageRepository.get(page.getId());
        assertThat(migratedPage.getAssets()).contains(expectedAsset("style2.css"));
        verify(assetRepository).save(page.getId(), expectedAsset("style2.css"), expectedAssetContent());
    }
}
