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
package org.bonitasoft.web.designer.model.migrationReport.builders;

import org.bonitasoft.web.designer.model.migrationReport.MigrationReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;

public class MigrationReportBuilder {

    private MigrationReport migrationReport;

    private MigrationReportBuilder(MigrationReport migrationReport) {
        this.migrationReport = migrationReport;
    }

    public static MigrationReportBuilder aMigrationReport() {
        return new MigrationReportBuilder(new MigrationReport());
    }

    public static MigrationReport aSuccessMigrationReport() {
        return aMigrationReport().withStatus(MigrationStatus.SUCCESS).withElementId("myPage")
                .build();

    }

    public MigrationReportBuilder withStatus(MigrationStatus migrationStatus) {
        migrationReport.setStatus(migrationStatus);
        return this;
    }

    public MigrationReportBuilder withElementId(String elementId) {
        migrationReport.setElementId(elementId);
        return this;
    }

    public MigrationReport build() {
        return migrationReport;
    }
}
