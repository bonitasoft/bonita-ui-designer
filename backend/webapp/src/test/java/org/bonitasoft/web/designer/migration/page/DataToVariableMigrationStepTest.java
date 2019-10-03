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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import java.util.Arrays;

import org.bonitasoft.web.designer.builder.DataBuilder;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataToVariableMigrationStepTest {

    DataToVariableMigrationStep<Page> dataToVariableMigrationStep;

    @Before
    public void setUp() throws Exception {
        dataToVariableMigrationStep = new DataToVariableMigrationStep<Page>();
    }

    @Test
    public void should_migrate_page_with_data() throws Exception {
        Page pageWithData = aPage().withId("pageWithData").withData("myData", DataBuilder.aConstantData().value("default value").build()).build();

        dataToVariableMigrationStep.migrate(pageWithData);

        assertThat(pageWithData.getVariables().get("myData").getValue()).isEqualTo(Arrays.asList("default value"));
        assertThat(pageWithData.getData()).isNull();
    }

    @Test
    public void should_migrate_page_with_data_with_null_value() throws Exception {
        Page pageWithData = aPage().withId("pageWithData").withData("myEmptyData", 
                DataBuilder.aConstantData().exposed(true).build()).build();

        dataToVariableMigrationStep.migrate(pageWithData);

        Variable variable = pageWithData.getVariables().get("myEmptyData");
        assertThat(variable.getValue()).isEmpty();
        assertThat(variable.isExposed()).isTrue();
        assertThat(pageWithData.getData()).isNull();
    }


}
