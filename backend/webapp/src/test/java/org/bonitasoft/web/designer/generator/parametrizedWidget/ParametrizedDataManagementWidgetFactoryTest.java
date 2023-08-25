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

package org.bonitasoft.web.designer.generator.parametrizedWidget;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.Container;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

public class ParametrizedDataManagementWidgetFactoryTest implements ParameterConstants {

    private ParametrizedDataManagementWidgetFactory elementFactory;
    private NodeBusinessObjectInput nodeInput;
    private DimensionFactory dimensionFactory;

    @BeforeEach
    public void init() {
        this.elementFactory = new ParametrizedDataManagementWidgetFactory();
        this.dimensionFactory = new DimensionFactory();
        this.nodeInput = new NodeBusinessObjectInput("com.company.model.person");
        this.nodeInput.setMultiple(true);
        this.nodeInput.setPageDataName("person");
    }


    @Test
    public void should_create_read_only_input_widget_when_leaf_is_readOnly() {
        LeafContractInput input = new LeafContractInput("LastName", String.class);
        input.setReadonly(true);

        AbstractParametrizedWidget widget = this.elementFactory.createParametrizedWidget(input);
        assertThat(widget).isInstanceOf(InputWidget.class);
        assertThat(((InputWidget) widget).isReadOnly()).isEqualTo(true);
        assertThat(((InputWidget) widget).getLabel()).isEqualTo("Last Name");
    }

    @Test
    public void should_set_input_widget_value_with_global_variable_name_from_parent_input() {
        LeafContractInput input = new LeafContractInput("lastName", String.class);
        input.setParent(this.nodeInput);
        input.setReadonly(true);
        input.setMandatory(false);

        AbstractParametrizedWidget widget = this.elementFactory.createParametrizedWidget(input);

        assertThat(widget).isInstanceOf(InputWidget.class);
        assertThat(((InputWidget) widget).isReadOnly()).isEqualTo(true);
        assertThat(((InputWidget) widget).isRequired()).isEqualTo(false);
        assertThat(((InputWidget) widget).getValue()).isEqualTo(this.nodeInput.getPageDataNameSelected() + "." + input.getName());
        assertThat(((InputWidget) widget).getLabel()).isEqualTo("Last Name");
    }

    @Test
    public void should_set_dateTime_widget_value_with_global_variable_name_from_parent_input() {
        LeafContractInput input = new LeafContractInput("birthday", LocalDate.class);
        input.setParent(this.nodeInput);
        input.setReadonly(true);

        AbstractParametrizedWidget widget = this.elementFactory.createParametrizedWidget(input);

        assertThat(widget).isInstanceOf(DatePickerWidget.class);
        assertThat(((DatePickerWidget) widget).isReadOnly()).isEqualTo(true);
        assertThat(((DatePickerWidget) widget).getValue()).isEqualTo(this.nodeInput.getPageDataNameSelected() + "." + input.getName());
        assertThat(((InputWidget) widget).getLabel()).isEqualTo("Birthday");
    }

    @Test
    public void create_a_details_container_with_hidden_property() {
        NodeBusinessObjectInput input = new NodeBusinessObjectInput("com.company.model.person");
        input.setPageDataName("person");

        Container container = this.elementFactory.createDetailsWidgetContainer(dimensionFactory,input);

        assertThat(container.getDescription()).isNotEmpty();
        assertThat(container.getPropertyValues().get("hidden").getValue()).isEqualTo("!person_selected");
        assertThat(container.getDimension().get("xs")).isEqualTo(11);
    }

    @Test
    public void create_a_spacing_container() {
        NodeBusinessObjectInput input = new NodeBusinessObjectInput("com.company.model.person");
        input.setPageDataName("person");

        Container container = this.elementFactory.createSpacingContainer(dimensionFactory,input);

        assertThat(container.getDimension().get("xs")).isEqualTo(1);
        assertThat(container.getRows().get(0)).isEqualTo(Collections.emptyList());
        assertThat(container.getPropertyValues().get("hidden").getValue()).isEqualTo("!person_selected");
    }

    @Test
    public void should_set_required_false_for_non_mandatory_contract_input() {
        //InputWidget
        LeafContractInput aInputWidget = aStringContractInput("myString");
        aInputWidget.setMandatory(false);

        AbstractParametrizedWidget widget = createFactory().createParametrizedWidget(aInputWidget);
        assertThat(widget).isInstanceOf(InputWidget.class);

        aInputWidget.setMandatory(true);
        widget = createFactory().createParametrizedWidget(aInputWidget);
        assertThat(widget).isInstanceOf(InputWidget.class);
        assertThat(((InputWidget) widget).isRequired()).isTrue();

        //Multiple
        aInputWidget.setMultiple(true);
        aInputWidget.setMandatory(false);
        widget = createFactory().createParametrizedWidget(aInputWidget);
        assertThat(widget).isInstanceOf(InputWidget.class);
        assertThat(((InputWidget) widget).isRequired()).isTrue();
        assertThat(((InputWidget) widget).getValue()).isEqualTo("$item");
    }

    private ParametrizedDataManagementWidgetFactory createFactory() {
        return new ParametrizedDataManagementWidgetFactory();
    }


}
