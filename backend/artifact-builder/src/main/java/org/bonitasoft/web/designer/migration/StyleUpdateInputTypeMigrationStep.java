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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleUpdateInputTypeMigrationStep extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleUpdateInputTypeMigrationStep.class);
    private static final String REGEX_SELECTOR = "(.*)(input\\[type='(?:text|url|password|number)']\\.ng-invalid\\.ng-dirty \\{)";
    String replacement = "$1input[type='url'].ng-invalid.ng-dirty,\n$2";
    private final AssetService<Page> pageAssetService;

    public StyleUpdateInputTypeMigrationStep(AssetService<Page> pageAssetService) {
        this.pageAssetService = pageAssetService;
    }

    @Override
    public Optional<MigrationStepReport> migrate(Page page) throws IOException {
        Pattern pattern = Pattern.compile(REGEX_SELECTOR);
        for (var asset : page.getAssets()) {
            if (asset.getName().equals("style.css")) {
                String newContent = pageAssetService.getAssetContent(page, asset);
                Matcher matcher = pattern.matcher(newContent);
                if (matcher.find()) {
                    String updatedStyleContent = newContent.replaceAll(REGEX_SELECTOR,replacement);
                    pageAssetService.save(page, asset, updatedStyleContent.getBytes());
                    logger.info("[MIGRATION] Update input style in asset [{}] for {} [{}]",
                            asset.getName(), page.getType(), page.getName());
                }
            }
        }
        return Optional.empty();
    }
}
