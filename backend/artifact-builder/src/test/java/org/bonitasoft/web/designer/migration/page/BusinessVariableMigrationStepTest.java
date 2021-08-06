package org.bonitasoft.web.designer.migration.page;

import junit.framework.TestCase;
import org.bonitasoft.web.designer.builder.DataBuilder;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.builder.VariableBuilder;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.AbstractPage;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

@RunWith(MockitoJUnitRunner.class)
public class BusinessVariableMigrationStepTest extends TestCase {

    BusinessVariableMigrationStep<AbstractPage> businessVariableMigrationStep;

    @Before
    public void setUp() throws Exception {
        businessVariableMigrationStep = new BusinessVariableMigrationStep<>();
    }

    @Test
    public void should_migrate_page_with_business_data() throws Exception {

        var businessDataValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com_company_model_BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":0,\"c\":10}}";
        var expectedValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com.company.model.BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":\"0\",\"c\":\"10\"}}";
        Page pageWithData = aPage().withId("pageWithData")
            .withVariable("aBusinessData", VariableBuilder.aBusinessDataVariable().value(businessDataValue).build()).build();

        businessVariableMigrationStep.migrate(pageWithData);

        assertThat(pageWithData.getVariables().get("aBusinessData").getValue()).isEqualTo(Arrays.asList(expectedValue));

    }

    @Test
    public void should_migrate_fragment_with_business_data() throws Exception {

        var businessDataValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com_company_model_BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":0,\"c\":10}}";
        var expectedValue = "{\"businessObjectName\":\"BusinessObject1\",\"id\":\"com.company.model.BusinessObject1\",\"filters\":[],\"pagination\":{\"p\":\"0\",\"c\":\"10\"}}";
        Fragment fragmentWithData = FragmentBuilder.aFragment().withId("fragmentWithData")
                .withVariable("aBusinessData", VariableBuilder.aBusinessDataVariable().value(businessDataValue).build()).build();

        businessVariableMigrationStep.migrate(fragmentWithData);

        assertThat(fragmentWithData.getVariables().get("aBusinessData").getValue()).isEqualTo(Arrays.asList(expectedValue));
    }


}
