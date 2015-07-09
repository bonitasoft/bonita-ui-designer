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
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Test;

public class AssetIdMigrationStepTest {

    @Test
    public void should_migrate_artifact_assets() throws Exception {
        AssetIdMigrationStep<Page> migrationStep = new AssetIdMigrationStep<>();

        Page page = aPage()
                .withAsset(anAsset()
                        .withId(null))
                .build();

        migrationStep.migrate(page);

        assertThat(page.getAssets().iterator().next().getId()).isNotNull();
    }

    @Test
    public void should_not_change_an_asset_id() throws Exception {
        AssetIdMigrationStep<Page> migrationStep = new AssetIdMigrationStep<>();

        Page page = aPage()
                .withAsset(anAsset()
                        .withId("123"))
                .build();

        migrationStep.migrate(page);

        assertThat(page.getAssets().iterator().next().getId()).isEqualTo("123");
    }
}
