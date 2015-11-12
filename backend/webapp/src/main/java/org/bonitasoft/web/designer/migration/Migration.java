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

import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration<A extends DesignerArtifact> {

    private static final Logger logger = LoggerFactory.getLogger(Migration.class);
    private final Version version;
    private final MigrationStep<A>[] migrationSteps;

    public Migration(String version, MigrationStep<A>... migrationSteps) {
        this.version = new Version(version);
        this.migrationSteps = migrationSteps;
    }

    public void migrate(A artifact) {
        if (artifact.getDesignerVersion() == null || version.isGreaterThan(artifact.getDesignerVersion())) {

            logger.info(format("%s <%s> with id <%s> is being migrated from version <%s> to <%s>...",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    artifact.getId(),
                    artifact.getDesignerVersion(), version));

            for (MigrationStep<A> migrationStep : migrationSteps) {
                migrationStep.migrate(artifact);
            }

            artifact.setDesignerVersion(version.toString());
            logger.info(format("%s <%s> version is now <%s>",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    version));
        }
    }
}
