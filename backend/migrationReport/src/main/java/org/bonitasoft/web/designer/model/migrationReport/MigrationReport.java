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
package org.bonitasoft.web.designer.model.migrationReport;

import java.util.Collections;
import java.util.List;


public class MigrationReport {

    private String type;
    private String comments;
    private String status;
    private String elementId;
    private String previousArtifactVersion;
    private String newArtifactVersion;
    private List<MigrationStepReport> migrationStepReport = Collections.emptyList();

    public MigrationReport() {}

    public MigrationReport(MigrationStatus status, String elementId) {
        this.elementId = elementId;
        this.status = status.getValue();
    }

    public MigrationReport(MigrationStatus status, String elementId, String type, String previousArtifactVersion, String newArtifactVersion, List<MigrationStepReport> migrationStepReport) {
        this.elementId = elementId;
        this.type = type;
        this.status = status.getValue();
        this.previousArtifactVersion = previousArtifactVersion;
        this.newArtifactVersion = newArtifactVersion;
        this.migrationStepReport = migrationStepReport;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(MigrationStatus status) {
        this.status = status.getValue();
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPreviousArtifactVersion() {
        return previousArtifactVersion;
    }

    public void setPreviousArtifactVersion(String previousArtifactVersion) {
        this.previousArtifactVersion = previousArtifactVersion;
    }

    public String getNewArtifactVersion() {
        return newArtifactVersion;
    }

    public void setNewArtifactVersion(String newArtifactVersion) {
        this.newArtifactVersion = newArtifactVersion;
    }

    public List<MigrationStepReport> getMigrationStepReport() {
        return migrationStepReport;
    }

    public void setMigrationStepReport(List<MigrationStepReport> migrationStepReport) {
        this.migrationStepReport = migrationStepReport;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}


