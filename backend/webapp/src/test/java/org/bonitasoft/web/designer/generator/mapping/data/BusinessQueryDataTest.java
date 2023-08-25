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

import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.page.PageData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessQueryDataTest {

    @Test
    public void should_add_external_api_variable_on_page_when_contract_contains_an_aggregated_data_ref() throws Exception {
        PageData queryData = new BusinessQueryData(new BusinessDataReference("manager","com.company.model.EmployeeObject", RelationType.AGGREGATION, LoadingType.EAGER));

        Data data = queryData.create();

        assertThat(queryData.name()).isEqualTo("employeeObject_query");
        assertThat(data.getType()).isEqualTo(DataType.URL);
        assertThat(data.getValue()).isEqualTo("../API/bdm/businessData/com.company.model.EmployeeObject?q=find&p=0&c=99");
    }

    @Test
    public void should_BusinessQueryData_with_the_same_name_be_equal() throws Exception {
        PageData queryData = new BusinessQueryData(new BusinessDataReference("manager","com.company.model.EmployeeObject", RelationType.AGGREGATION, LoadingType.EAGER));
        PageData queryData2 = new BusinessQueryData(new BusinessDataReference("deleguee","com.company.model.EmployeeObject", RelationType.AGGREGATION, LoadingType.EAGER));

        assertThat(queryData).isEqualTo(queryData2);
    }

}
