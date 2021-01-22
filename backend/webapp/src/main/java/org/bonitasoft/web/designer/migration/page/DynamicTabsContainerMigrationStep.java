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

package org.bonitasoft.web.designer.migration.page;

import java.util.Optional;

import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicTabsContainerMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(DynamicTabsContainerMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(AbstractPage page) {
            MigrationTabsContainerVisitor collectTabsContainer = new MigrationTabsContainerVisitor();
            page.accept(collectTabsContainer);
            return Optional.empty();
    }
}
