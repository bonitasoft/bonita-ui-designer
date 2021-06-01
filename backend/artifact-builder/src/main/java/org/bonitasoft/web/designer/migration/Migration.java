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

import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public class Migration<A extends DesignerArtifact> {

    private static final Logger logger = LoggerFactory.getLogger(Migration.class);

    private final Version newArtifactVersion;

    private final MigrationStep<A>[] migrationSteps;

    /**
     * Associate one or more migration steps to a given artifact version.
     * Migration steps will be executed on models which artifact version is lower or equal than the given artifact version.
     *
     * @param newArtifactVersion Latest artifact version that does need migration (can be UI designer version or model version)
     * @param migrationSteps     The migration steps that need to be executed
     */
    public Migration(String newArtifactVersion, MigrationStep<A>... migrationSteps) {
        this.newArtifactVersion = new Version(newArtifactVersion);
        this.migrationSteps = migrationSteps;
    }

    public List<MigrationStepReport> migrate(A artifact) {
        var msr = new ArrayList<MigrationStepReport>();

        var artifactVersion = artifact.getArtifactVersion();
        if (artifactVersion == null || newArtifactVersion.isGreaterThan(artifactVersion)) {
            logger.info(
                    "[MIGRATION] {} <{}> with id <{}> is being migrated from version <{}> to <{}>...",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    artifact.getId(),
                    getDisplayVersion(artifactVersion), getDisplayVersion(newArtifactVersion.toString()));

            for (var migrationStep : migrationSteps) {
                try {
                    var report = migrationStep.migrate(artifact);
                    var stepReport = report.map(r -> {
                        r.setVersion(newArtifactVersion.toString());
                        return r;
                    }).orElseGet(MigrationStepReport::successMigrationReport);
                    msr.add(stepReport);
                } catch (Exception e) {
                    msr.add(MigrationStepReport.errorMigrationReport(artifact.getId(), migrationStep.getErrorMessage()));
                }
            }
            updateVersion(artifact);
            logger.info("[MIGRATION] {} <{}> artifact version is now <{}>",
                    artifact.getClass().getSimpleName(),
                    artifact.getName(),
                    newArtifactVersion);

            return msr;
        }
        return msr;
    }

    private String getDisplayVersion(String artifactVersion) {
        if (artifactVersion == null) {
            return "null";
        }
        if (new Version(artifactVersion).isGreaterOrEqualThan(Version.INITIAL_MODEL_VERSION)) {
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
        return newArtifactVersion.isGreaterOrEqualThan(Version.INITIAL_MODEL_VERSION);
    }
}
