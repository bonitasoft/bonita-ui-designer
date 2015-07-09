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

import java.util.List;

import org.bonitasoft.web.designer.model.Versioned;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Migration<A extends Versioned> {

    protected static final Logger logger = LoggerFactory.getLogger(Migration.class);
    private final String version;
    private final List<MigrationStep<A>> migrationSteps;

    public Migration(String version, List<MigrationStep<A>> migrationSteps) {
        this.version = version;
        this.migrationSteps = migrationSteps;
    }

    public void migrate(A artifact) {
        if (version.compareTo(artifact.getDesignerVersion()) > 0) {

            logger.info(format("Migrate <%s> with id <%s> from %s to %s...",
                    artifact.getName(),
                    artifact.getId(),
                    artifact.getDesignerVersion(), version));

            for (MigrationStep<A> migrationStep : migrationSteps) {
                migrationStep.migrate(artifact);
            }

            artifact.setDesignerVersion(version);
        }
    }
}
