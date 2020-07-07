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

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class StyleAssetMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleAssetMigrationStep.class);

    private AssetService<Page> assetService;

    @Inject
    public StyleAssetMigrationStep(@Named("pageAssetService") AssetService<Page> assetService) {
        this.assetService = assetService;
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page artifact) {
        Asset style = new Asset()
                .setName(getAssetName(artifact))
                .setType(CSS);

        assetService.save(artifact, style, getContent());

        logger.info(format(
                "[MIGRATION] Adding default CSS asset [%s] to %s [%s] (introduced in 1.4.8)",
                style.getName(), artifact.getType(), artifact.getName()));
        return Optional.empty();
    }

    private String getAssetName(Page artifact) {
        String name = "style%s.css";
        String suffix = "";
        while (artifact.hasAsset(CSS, format(name, suffix))) {
            suffix = nextSuffix(suffix);
        }
        return format(name, suffix);
    }

    private String nextSuffix(String suffix) {
        return isBlank(suffix) ? "1" : String.valueOf(Integer.valueOf(suffix) + 1);
    }

    public byte[] getContent() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("templates/page/assets/css/style.css")) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException("Missing templates/page/assets/css/style.css from classpath", e);
        }
    }
}
