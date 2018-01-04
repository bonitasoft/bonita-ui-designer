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
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;

@Named
public class UIBootstrapAssetMigrationStep implements MigrationStep<Widget> {

    public static final String ASSET_FILE_NAME = "ui-bootstrap-tpls-0.13.0.min.js";

    private static final String assetPath = "templates/widget/assets/js/";

    private static final Logger logger = LoggerFactory.getLogger(UIBootstrapAssetMigrationStep.class);

    private AssetService<Widget> assetService;

    @Inject
    public UIBootstrapAssetMigrationStep(@Named("widgetAssetService") AssetService<Widget> assetService) {
        this.assetService = assetService;
    }

    @Override
    public void migrate(Widget widget) {

        if (widget.isCustom() && !widgetHasAsset(widget, "ui-bootstrap")) {
            Asset uiBootstrap = new Asset()
                    .setName(ASSET_FILE_NAME)
                    .setType(JAVASCRIPT);

            assetService.save(widget, uiBootstrap, getContent());

            logger.info(format(
                    "[MIGRATION] Adding %s asset [%s] to [%s] (as it was removed from vendor.min.js). You can remove it if you don't need it.",
                    uiBootstrap.getType(), uiBootstrap.getName(), widget.getName()));
        }
    }

    private boolean widgetHasAsset(Widget widget, String assetPrefix) {
        for (Asset asset: widget.getAssets()) {
            if (asset.getName().contains(assetPrefix) && JAVASCRIPT.equals(asset.getType())) {
                return true;
            }
        }
        return false;
    }

    public byte[] getContent() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(assetPath + ASSET_FILE_NAME)) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException("Missing " + assetPath + ASSET_FILE_NAME + " from classpath", e);
        }
    }
}
