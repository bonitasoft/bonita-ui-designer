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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class StyleAddModalContainerPropertiesMigrationStep implements MigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(StyleAddModalContainerPropertiesMigrationStep.class);

    private AssetService<Page> assetService;

    @Inject
    public StyleAddModalContainerPropertiesMigrationStep(@Named("pageAssetService") AssetService<Page> assetService) {
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
                            "[MIGRATION] Adding modalContainer classes in asset [%s] to %s [%s] (introduced in 1.8.28)",
                            asset.getName(), artifact.getType(), artifact.getName()));
                } catch (IOException e) {
                    logger.error("An error occurred during migration",e);
                }
            }
        }
    }

    private byte[] getMigratedAssetContent(String styleCssContent) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("templates/migration/assets/css/styleAddModalContainerProperties.css");
            List<InputStream> streams = Arrays.asList(
                    new ByteArrayInputStream(styleCssContent.getBytes()),
                    new ByteArrayInputStream(IOUtils.toByteArray(is)));
            SequenceInputStream sis = new SequenceInputStream(Collections.enumeration(streams));
            return IOUtils.toByteArray(sis);
        } catch (IOException e) {
            throw new RuntimeException("Missing templates/page/assets/css/style.css from classpath", e);
        }
    }
}
