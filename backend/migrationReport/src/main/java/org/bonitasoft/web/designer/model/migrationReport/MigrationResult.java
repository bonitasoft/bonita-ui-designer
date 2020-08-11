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

import static org.bonitasoft.web.designer.model.migrationReport.MigrationStatus.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MigrationResult<T> {

    private T artifact;
    private List<MigrationStepReport> migrationStepReportList = new ArrayList<>();

    public MigrationResult(T artifact, List<MigrationStepReport> migrationStepReportList) {
        this.artifact = artifact;
        this.migrationStepReportList = migrationStepReportList;
    }

    public List<MigrationStepReport> getMigrationStepReportList() {
        return migrationStepReportList;
    }

    public void setMigrationStepReportList(List<MigrationStepReport> migrationStepReportList) {
        this.migrationStepReportList = migrationStepReportList;
    }

    public T getArtifact() {
        return artifact;
    }

    public void setArtifact(T artifact) {
        this.artifact = artifact;
    }

    /**
     * Return list of report filter by final status
     * ex:
     * If finalStatus is SUCCESS, we return an empty list
     * If finalStatus is ERROR or WARNING, we return ERROR and WARNING step report
     *
     * @return
     */
    public List<MigrationStepReport> getMigrationStepReportListFilterByFinalStatus() {
        return migrationStepReportList.stream().filter(stepReport -> {
            return getStatusList().contains(stepReport.getMigrationStatus());
        }).collect(Collectors.toList());
    }

    public MigrationStatus getFinalStatus() {
        return containsMigrationStatus(ERROR) ? ERROR : (containsMigrationStatus(WARNING) ? WARNING : SUCCESS);
    }

    private boolean containsMigrationStatus(MigrationStatus migrationStatus) {
        List<MigrationStepReport> list = migrationStepReportList.stream().filter(stepReport -> migrationStatus.getValue().equals(stepReport.getMigrationStatus().getValue())).collect(Collectors.toList());
        return list.size() > 0;
    }

    private List<MigrationStatus> getStatusList() {
        switch (getFinalStatus()) {
            case SUCCESS:
                return Collections.emptyList();
            case ERROR:
            case WARNING:
                return Arrays.asList(ERROR, WARNING);
            default:
                return Arrays.asList(SUCCESS, ERROR, WARNING, MigrationStatus.NONE);
        }
    }


}
