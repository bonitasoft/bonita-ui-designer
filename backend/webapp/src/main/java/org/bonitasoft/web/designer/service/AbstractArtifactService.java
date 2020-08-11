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
package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.Identifiable;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractArtifactService<T extends Identifiable> implements ArtifactService {

    @Value("${designer.modelVersion}")
    protected String modelVersion;

    public MigrationStatusReport getStatus(Identifiable artifact) {
        return getArtifactStatus(artifact);
    }

    private MigrationStatusReport getArtifactStatus(Identifiable artifact) {
        // Check status of this artifact
        if(artifact.getArtifactVersion() == null){
            return new MigrationStatusReport(true,true);
        }
        boolean migration = new Version(modelVersion).isGreaterThan(artifact.getArtifactVersion());
        boolean compatible = !new Version(artifact.getArtifactVersion()).isGreaterThan(modelVersion);

        return new MigrationStatusReport(compatible, migration);
    }

    public MigrationStatusReport mergeStatusReport(MigrationStatusReport artifactReport, MigrationStatusReport dependenciesReport) {

        boolean isCompatible = artifactReport.isCompatible() && dependenciesReport.isCompatible();
        boolean needMigration = isCompatible && (artifactReport.isMigration() || dependenciesReport.isMigration());

        return new MigrationStatusReport(isCompatible, needMigration);
    }

    /**
     * Return status of artifact without checking dependencies
     * @param artifact
     * @return MigrationStatusReport
     */
    public MigrationStatusReport getStatusWithoutDependencies(Identifiable artifact) {
        return getArtifactStatus(artifact);
    }

}
