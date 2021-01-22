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

import java.util.Optional;
import java.util.UUID;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class AssetIdMigrationStep<A extends Identifiable & Assetable> extends AbstractMigrationStep<A> {

    private static final Logger logger = LoggerFactory.getLogger(AssetIdMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(A artifact) {
        for (Asset asset : artifact.getAssets()) {
            if (asset.getId() == null) {
                asset.setId(UUID.randomUUID().toString());
                logger.info(format("[MIGRATION] A uuid <%s> has been added to asset <%s> (Id was introduced in 1.0.2)", asset.getId(), asset.getName()));
            }
        }
        return Optional.empty();
    }
}
