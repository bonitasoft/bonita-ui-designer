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

import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;

import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddModelVersionMigrationStep<A extends DesignerArtifact> extends AbstractMigrationStep<A> {

    @Override
    public Optional<MigrationStepReport> migrate(A artifact) throws Exception {
        artifact.setModelVersion(MigrationConfig.INITIAL_MODEL_VERSION);
        return Optional.empty();
    }

}
