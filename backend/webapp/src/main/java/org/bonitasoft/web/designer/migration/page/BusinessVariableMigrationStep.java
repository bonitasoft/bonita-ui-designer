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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Named;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bonitasoft.web.designer.migration.AbstractMigrationStep;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class BusinessVariableMigrationStep<T extends AbstractPage> extends AbstractMigrationStep<Page> {

    private static final Logger logger = LoggerFactory.getLogger(BusinessVariableMigrationStep.class);

    @Override
    public Optional<MigrationStepReport> migrate(Page page) throws IOException {
        for (Variable variable : page.getVariables().values()) {
            if (variable.getType() == DataType.BUSINESSDATA) {
                String property = variable.getValue().get(0);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode businessObjectNode;
                businessObjectNode = mapper.readTree(property);


                // 'id' property: replace '_' separator by '.'
                JsonNode idNode = businessObjectNode.get("id");
                String newId = idNode.asText().replace('_', '.');
                ((ObjectNode) businessObjectNode).put("id", newId);

                // Remove 'qualifiedName' property
                ((ObjectNode) businessObjectNode).remove("qualifiedName");

                // 'pagination' property: values are now strings (instead of numbers)
                JsonNode paginationNode = businessObjectNode.get("pagination");
                JsonNode pNode = paginationNode.get("p");
                ((ObjectNode) paginationNode).put("p", pNode.asText());
                JsonNode cNode = paginationNode.get("c");
                ((ObjectNode) paginationNode).put("c", cNode.asText());

                List<String> values = new ArrayList<>();
                values.add(businessObjectNode.toString());
                variable.setValue(values);
            }
        }
        return Optional.empty();
    }

    @Override
    public String getErrorMessage() {
        return "An error occurred during business variable migration";
    }

}
