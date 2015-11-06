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

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetExternalMigrationStep<A extends Identifiable & Assetable> implements MigrationStep<A> {

    private static final Logger logger = LoggerFactory.getLogger(AssetExternalMigrationStep.class);

    @Override
    public void migrate(A artifact) {
        for (Asset asset : artifact.getAssets()) {
            asset.setExternal(isAssetURL(asset.getName()));
            logger.info(
                    format("<%s> asset with id <%s> has been identified as %s (external property was introduced in 1.2)", asset.getName(), asset.getId(),
                            (asset.isExternal() ? "external" : "internal")));
        }
    }

    public static boolean isAssetURL(String name) {
        return name != null && (name.startsWith("http:") || name.startsWith("https:"));
    }
}
