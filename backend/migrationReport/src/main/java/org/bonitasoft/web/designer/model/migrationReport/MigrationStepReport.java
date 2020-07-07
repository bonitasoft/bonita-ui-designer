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

public class MigrationStepReport {

    private String version;
    private String comments;
    private String artifactId;
    private String stepInfo;
    private MigrationStatus migrationStatus;

    public MigrationStepReport(MigrationStatus migrationStatus, String artifactId, String comments, String stepInfo) {
        this.artifactId = artifactId;
        this.comments = comments;
        this.migrationStatus = migrationStatus;
        this.stepInfo = stepInfo;
    }

    public MigrationStepReport(MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public MigrationStepReport(MigrationStatus migrationStatus, String artifactId) {
        this.migrationStatus = migrationStatus;
        this.artifactId = artifactId;
    }

    public MigrationStepReport(MigrationStatus migrationStatus, String artifactId, String stepInfo) {
        this.migrationStatus = migrationStatus;
        this.artifactId = artifactId;
        this.stepInfo = stepInfo;
    }

    public static MigrationStepReport successMigrationReport() {
        return new MigrationStepReport(MigrationStatus.SUCCESS);
    }

    public static MigrationStepReport warningMigrationReport(String artifactId, String message, String stepInfo) {
        return new MigrationStepReport(MigrationStatus.WARNING, artifactId, message, stepInfo);
    }

    public static MigrationStepReport errorMigrationReport(String artifactId, String stepInfo) {
        return new MigrationStepReport(MigrationStatus.ERROR, artifactId, stepInfo);
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getStepInfo() {
        return stepInfo;
    }

    public void setStepInfo(String stepInfo) {
        this.stepInfo = stepInfo;
    }
}
