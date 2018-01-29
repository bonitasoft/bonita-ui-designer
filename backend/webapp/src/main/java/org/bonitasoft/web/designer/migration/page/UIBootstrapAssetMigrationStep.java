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

package org.bonitasoft.web.designer.migration.page;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;

@Named
public class UIBootstrapAssetMigrationStep implements MigrationStep<Page> {

    public static final String ASSET_FILE_NAME = "ui-bootstrap-tpls-0.13.0.min.js";

    private static final Logger logger = LoggerFactory.getLogger(UIBootstrapAssetMigrationStep.class);

    private AssetService<Page> pageAssetService;

    private ComponentVisitor componentVisitor;

    private WidgetRepository widgetRepository;

    @Inject
    public UIBootstrapAssetMigrationStep(@Named("pageAssetService") AssetService<Page> pageAssetService,
                                         ComponentVisitor componentVisitor, WidgetRepository widgetRepository) {
        this.pageAssetService = pageAssetService;
        this.componentVisitor = componentVisitor;
        this.widgetRepository = widgetRepository;
    }

    @Override
    public void migrate(Page page) {

        if (!pageHasAsset(page, "ui-bootstrap") && !widgetHasAsset(page, "ui-bootstrap")) {
            Asset uiBootstrap = new Asset()
                    .setName(ASSET_FILE_NAME)
                    .setType(JAVASCRIPT);

            pageAssetService.save(page, uiBootstrap, getContent());

            logger.info(format(
                    "[MIGRATION] Adding %s asset [%s] to [%s] (as it was removed from vendor.min.js). You can remove it if you don't need it.",
                    uiBootstrap.getType(), uiBootstrap.getName(), page.getName()));
        }
    }

    private boolean widgetHasAsset(Page page, String assetNameFilter) {
        for (Component component : page.accept(componentVisitor)) {
            Widget widget = widgetRepository.get(component.getId());
            if(widget.isCustom()) {
                for (Asset asset : widget.getAssets()) {
                    if (asset.getName().contains(assetNameFilter) && JAVASCRIPT.equals(asset.getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean pageHasAsset(Page page, String assetPrefix) {
        for (Asset asset: page.getAssets()) {
            if (asset.getName().contains(assetPrefix) && JAVASCRIPT.equals(asset.getType())) {
                return true;
            }
        }
        return false;
    }

    public byte[] getContent() {
        try (InputStream is = this.getClass().getResourceAsStream(ASSET_FILE_NAME)) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new RuntimeException("Missing " + this.getClass().getPackage() + "/" + ASSET_FILE_NAME + " from classpath", e);
        }
    }
}
