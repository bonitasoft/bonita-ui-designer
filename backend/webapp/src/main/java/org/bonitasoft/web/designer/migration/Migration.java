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
    private final Version newArtifactVersion;
    private final MigrationStep<A>[] migrationSteps;

    /**
     * Associate one or more migration steps to a given artifact version.
     * Migration steps will be executed on models which artifact version is lower or equal than the given artifact version.
     *
     * @param newArtifactVersion   Latest artifact version that does need migration (can be UI designer version or model version)
     * @param migrationSteps    The migration steps that need to be executed
     */
    public Migration(String newArtifactVersion, MigrationStep<A>... migrationSteps) {
        this.newArtifactVersion = new Version(newArtifactVersion);
        this.migrationSteps = migrationSteps;
    }

    public void migrate(A artifact) {
        String artifactVersion = artifact.getArtifactVersion();
        if (artifactVersion == null || newArtifactVersion.isGreaterThan(artifactVersion)) {
            logger.info(format("[MIGRATION] %s <%s> with id <%s> is being migrated from version <%s> to <%s>...",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    artifact.getId(),
                    getDisplayVersion(artifactVersion), getDisplayVersion(newArtifactVersion.toString())));

            for (MigrationStep<A> migrationStep : migrationSteps) {
                migrationStep.migrate(artifact);
            }

            updateVersion(artifact);
            logger.info(format("[MIGRATION] %s <%s> artifact version is now <%s>",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    newArtifactVersion));
        }
    }

    private String getDisplayVersion(String artifactVersion) {
        if (artifactVersion == null) {
            return "null";
        }
        if (new Version(artifactVersion).isGreaterOrEqualThan(MigrationConfig.INITIAL_MODEL_VERSION)) {
            return format("model version <%s>", artifactVersion);
        } else {
            return format("UI Designer version <%s>", artifactVersion);
        }
    }

    private void updateVersion(A artifact) {
        if (isModelVersionMigration()) {
            artifact.setModelVersion(newArtifactVersion.toString());
        } else {
            // Migrating from a UID version to a newer UID version
            artifact.setDesignerVersion(newArtifactVersion.toString());
        }
    }

    private boolean isModelVersionMigration() {
        return newArtifactVersion.isGreaterOrEqualThan(MigrationConfig.INITIAL_MODEL_VERSION);
    }
}
