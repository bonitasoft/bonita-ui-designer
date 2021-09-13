package org.bonitasoft.web.designer.visitor.angular;

import junit.framework.TestCase;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;

@RunWith(MockitoJUnitRunner.class)
public class AngularVariableVisitorTest extends TestCase {

    @Rule
    public TestResource testResource = new TestResource(this.getClass());

    @InjectMocks
    private AngularVariableVisitor variableModelVisitor;

    private Variable variable;

    @Before
    public void setUp() throws Exception {
        variable = aConstantVariable().value("bar").build();
    }

    @Test
    public void should_generate_a_factory_based_on_model_found_in_the_page() throws Exception {
        Page page = aPage()
                .withId("page-id")
                .withVariable("foo", variable)
                .build();

        assertThat(variableModelVisitor.generate(page)).isEqualTo(testResource.load("variableModel.result.ts"));
    }

}
