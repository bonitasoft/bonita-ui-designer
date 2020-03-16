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

package org.bonitasoft.web.designer.experimental.parametrizedWidget;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;

import org.bonitasoft.web.designer.experimental.mapping.DimensionFactory;
import org.bonitasoft.web.designer.experimental.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.page.Container;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParametrizedDataManagementWidgetFactoryTest implements ParameterConstants {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ParametrizedDataManagementWidgetFactory elementFactory;
    private NodeBusinessObjectInput nodeInput;

    @Before
    public void init() {
        this.elementFactory = new ParametrizedDataManagementWidgetFactory();
        this.nodeInput = new NodeBusinessObjectInput("com_company_model_person");
        this.nodeInput.setDataName("person");
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
        LeafContractInput input = new LeafContractInput("LastName", String.class);
        input.setParent(this.nodeInput);
        input.setReadonly(true);

        AbstractParametrizedWidget widget = this.elementFactory.createParametrizedWidget(input);
        assertThat(widget).isInstanceOf(InputWidget.class);
        assertThat(((InputWidget) widget).isReadOnly()).isEqualTo(true);
        assertThat(((InputWidget) widget).getValue()).isEqualTo(this.nodeInput.getDataNameSelected() + "." + input.getName());
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
        assertThat(((DatePickerWidget) widget).getValue()).isEqualTo(this.nodeInput.getDataNameSelected() + "." + input.getName());
        assertThat(((InputWidget) widget).getLabel()).isEqualTo("Birthday");
    }

    @Test
    public void create_a_details_container_with_hidden_property() {
        NodeBusinessObjectInput input = new NodeBusinessObjectInput("com_company_model_person");
        input.setDataName("person");
        WidgetContainer container = this.elementFactory.createDetailsWidgetContainer(input);

        assertThat(container.getHidden()).isEqualTo("!person_selected");
        assertThat(container.getDimension()).isEqualTo(11);
    }

    @Test
    public void create_a_spacing_container() {
        DimensionFactory dimensionFactory = new DimensionFactory();
        Container container = this.elementFactory.createSpacingContainer(dimensionFactory);

        assertThat(container.getDimension().get("xs")).isEqualTo(1);
        assertThat(container.getRows().get(0)).isEqualTo(Collections.emptyList());
    }


}
