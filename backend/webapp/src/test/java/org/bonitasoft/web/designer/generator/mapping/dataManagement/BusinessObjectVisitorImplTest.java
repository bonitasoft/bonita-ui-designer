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
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.generator.parametrizedWidget.ParameterConstants.*;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;

public class BusinessObjectVisitorImplTest {

    private BusinessDataToWidgetMapper businessDataToWidgetMapper;

    @BeforeEach
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
        assertThat(component.getDimension()).containsEntry("xs",1);
        assertThat(detailsRow.get(1).getDimension()).containsEntry("xs",11);
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
        assertThat(rows.get(0).get(0).getPropertyValues().get(TEXT_PARAMETER).getValue()).isEqualTo("Person");
        assertThat(((Component) rows.get(1).get(0)).getId()).isEqualTo("pbTable");
        assertThat(rows.get(1).get(0).getPropertyValues().get(HEADERS_PARAMETER).getValue()).isEqualTo(Arrays.asList("Name"));

        // Details container nested Section
        Container detailsContainer = (Container) rows.get(2).get(1);
        assertThat(detailsContainer.getId()).isEqualTo("pbContainer");
        assertThat(detailsContainer.getDimension()).containsEntry("xs",11);
        assertThat(detailsContainer.getPropertyValues().get(HIDDEN_PARAMETER).getValue()).isEqualTo("!person_selected");
        assertThat(detailsContainer.getRows().get(0).get(0).getPropertyValues().get(LABEL_PARAMETER).getValue()).isEqualTo("Name");

        // ADDRESSES nested Section
        List<List<Element>> addressesContainerObject = ((Container) detailsContainer.getRows().get(1).get(0)).getRows();
        assertThat(addressesContainerObject.size()).isEqualTo(3);
        assertThat(addressesContainerObject.get(0).get(0).getPropertyValues().get(TEXT_PARAMETER).getValue()).isEqualTo("Addresses");
        assertThat(addressesContainerObject.get(1).get(0).getPropertyValues().get(CONTENT_PARAMETER).getValue()).isEqualTo("person_address");
        assertThat(addressesContainerObject.get(1).get(0).getPropertyValues().get(SELECTED_ROW_PARAMETER).getValue()).isEqualTo("person_address_selected");
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

        assertThat(detailsContainer.getRows()).hasSize(3);
        assertThat(detailsContainer.getDescription()).isNotEmpty();
    }

    @Test
    public void visit_business_object_with_complex_object() {
        Container container = new Container();

        NodeBusinessObjectInput invoice = aInvoiceInvoiceLineProductInCompositionAndEager();

        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);

        invoice.accept(visitor);
        Container rootContainer = (Container) container.getRows().get(0).get(0);
        assertThat(rootContainer.getRows()).hasSize(3);

        Container invoiceDetailsContainer = (Container) rootContainer.getRows().get(2).get(1);

        assertThat(invoiceDetailsContainer.getRows()).hasSize(2);
        assertThat(invoiceDetailsContainer.getDescription()).isNotEmpty();

        // Product Section
        Container invoiceLineDetailsContainer = (Container) ((Container) invoiceDetailsContainer.getRows().get(1).get(0)).getRows().get(2).get(1);
        assertThat(invoiceLineDetailsContainer.getRows()).hasSize(3);

        Component quantity = (Component) invoiceLineDetailsContainer.getRows().get(1).get(0);
        assertThat(quantity.getPropertyValues().get("value").getValue()).isEqualTo("invoice_invoiceLines_selected.price");
        //assertThat(quantity.getPropertyValues().get("hidden").getValue()).isEqualTo("invoice_invoiceLines_selected.price");
        Component price = (Component) invoiceLineDetailsContainer.getRows().get(1).get(0);
        assertThat(price.getPropertyValues().get("value").getValue()).isEqualTo("invoice_invoiceLines_selected.price");


        Container productDetailsContainer = (Container) ((Container) invoiceLineDetailsContainer.getRows().get(2).get(0)).getRows().get(1).get(1);
        assertThat(productDetailsContainer.getRows()).hasSize(2);
        Component productNameInput = (Component) productDetailsContainer.getRows().get(0).get(0);
        assertThat(productNameInput.getPropertyValues().get("value").getValue()).isEqualTo("invoice_invoiceLines_selected.product.name");
    }

    @Test
    public void should_no_generated_container_for_business_object_relation_when_it_dont_have_attribute() {
        Container container = new Container();

        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.customer");
        node.setPageDataName("Customer");
        node.addInput(new LeafContractInput("name", String.class));

        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("com.company.model.adress", "adress", "adress");
        childNode.setDataReference(new BusinessDataReference("customer_adress", "String", BusinessDataReference.RelationType.AGGREGATION,
                BusinessDataReference.LoadingType.LAZY));
        node.addInput(childNode);

        BusinessObjectVisitorImpl visitor = new BusinessObjectVisitorImpl(container, businessDataToWidgetMapper);

        node.accept(visitor);

        Container rootContainer = (Container) container.getRows().get(0).get(0);
        assertThat(rootContainer.getRows()).hasSize(3);

        Container detailsContainer = (Container) rootContainer.getRows().get(2).get(1);

        assertThat(detailsContainer.getRows()).hasSize(1);
        assertThat(detailsContainer.getDescription()).isNotEmpty();
    }


    private NodeBusinessObjectInput aInvoiceInvoiceLineProductInCompositionAndEager() {
        NodeBusinessObjectInput node = new NodeBusinessObjectInput("com.company.model.invoice", "invoice");
        node.addInput(new LeafContractInput("date", String.class));
        node.setMultiple(true);
        NodeBusinessObjectInput childNode = new NodeBusinessObjectInput("com.company.model.invoiceLine", "invoice_invoiceLines", "invoiceLine");
        childNode.addInput(aContractInput("quantity").withType(Integer.class.getName()).build());
        childNode.addInput(aContractInput("price").withType(Integer.class.getName()).build());
        childNode.setDataReference(new BusinessDataReference("invoice_invoiceLines", "String", BusinessDataReference.RelationType.COMPOSITION,
                BusinessDataReference.LoadingType.EAGER));
        childNode.setMultiple(true);
        node.addInput(childNode);

        NodeBusinessObjectInput product = new NodeBusinessObjectInput("com.company.model.product", "invoice_invoiceLines", "product");
        product.addInput(aContractInput("name").withType(String.class.getName()).build());
        product.addInput(aContractInput("labels").mulitple().withType(String.class.getName()).build());
        product.setDataReference(new BusinessDataReference("invoice_invoiceLines", "String", BusinessDataReference.RelationType.AGGREGATION,
                BusinessDataReference.LoadingType.EAGER));
        childNode.addInput(product);
        return node;
    }


    private NodeBusinessObjectInput aComplexNodeObjectInput() {
        NodeBusinessObjectInput person = new NodeBusinessObjectInput("com.company.model.person");
        person.setPageDataName("person");
        person.setMultiple(true);
        person.addInput(new LeafContractInput("name", String.class));
        person.addInput(new LeafContractInput("lastName", String.class));

        NodeBusinessObjectInput address = new NodeBusinessObjectInput("com.company.model.address", "person_address", "address");
        address.addInput(new LeafContractInput("city", String.class));
        address.addInput(new LeafContractInput("zipCode", Integer.class));
        address.setDataReference(new BusinessDataReference("address", "String", BusinessDataReference.RelationType.COMPOSITION, BusinessDataReference.LoadingType.EAGER));

        person.addInput(address);

        NodeBusinessObjectInput sport = new NodeBusinessObjectInput("com.company.model.sport", "person_sport", "sport");
        sport.addInput(new LeafContractInput("Name", String.class));
        sport.addInput(new LeafContractInput("needBalls", Boolean.class));

        LeafContractInput comments = new LeafContractInput("comments", Boolean.class);
        comments.setMultiple(true);
        sport.addInput(comments);

        sport.setPageDataName("sport");
        sport.setDataReference(new BusinessDataReference("sport", "String", BusinessDataReference.RelationType.AGGREGATION, BusinessDataReference.LoadingType.LAZY));
        return person;
    }

}
