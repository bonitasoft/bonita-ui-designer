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

import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static java.lang.String.format;

public class DataExposedMigrationStep<T extends Fragment> extends AbstractMigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataExposedMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(T artifact) {
        artifact.getVariables().values().stream()
                .filter(variable -> variable.isExposed())
                .forEach(var -> {
                    var.setType(DataType.CONSTANT);
                    var.setDisplayValue("");
                });

        logger.info(format(
                "[MIGRATION] Set type to constant for each exposed data into fragments [%s]",
                artifact.getName())
        );
        return Optional.empty();
    }
}
