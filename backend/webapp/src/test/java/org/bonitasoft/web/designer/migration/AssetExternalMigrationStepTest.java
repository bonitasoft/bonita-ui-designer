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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;

public class AssetExternalMigrationStepTest {

    @Test
    public void should_migrate_artifact_assets() throws Exception {
        AssetExternalMigrationStep<Page> migrationStep = new AssetExternalMigrationStep<>();

        Page page = aPage()
                .withAsset(anAsset()
                        .withName("bonita.jpg"))
                .withAsset(anAsset()
                        .withName("http://www.bonitasoft.com"))
                .build();

        migrationStep.migrate(page);

        assertThat(page.getAssets())
                .extracting("name", "external")
                .containsOnly(
                        tuple("bonita.jpg", false),
                        tuple("http://www.bonitasoft.com", true));
    }

    @Test
    public void should_not_change_an_asset_external_property() throws Exception {
        AssetExternalMigrationStep<Page> migrationStep = new AssetExternalMigrationStep<>();

        Page page = aPage()
                .withAsset(anAsset()
                        .withExternal(false).withName("bonita.jpg"))
                .withAsset(anAsset()
                        .withExternal(true).withName("https://www.bonitasoft.com"))
                .build();

        migrationStep.migrate(page);

        assertThat(page.getAssets())
                .extracting("name", "external")
                .containsOnly(
                        tuple("bonita.jpg", false),
                        tuple("https://www.bonitasoft.com", true));
    }
}
