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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class StyleUpdateInputRequiredLabelMigrationStep implements MigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleUpdateInputRequiredLabelMigrationStep.class);

    private AssetService<Page> assetService;

    @Inject
    public StyleUpdateInputRequiredLabelMigrationStep(@Named("pageAssetService") AssetService<Page> assetService) {
        this.assetService = assetService;
    }

    @Override
    public void migrate(Page artifact) {
        for (Asset asset : artifact.getAssets()) {
            if (asset.getName().equals("style.css")) {
                try {
                    String pageStyleCssContent = assetService.getAssetContent(artifact, asset);

                    assetService.save(artifact, asset, getMigratedAssetContent(pageStyleCssContent));

                    logger.info(format(
                            "[MIGRATION] Update content property in control-required css class in asset [%s] to %s [%s]",
                            asset.getName(), artifact.getType(), artifact.getName()));
                } catch (IOException e) {
                    logger.error("An error occurred during migration", e);
                }
            }
        }
    }

    private byte[] getMigratedAssetContent(String styleCssContent) {
        //Regex to catch control-label-required
        final String EXTENSION_RESOURCE_REGEX = "((\\.control-label--required:after\\s+\\{)\\s+(content: \"\\*\";))";
        Pattern p = Pattern.compile(EXTENSION_RESOURCE_REGEX);
        Matcher m = p.matcher(styleCssContent);
        StringBuffer buffer = new StringBuffer();
        while (m.find()) {
            if (m.group(2) != null) {
                //Replace content: "*" by content: " *"
                m.appendReplacement(buffer, m.group(2) + "\n  content: \" *\";");
            } else {
                m.appendTail(buffer);
            }

        }
        m.appendTail(buffer);
        return buffer.toString().getBytes();
    }
}
