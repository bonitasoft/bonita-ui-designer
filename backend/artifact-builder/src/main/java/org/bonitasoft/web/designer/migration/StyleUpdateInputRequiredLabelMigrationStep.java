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

import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

public class StyleUpdateInputRequiredLabelMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleUpdateInputRequiredLabelMigrationStep.class);

    private AssetService<Page> assetService;

    public StyleUpdateInputRequiredLabelMigrationStep(AssetService<Page> assetService) {
        this.assetService = assetService;
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page artifact) throws IOException {
        for (var asset : artifact.getAssets()) {
            if (asset.getName().equals("style.css")) {
                String pageStyleCssContent = assetService.getAssetContent(artifact, asset);

                assetService.save(artifact, asset, getMigratedAssetContent(pageStyleCssContent));

                logger.info("[MIGRATION] Update content property in control-required css class in asset [{}] to {} [{}]",
                        asset.getName(), artifact.getType(), artifact.getName());
            }
        }
        return Optional.empty();
    }

    private byte[] getMigratedAssetContent(String styleCssContent) {
        //Regex to catch control-label-required
        var EXTENSION_RESOURCE_REGEX = "((\\.control-label--required:after\\s+\\{)\\s+(content: \"\\*\";))";
        var p = Pattern.compile(EXTENSION_RESOURCE_REGEX);
        var m = p.matcher(styleCssContent);
        var buffer = new StringBuilder();
        while (m.find()) {
            if (m.group(2) != null) {
                //Replace content: "*" by content: " *"
                m.appendReplacement(buffer, m.group(2) + "\n  content: \" *\";");
            } else {
                m.appendTail(buffer);
            }

        }
        m.appendTail(buffer);
        return buffer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
