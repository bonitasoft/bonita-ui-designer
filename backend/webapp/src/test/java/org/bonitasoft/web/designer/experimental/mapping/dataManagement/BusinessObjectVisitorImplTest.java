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

package org.bonitasoft.web.designer.experimental.mapping.dataManagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterConstants.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.designer.experimental.mapping.BusinessDataToWidgetMapper;
import org.bonitasoft.web.designer.experimental.mapping.DimensionFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterConstants;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterType;
import org.bonitasoft.web.designer.model.ElementContainer;
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
        ElementContainer container = new Container();
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("Person");
        node.setDataName("Person");
        node.addInput(new LeafContractInput("name", String.class));
        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);

        node.accept(visitor);

        assertThat(container.getRows()).hasSize(2);
        List<Element> firstRow = container.getRows().get(0);
        Component pbTable = ((Component) firstRow.get(0));
        assertThat(firstRow.get(0)).isInstanceOf(Component.class);
        assertThat(pbTable.getId()).isEqualTo("pbTable");
        assertThat(pbTable.getPropertyValues().get(CONTENT_PARAMETER).getValue()).isEqualTo("Person");
        assertThat(pbTable.getPropertyValues().get(CONTENT_PARAMETER).getType()).isEqualTo(ParameterType.EXPRESSION.getValue());

        List<Element> detailsRow = container.getRows().get(1);
        Component component = ((Component) detailsRow.get(0));
        assertThat(detailsRow.get(0)).isInstanceOf(Component.class);
        assertThat(component.getId()).isEqualTo("pbContainer");
        assertThat(component.getDimension().get("xs")).isEqualTo(1);
        assertThat(((Component) detailsRow.get(1)).getDimension().get("xs")).isEqualTo(11);
    }

    @Test
    public void add_master_details_pattern_when_visit_a_child_node_business_object() throws Exception {
        ElementContainer container = new Container();
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("Person", "Person");
        node.addInput(new LeafContractInput("name", String.class));
        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("address", "person_selected");
        childNode.addInput(new LeafContractInput("city", String.class));


        node.addInput(childNode);

        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);
        node.accept(visitor);

        Container detailsContainer = (Container) container.getRows().get(1).get(1);
        assertThat(detailsContainer.getRows()).hasSize(3);
        List<List<Element>> rows = detailsContainer.getRows();
        assertThat(((Component) rows.get(0).get(0)).getPropertyValues().get(LABEL_PARAMETER).getValue()).isEqualTo("Name");
        assertThat(((Component) rows.get(1).get(0)).getId()).isEqualTo("pbTable");
        assertThat(((Component) rows.get(1).get(0)).getPropertyValues().get(HEADERS_PARAMETER).getValue()).isEqualTo(Arrays.asList("City"));

        assertThat(((Component) rows.get(2).get(1)).getId()).isEqualTo("pbContainer");
        assertThat(((Component) rows.get(2).get(1)).getDimension().get("xs")).isEqualTo(11);
        assertThat(((Component) ((Container) rows.get(2).get(1)).getRows().get(0).get(0)).getPropertyValues().get("label").getValue()).isEqualTo("City");
    }

}
