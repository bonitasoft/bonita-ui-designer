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

import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;

public class StyleAssetMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleAssetMigrationStep.class);

    private final AssetService<Page> assetService;
    private final UiDesignerProperties uiDesignerProperties;

    public StyleAssetMigrationStep(UiDesignerProperties uiDesignerProperties, AssetService<Page> assetService) {
        this.assetService = assetService;
        this.uiDesignerProperties = uiDesignerProperties;
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page artifact) {
        var style = new Asset()
                .setName(getAssetName(artifact))
                .setType(CSS);

        assetService.save(artifact, style, getContent());

        logger.info("[MIGRATION] Adding default CSS asset [{}] to {} [{}] (introduced in 1.4.8)",
                style.getName(), artifact.getType(), artifact.getName());
        return Optional.empty();
    }

    private String getAssetName(Page artifact) {
        var name = "style%s.css";
        var suffix = "";
        while (artifact.hasAsset(CSS, format(name, suffix))) {
            suffix = nextSuffix(suffix);
        }
        return format(name, suffix);
    }

    private String nextSuffix(String suffix) {
        return isBlank(suffix) ? "1" : String.valueOf(parseInt(suffix) + 1);
    }

    public byte[] getContent() {
        var defaultStyleFile = "templates/page/assets/css/style.css";
        try {
            return Files.readAllBytes(uiDesignerProperties.getWorkspaceUid().getExtractPath().resolve(defaultStyleFile));
        } catch (IOException e) {
            throw new ArtifactBuilderException("Missing " + defaultStyleFile + " from classpath", e);
        }
    }
}
