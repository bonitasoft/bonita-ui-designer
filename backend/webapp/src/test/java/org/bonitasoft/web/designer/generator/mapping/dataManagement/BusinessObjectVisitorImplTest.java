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

package org.bonitasoft.web.designer.generator.mapping.dataManagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterConstants.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterType;
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.junit.Before;
import org.junit.Test;

public class BusinessObjectVisitorImplTest {

    private BusinessDataToWidgetMapper businessDataToWidgetMapper;

    @Before
    public void setUp() throws Exception {
        businessDataToWidgetMapper = new BusinessDataToWidgetMapper(new DimensionFactory(), new BusinessObjectContainer(new Container()));
    }

    @Test
    public void add_master_details_ui_when_visit_a_node_business_object() throws Exception {
        ElementContainer initContainer = new Container();
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.person");
        node.setPageDataName("Person");
        node.addInput(new LeafContractInput("name", String.class));
        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(initContainer, businessDataToWidgetMapper);

        node.accept(visitor);
        Container container = (Container) initContainer.getRows().get(0).get(0);
        assertThat(container.getRows()).hasSize(3);
        List<Element> tableRow = container.getRows().get(1);
        Component pbTable = ((Component) tableRow.get(0));
        assertThat(tableRow.get(0)).isInstanceOf(Component.class);
        assertThat(pbTable.getId()).isEqualTo("pbTable");
        assertThat(pbTable.getPropertyValues().get(CONTENT_PARAMETER).getValue()).isEqualTo("Person");
        assertThat(pbTable.getPropertyValues().get(CONTENT_PARAMETER).getType()).isEqualTo(ParameterType.EXPRESSION.getValue());

        List<Element> detailsRow = container.getRows().get(2);
        Component component = ((Component) detailsRow.get(0));
        assertThat(detailsRow.get(0)).isInstanceOf(Component.class);
        assertThat(component.getId()).isEqualTo("pbContainer");
        assertThat(component.getDimension().get("xs")).isEqualTo(1);
        assertThat(((Component) detailsRow.get(1)).getDimension().get("xs")).isEqualTo(11);
    }

    @Test
    public void add_master_details_pattern_when_visit_a_child_node_business_object() throws Exception {
        ElementContainer container = new Container();

        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.person", "person");
        node.setMultiple(true);
        node.addInput(new LeafContractInput("name", String.class));

        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("com.company.model.addresses", "person_address", "address");
        childNode.setMultiple(true);
        LeafContractInput a = new LeafContractInput("city", String.class);
        childNode.setDataReference(new BusinessDataReference("addresses", "String", BusinessDataReference.RelationType.AGGREGATION, BusinessDataReference.LoadingType.LAZY));
        childNode.addInput(a);
        node.addInput(childNode);

        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);
        node.accept(visitor);

        Container objectContainer = (Container) container.getRows().get(0).get(0);
        assertThat(objectContainer.getRows()).hasSize(3);
        List<List<Element>> rows = objectContainer.getRows();
        assertThat(((Component) rows.get(0).get(0)).getPropertyValues().get(TEXT_PARAMETER).getValue()).isEqualTo("Person");
        assertThat(((Component) rows.get(1).get(0)).getId()).isEqualTo("pbTable");
        assertThat(((Component) rows.get(1).get(0)).getPropertyValues().get(HEADERS_PARAMETER).getValue()).isEqualTo(Arrays.asList("Name"));

        // Details container nested Section
        Container detailsContainer = (Container) rows.get(2).get(1);
        assertThat(detailsContainer.getId()).isEqualTo("pbContainer");
        assertThat(detailsContainer.getDimension().get("xs")).isEqualTo(11);
        assertThat(detailsContainer.getPropertyValues().get(HIDDEN_PARAMETER).getValue()).isEqualTo("!person_selected");
        assertThat(((Component) detailsContainer.getRows().get(0).get(0)).getPropertyValues().get(LABEL_PARAMETER).getValue()).isEqualTo("Name");

        // ADDRESSES nested Section
        List<List<Element>> addressesContainerObject = ((Container) detailsContainer.getRows().get(1).get(0)).getRows();
        assertThat(addressesContainerObject.size()).isEqualTo(3);
        assertThat(((Component) addressesContainerObject.get(0).get(0)).getPropertyValues().get(TEXT_PARAMETER).getValue()).isEqualTo("Addresses");
        assertThat(((Component) addressesContainerObject.get(1).get(0)).getPropertyValues().get(CONTENT_PARAMETER).getValue()).isEqualTo("person_address");
        assertThat(((Component) addressesContainerObject.get(1).get(0)).getPropertyValues().get(SELECTED_ROW_PARAMETER).getValue()).isEqualTo("person_address_selected");
    }

    @Test
    public void visit_business_object_with_nested_object() throws Exception {
        Container container = new Container();

        NodeBusinessObjectInput person = aComplexNodeObjectInput();

        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);

        person.accept(visitor);
        Container rootContainer = (Container) container.getRows().get(0).get(0);
        assertThat(rootContainer.getRows()).hasSize(3);

        Container detailsContainer = (Container) rootContainer.getRows().get(2).get(1);

        assertThat(detailsContainer.getRows()).hasSize(4);
        assertThat(detailsContainer.getDescription()).isNotEmpty();
    }

    private NodeBusinessObjectInput aComplexNodeObjectInput() {
        NodeBusinessObjectInput person = new NodeBusinessObjectInput("com.company.model.person");
        person.setPageDataName("person");
        person.addInput(new LeafContractInput("name", String.class));
        person.addInput(new LeafContractInput("lastName", String.class));

        NodeBusinessObjectInput address = new NodeBusinessObjectInput("com.company.model.address", "person_address","address");
        address.addInput(new LeafContractInput("city", String.class));
        address.addInput(new LeafContractInput("zipCode", Integer.class));
        address.setDataReference(new BusinessDataReference("address", "String", BusinessDataReference.RelationType.COMPOSITION, BusinessDataReference.LoadingType.EAGER));

        person.addInput(address);

        NodeBusinessObjectInput sport = new NodeBusinessObjectInput("com.company.model.sport","person_sport","sport");
        sport.addInput(new LeafContractInput("Name", String.class));
        sport.addInput(new LeafContractInput("needBalls", Boolean.class));

        LeafContractInput comments = new LeafContractInput("comments", Boolean.class);
        comments.setMultiple(true);
        sport.addInput(comments);
        sport.setPageDataName("sport");
        sport.setDataReference(new BusinessDataReference("sport", "String", BusinessDataReference.RelationType.AGGREGATION, BusinessDataReference.LoadingType.LAZY));

        person.addInput(address);
        return person;
    }

}
