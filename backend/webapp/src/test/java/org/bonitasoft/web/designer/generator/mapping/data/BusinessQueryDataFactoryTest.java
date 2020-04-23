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
package org.bonitasoft.web.designer.generator.mapping.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractBuilder.aContractWithDataRefAndAggregation;

import java.util.Set;

import org.bonitasoft.web.designer.model.contract.EditMode;
import org.junit.Test;

public class BusinessQueryDataFactoryTest {


    @Test
    public void should_create_queryBusinessData_for_aggregated_dataRef_in_contract() throws Exception {
        BusinessQueryDataFactory factory = new BusinessQueryDataFactory();

        Set<PageData> data = factory.create(aContractWithDataRefAndAggregation(EditMode.EDIT));

        assertThat(data).hasSize(1);
        assertThat(data.iterator().next().name()).isEqualTo("employee_query");
    }

}
