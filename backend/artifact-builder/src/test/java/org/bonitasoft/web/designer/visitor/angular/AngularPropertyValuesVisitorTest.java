package org.bonitasoft.web.designer.visitor.angular;

import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;


@RunWith(MockitoJUnitRunner.class)
public class AngularPropertyValuesVisitorTest {
    @Rule
    public TestResource testResource = new TestResource(this.getClass());

    @InjectMocks
    private AngularPropertyValuesVisitor propertyValuesVisitor;

    private PropertyValue propertyValue;

    @Before
    public void setUp() throws Exception {
        propertyValue = new PropertyValue();
        propertyValue.setType("bar");
        propertyValue.setValue("baz");
    }

    @Test
    public void should_generate_a_service_containing_parameter_values() throws Exception {
        Component component = aComponent().withPropertyValue("foo", "bar", "baz").withReference("component-ref").build();
        Page page = aPage().with(component).build();

        String service = propertyValuesVisitor.generate(page);
//        Assert.assertEquals(service,"");

        assertThat(service).isEqualTo(testResource.load("propertiesValues.result.ts"));
    }

}
