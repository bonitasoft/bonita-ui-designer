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

import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

@Named
public class DataToVariableMigrationStep<T extends AbstractPage > implements MigrationStep<T> {

    private static final Logger logger = LoggerFactory.getLogger(DataToVariableMigrationStep.class);

    @Override
    public void migrate(T artifact) {

        logger.info(format(
                "[MIGRATION] Convert all data to variables in page [%s]",
                artifact.getName())
        );

        Map<String, Data> data = artifact.getData();

        for (Map.Entry<String, Data> dataEntry: data.entrySet()) {
            artifact.addVariable(dataEntry.getKey(), convertDataToVariable(dataEntry.getValue()));
        }

        artifact.setData(null);
    }

    private Variable convertDataToVariable(Data data) {
        String variableValue = Objects.toString(data.getValue(), null);
        return new Variable(data.getType(), variableValue);
    }
}
