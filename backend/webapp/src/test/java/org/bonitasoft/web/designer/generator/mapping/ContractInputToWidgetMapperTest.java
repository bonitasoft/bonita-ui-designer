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
package org.bonitasoft.web.designer.generator.mapping;

import org.bonitasoft.web.designer.generator.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.generator.widgets.PbInput;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.bonitasoft.web.designer.model.contract.*;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.*;

public class ContractInputToWidgetMapperTest {
    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    @Test
    public void should_string_contract_input_create_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aStringContractInput("firstName"),
                Collections.<List<Element>> emptyList());

        assertThat(element.getId()).isEqualTo("pbInput");
    }

    @Test
    public void should_read_only_string_contract_input_create_an_text_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aReadOnlyStringContractInput("firstName"),
                Collections.<List<Element>> emptyList());

        assertThat(element.getId()).isEqualTo("pbText");
        assertThat(element.getPropertyValues().get("allowHtml").getValue()).isEqualTo(true);
    }

    @Test
    public void should_map_simple_numeric_input_to_an_input_widget_id() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Component element = (Component) contractInputToWidgetMapper.toElement(aLongContractInput("updateTime"),
                Collections.<List<Element>> emptyList());

        assertThat(element.getId()).isEqualTo("pbInput");
    }

    @Test
    public void should_map_input_name_to_a_widget_display_label() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper
                .toElement(aStringContractInput("aComplicatedCamelCaseInputName"), Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("label").getValue()).isEqualTo("A Complicated Camel Case Input Name");
    }

    @Test
    public void should_map_a_numeric_contract_input_to_an_input_with_number_type() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aLongContractInput("timestamp"),
                Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("type").getValue()).isEqualTo("number");
    }

    @Test
    public void should_map_a_non_numeric_contract_input_to_an_input_with_text_type() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aStringContractInput("name"),
                Collections.<List<Element>> emptyList());

        assertThat(element.getPropertyValues().get("type").getValue()).isEqualTo("text");
    }

    @Test
    public void should_keep_default_placeholder_input_when_contract_input_have_description() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        LeafContractInput contractInput = aStringContractInput("name");
        contractInput.setDescription("name of the user");
        Element element = contractInputToWidgetMapper.toElement(contractInput, Collections.<List<Element>> emptyList());
        assertThat(element.getPropertyValues().get("placeholder").getValue()).isEqualTo(new PbInput().getPlaceholder());
    }

    @Test
    public void should_submit_button_have_contract_action() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(ButtonAction.SUBMIT_TASK);

        assertThat(element.getPropertyValues().get("action").getValue()).isEqualTo("Submit task");
    }

    @Test
    public void should_submit_button_send_sentData_variable() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(ButtonAction.SUBMIT_TASK);

        PropertyValue PropertyValue = element.getPropertyValues().get("dataToSend");
        assertThat(PropertyValue.getType()).isEqualTo("expression");
        assertThat(PropertyValue.getValue()).isEqualTo("formOutput");
    }

    @Test
    public void submit_button_should_redirect_to_bonita_on_success() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.createSubmitButton(ButtonAction.SUBMIT_TASK);

        PropertyValue PropertyValue = element.getPropertyValues().get("targetUrlOnSuccess");
        assertThat(PropertyValue.getType()).isEqualTo("interpolation");
        assertThat(PropertyValue.getValue()).isEqualTo("/bonita");
    }

    @Test
    public void should_string_contract_input_has_value_configured_on_sentData() throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Element element = contractInputToWidgetMapper.toElement(aStringContractInput("firstName"),
                Collections.<List<Element>> emptyList());

        PropertyValue valueParameter = element.getPropertyValues().get("value");
        assertThat(valueParameter.getType()).isEqualTo("variable");
        assertThat(valueParameter.getValue()).isEqualTo("formInput.firstName");
    }

    @Test
    public void should_configure_value_property_of_container_with_$item_when_generating_a_multiple_string_in_a_multiple_node_contract_input()
            throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput contractInput = aContractInput("names").mulitple().withType(String.class.getName()).build();
        aNodeContractInput("employee").mulitple()
                .withInput(contractInput).build();
        Element container = contractInputToWidgetMapper.toElement(contractInput,
                new ArrayList<List<Element>>());

        assertThat(container).isInstanceOf(Container.class);
        PropertyValue repeatedCollectionPropetyValue = container.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$item.names");
    }

    @Test
    public void should_configure_value_property_of_container_with_sentData_when_generating_a_multiple_node_input()
            throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        Container container = contractInputToWidgetMapper.toContainer(
                (NodeContractInput) aNodeContractInput("employee").mulitple().build(),
                new ArrayList<List<Element>>());

        PropertyValue repeatedCollectionPropetyValue = container.getPropertyValues().get("repeatedCollection");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("formInput.employee");
    }

    @Test
    public void should_configure_collection_property_of_add_button_with_iterator_if_in_a_repeated_container()
            throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput skills = aNodeContractInput("skills")
                .withDataReference(
                        new BusinessDataReference("skills", "org.test.Skill", RelationType.COMPOSITION, LoadingType.EAGER))
                .mulitple().build();
        aNodeContractInput("employee").mulitple().withInput(skills);
        Component button = contractInputToWidgetMapper.createAddButton(skills);

        PropertyValue labelValue = button.getPropertyValues().get("label");
        assertThat(labelValue.getValue()).isEqualTo("<span class=\"glyphicon glyphicon-plus\"></span> Add Skill");

        PropertyValue repeatedCollectionPropetyValue = button.getPropertyValues().get("collectionToModify");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$item.skills");
    }

    @Test
    public void should_configure_collection_property_of_remove_button_with_iterator_if_in_a_repeated_container()
            throws Exception {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        ContractInput skills = aNodeContractInput("skills").mulitple().build();
        aNodeContractInput("employee").mulitple().withInput(skills);
        Component button = contractInputToWidgetMapper.createRemoveButton();

        PropertyValue repeatedCollectionPropetyValue = button.getPropertyValues().get("collectionToModify");
        assertThat(repeatedCollectionPropetyValue.getType()).isEqualTo("variable");
        assertThat(repeatedCollectionPropetyValue.getValue()).isEqualTo("$collection");
    }

    @Test
    public void should_create_single_document_to_edit() {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        LeafContractInput fileContractInput = ContractInputBuilder.aFileContractInput("aDocument.txt");
        ContractInputDataHandler dataHandler = new ContractInputDataHandler(fileContractInput);
        assertThat(dataHandler.isDocumentEdition()).isFalse();
        fileContractInput.setDataReference(new DataReference("myDoc", File.class.getName()));
        fileContractInput.setMode(EditMode.EDIT);
        assertThat(dataHandler.isDocumentEdition()).isTrue();

        Container container = (Container) contractInputToWidgetMapper.toDocument(fileContractInput);

        Component titleComponent = (Component) container.getRows().get(0).get(0);
        assertThat(titleComponent.getId()).isEqualTo("pbTitle");
        PropertyValue textProperty = titleComponent.getPropertyValues().get("text");
        assertThat(textProperty.getValue()).isEqualTo("My Doc");

        Component fileViewerComponent = (Component) container.getRows().get(1).get(0);
        assertThat(fileViewerComponent.getId()).isEqualTo("pbFileViewer");
        PropertyValue documentProperty = fileViewerComponent.getPropertyValues().get("document");
        assertThat(documentProperty.getValue()).isEqualTo("context.myDoc_ref");

        Component fileUploadComponent = (Component) container.getRows().get(2).get(0);
        assertThat(fileUploadComponent.getId()).isEqualTo("pbUpload");
        PropertyValue labelHiddenProperty = fileUploadComponent.getPropertyValues().get("labelHidden");
        assertThat(labelHiddenProperty.getValue()).isEqualTo(Boolean.TRUE);

        PropertyValue valueProperty = fileUploadComponent.getPropertyValues().get("value");
        assertThat(valueProperty.getValue()).isEqualTo("context.myDoc_ref.newValue");
    }

    @Test
    public void should_create_multiple_document_to_edit_container() {
        ContractInputToWidgetMapper contractInputToWidgetMapper = makeContractInputToWidgetMapper();

        LeafContractInput fileContractInput = ContractInputBuilder.aFileContractInput("aDocument.txt");
        fileContractInput.setMultiple(true);
        fileContractInput.setMode(EditMode.EDIT);
        ContractInputDataHandler dataHandler = new ContractInputDataHandler(fileContractInput);
        fileContractInput.setDataReference(new DataReference("myDoc", File.class.getName()));
        assertThat(dataHandler.isDocumentEdition()).isTrue();

        Element element = contractInputToWidgetMapper.toDocument(fileContractInput);
        assertThat(element).isInstanceOf(Container.class);
        Container container = (Container) element;
        assertThat(container.getRows()).hasSize(2);
        assertThat(container.isRepeated()).isFalse();

        Component title = (Component) container.getRows().get(0).get(0);
        assertThat(title.getId()).isEqualTo("pbTitle");
        Container multipleContainer = (Container) container.getRows().get(1).get(0);

        Map<String, PropertyValue> propertyValues = multipleContainer.getPropertyValues();
        PropertyValue repeatedCollectionProperty = propertyValues.get("repeatedCollection");
        assertThat(repeatedCollectionProperty.getValue()).isEqualTo("context.myDoc_ref");

        Component fileViewerComponent = (Component) multipleContainer.getRows().get(0).get(0);
        assertThat(fileViewerComponent.getId()).isEqualTo("pbFileViewer");
        PropertyValue documentProperty = fileViewerComponent.getPropertyValues().get("document");
        assertThat(documentProperty.getValue()).isEqualTo("$item");

        Component fileUploadComponent = (Component) multipleContainer.getRows().get(1).get(0);
        assertThat(fileUploadComponent.getId()).isEqualTo("pbUpload");
        PropertyValue labelHiddenProperty = fileUploadComponent.getPropertyValues().get("labelHidden");
        assertThat(labelHiddenProperty.getValue()).isEqualTo(Boolean.TRUE);

        PropertyValue valueProperty = fileUploadComponent.getPropertyValues().get("value");
        assertThat(valueProperty.getValue()).isEqualTo("$item.newValue");
    }

    private ContractInputToWidgetMapper makeContractInputToWidgetMapper() {
        return new ContractInputToWidgetMapper(new DimensionFactory(), jsonHandler);
    }

}
