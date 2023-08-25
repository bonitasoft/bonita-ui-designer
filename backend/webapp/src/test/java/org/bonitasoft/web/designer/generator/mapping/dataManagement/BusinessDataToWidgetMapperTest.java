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

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

public class BusinessDataToWidgetMapperTest {

    @Test
    public void should_string_contract_input_create_an_input_widget_id() throws Exception {

        BusinessDataToWidgetMapper contractInputToWidgetMapper = makeBusinessDataToWidgetMapper(new BusinessObjectContainer());

        List<Element> element = contractInputToWidgetMapper.toElement(aStringContractInput("firstName"));
        Component a = (Component) element.get(0);
        assertThat(a.getId()).isEqualTo("pbInput");
    }

    @Test
    public void should_configure_value_property_of_input_with_$item_when_generating_a_multiple_string_in_a_multiple_leaf_contract_input() {
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(new BusinessObjectContainer());

        ContractInput contractInput = aContractInput("names").mulitple().withType(String.class.getName()).build();
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.person", "person");
        node.setMultiple(true);
        node.addInput(contractInput);

        List<Element> elements = businessDataToWidgetMapper.toElement((LeafContractInput) contractInput);

        Container container = (Container) elements.get(1);
        assertThat(container).isInstanceOf(Container.class);
        PropertyValue repeatedCollectionPropetyValue = container.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("person_selected.names");

        Element input = container.getRows().get(0).get(0);
        PropertyValue inputPropertyValue = input.getPropertyValues().get("value");
        assertThat(inputPropertyValue.getValue()).isEqualTo("$item");
    }

    @Test
    public void should_generate_title_and_table_widgets_pattern_when_node_is_read() {
        BusinessObjectContainer boc = new BusinessObjectContainer();
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(boc);

        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.person", "person");
        node.addInput(aContractInput("addresses").mulitple().withType(String.class.getName()).build());
        node.addInput(aContractInput("firstName").withType(String.class.getName()).build());
        node.addInput(aContractInput("lastName").withType(String.class.getName()).build());

        businessDataToWidgetMapper.generateMasterDetailsPattern(node, boc.getContainer().getRows());

        Component title = (Component) boc.getContainer().getRows().get(0).get(0);
        assertThat(title.getId()).isEqualTo("pbTitle");
        PropertyValue titleCollectionPropertyValue = title.getPropertyValues().get("text");
        assertThat(titleCollectionPropertyValue.getType()).isEqualTo("interpolation");
        assertThat(titleCollectionPropertyValue.getValue()).isEqualTo("Person");

        Component tableWidget = (Component) boc.getContainer().getRows().get(1).get(0);
        assertThat(tableWidget.getId()).isEqualTo("pbTable");

        Map<String, Variable> variables = boc.getBusinessObjectVariable();
        assertThat(variables).hasSize(1);
        assertThat(variables.get("person_selected")).isNotNull();
    }

    @Test
    public void should_generate_external_url_on_master_details_pattern_when_nested_object_is_lazy_loading() {
        BusinessObjectContainer boc = new BusinessObjectContainer();
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(boc);

        NodeBusinessObjectInput childNode = aNestedObjectInLazyAggregation(true);
        childNode.setMultiple(true);

        businessDataToWidgetMapper.generateMasterDetailsPattern(childNode, boc.getContainer().getRows());

        Component title = (Component) boc.getContainer().getRows().get(0).get(0);
        assertThat(title.getId()).isEqualTo("pbTitle");
        PropertyValue titleCollectionPropertyValue = title.getPropertyValues().get("text");
        assertThat(titleCollectionPropertyValue.getType()).isEqualTo("interpolation");
        assertThat(titleCollectionPropertyValue.getValue()).isEqualTo("Addresses");

        Component tableWidget = (Component) boc.getContainer().getRows().get(1).get(0);
        assertThat(tableWidget.getId()).isEqualTo("pbTable");

        Map<String, PropertyValue> tableCollectionPropertyValue = tableWidget.getPropertyValues();
        assertThat(tableCollectionPropertyValue.get("content").getValue()).isEqualTo("customer_addresses");
        assertThat(tableCollectionPropertyValue.get("selectedRow").getValue()).isEqualTo("customer_addresses_selected");
        assertThat(tableCollectionPropertyValue.get("headers").getValue()).isEqualTo(Arrays.asList("Addresses"));

        Map<String, Variable> variables = boc.getBusinessObjectVariable();
        assertThat(variables).hasSize(2);
        assertThat(variables.get("customer_addresses_selected")).isNotNull();
        assertThat(variables.get("customer_addresses").getType()).isEqualTo(DataType.URL);
        assertThat(variables.get("customer_addresses").getValue().get(0)).isEqualTo("{{customer_selected|lazyRef:'addresses'}}");
    }

    @Test
    public void should_generate_master_details_when_nested_object_is_loading_on_composition_and_eager() {
        BusinessObjectContainer boc = new BusinessObjectContainer();
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(boc);
        NodeBusinessObjectInput node = aNestedObjectInCompositionAndEager();
        node.setMultiple(true);

        businessDataToWidgetMapper.generateMasterDetailsPattern(node, boc.getContainer().getRows());

        Component title = (Component) boc.getContainer().getRows().get(0).get(0);
        assertThat(title.getId()).isEqualTo("pbTitle");
        PropertyValue titleCollectionPropertyValue = title.getPropertyValues().get("text");
        assertThat(titleCollectionPropertyValue.getType()).isEqualTo("interpolation");
        assertThat(titleCollectionPropertyValue.getValue()).isEqualTo("Sport");

        Component tableWidget = (Component) boc.getContainer().getRows().get(1).get(0);
        assertThat(tableWidget.getId()).isEqualTo("pbTable");

        Map<String, PropertyValue> tableCollectionPropertyValue = tableWidget.getPropertyValues();
        assertThat(tableCollectionPropertyValue.get("content").getValue()).isEqualTo("person_selected.sport");
        assertThat(tableCollectionPropertyValue.get("selectedRow").getValue()).isEqualTo("person_sport_selected");
        assertThat(tableCollectionPropertyValue.get("headers").getValue()).isEqualTo(Arrays.asList("Name", "Need Balls"));

        Map<String, Variable> variables = boc.getBusinessObjectVariable();
        assertThat(variables).hasSize(1);
        assertThat(variables.get("person_sport_selected")).isNotNull();
    }

    @Test
    public void should_not_generate_table_widget_when_child_object_is_not_multiple() {
        BusinessObjectContainer boc = new BusinessObjectContainer();
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(boc);

        NodeBusinessObjectInput childNode = aNestedObjectInLazyAggregation(true);
        childNode.setMultiple(false);

        businessDataToWidgetMapper.generateMasterDetailsPattern(childNode, boc.getContainer().getRows());

        Component title = (Component) boc.getContainer().getRows().get(0).get(0);
        assertThat(title.getId()).isEqualTo("pbTitle");
        PropertyValue titleCollectionPropertyValue = title.getPropertyValues().get("text");
        assertThat(titleCollectionPropertyValue.getType()).isEqualTo("interpolation");
        assertThat(titleCollectionPropertyValue.getValue()).isEqualTo("Addresses");

        List<Element> detailsRows = boc.getContainer().getRows().get(1);
        assertThat(detailsRows.size()).isEqualTo(2);

        Map<String, Variable> variables = boc.getBusinessObjectVariable();
        assertThat(variables.get("customer_addresses").getType()).isEqualTo(DataType.URL);
        assertThat(variables.get("customer_addresses").getValue().get(0)).isEqualTo("{{customer_selected|lazyRef:'addresses'}}");
    }

    @Test
    public void should_generated_repeatable_container_when_contract_input_is_multiple() {
        BusinessObjectContainer boc = new BusinessObjectContainer();
        BusinessDataToWidgetMapper businessDataToWidgetMapper = makeBusinessDataToWidgetMapper(boc);

        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.invoice.model.customer", "customer");
        node.setMultiple(true);
        node.addInput(aContractInput("labels").withType(String.class.getName()).mulitple().build());

        LeafContractInput contractInput = (LeafContractInput) node.getInput().get(0);
        List<Element> elements = businessDataToWidgetMapper.toElement(contractInput);

        assertThat(elements.size()).isEqualTo(2);
        assertThat(((Component) elements.get(0)).getId()).isEqualTo("pbTitle");

        Container multipleContainer = (Container) elements.get(1);
        assertThat(multipleContainer.getPropertyValues().get("repeatedCollection").getValue()).isEqualTo("customer_selected.labels");
    }

    private NodeBusinessObjectInput aNestedObjectInCompositionAndEager() {
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.person", "person");
        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("com.company.model.sport", "person_sport", "sport");
        childNode.addInput(aContractInput("name").withType(String.class.getName()).build());
        childNode.addInput(aContractInput("needBalls").withType(Boolean.class.getName()).build());
        childNode.setDataReference(new BusinessDataReference("sport", "String", RelationType.COMPOSITION, LoadingType.EAGER));
        node.addInput(childNode);
        return childNode;
    }

    private NodeBusinessObjectInput aNestedObjectInLazyAggregation(Boolean parentIsMultiple) {
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.invoice.model.customer", "customer");
        node.setMultiple(parentIsMultiple);
        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("com.invoice.model.addresses", "customer_addresses", "addresses");
        childNode.addInput(aContractInput("addresses").withType(String.class.getName()).build());
        childNode.setDataReference(new BusinessDataReference("addresses", "String", RelationType.AGGREGATION, LoadingType.LAZY));
        node.addInput(childNode);
        return childNode;
    }


    private BusinessDataToWidgetMapper makeBusinessDataToWidgetMapper(BusinessObjectContainer boc) {
        return new BusinessDataToWidgetMapper(new DimensionFactory(), boc);
    }

}
