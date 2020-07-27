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
package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.service.ArtifactService;

public class ArtifactStatusResource {

     public static MigrationStatusReport getStatus(DesignerArtifact artifact, String modelVersion) {
        if(artifact.getArtifactVersion() != null){
            Version artifactVersion = new Version(artifact.getArtifactVersion());
            Version currentVersion = new Version(modelVersion);
            return getStatus(artifactVersion,  currentVersion,null, null);
        }
        return new MigrationStatusReport();
    }

    public static MigrationStatusReport getStatus(Version artifactVersion,  Version currentVersion) {
        return getStatus(artifactVersion,currentVersion, null, null);
    }

    public static MigrationStatusReport getStatus( Version currentVersion, DesignerArtifact artifact) {
        Version artifactVersion = new Version(artifact.getArtifactVersion());
        return getStatus(artifactVersion, currentVersion, artifact, null);
    }

    public static MigrationStatusReport getStatusRecursive( Version currentVersion, DesignerArtifact artifact, ArtifactService service) {
        Version artifactVersion = new Version(artifact.getArtifactVersion());
        return getStatus(artifactVersion, currentVersion, artifact, service);
    }

    public static MigrationStatusReport getStatus(Version artifactVersion, Version currentVersion, DesignerArtifact artifact, ArtifactService service) {
        // Check status of this artifact
        boolean migration = false;
        boolean compatible = true;
        if (artifactVersion == null || currentVersion.isGreaterThan(artifactVersion.toString())) {
            migration = true;
        }
        if (artifactVersion != null && artifactVersion.isGreaterThan(currentVersion.toString())) {
            compatible = false;
        }

        MigrationStatusReport artifactReport = new MigrationStatusReport(compatible, migration);

        if (service == null || artifact == null || !artifactReport.isCompatible()) {
            // no dependencies check needed
            return artifactReport;
        }

        // Check status of dependencies
        MigrationStatusReport depReport = service.getStatus(artifact);
        if (!depReport.isCompatible()) {
            return depReport;
        }
        if (artifactReport.isMigration() != depReport.isMigration()) {
            return new MigrationStatusReport(true, true);
        } else if (artifactReport.isMigration()) {
            return new MigrationStatusReport(true, true);
        }
        return new MigrationStatusReport(true, false);
    }
}
