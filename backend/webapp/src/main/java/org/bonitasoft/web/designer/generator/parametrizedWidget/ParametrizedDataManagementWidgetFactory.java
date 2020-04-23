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

import java.util.Collections;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObjectDataHandler;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.WidgetDescription;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.page.Container;


public class ParametrizedDataManagementWidgetFactory extends ParametrizedWidgetFactory {

    private InputTypeResolver inputTypeResolver;


    public ParametrizedDataManagementWidgetFactory() {
        super();
        inputTypeResolver = new InputTypeResolver();
    }

    @Override
    public AbstractParametrizedWidget createParametrizedWidget(ContractInput input) {
        switch (inputTypeResolver.getContractInputType(input)) {
            case TEXT:
                return super.createInputWidget(input, InputType.TEXT);
            case NUMERIC:
                return super.createInputWidget(input, InputType.NUMBER);
            case LOCAL_DATE:
                return super.createDatePicker(input);
            case LOCAL_DATE_TIME:
                return super.createDateTimePicker(input, false);
            case OFFSET_DATE_TIME:
                return super.createDateTimePicker(input, true);
            case BOOLEAN:
                return super.createCheckBox(input);
            default:
                throw new IllegalStateException(
                        String.format("Unable to create a widget for contract input %s.", input.getName()));
        }
    }

    public Container createDetailsWidgetContainer(DimensionFactory dimensionFactory, NodeBusinessObjectInput businessObjectInput) {
        WidgetContainer container = new WidgetContainer();

        if(businessObjectInput.isRootOrMultipleInput()){
            container.setHidden(new StringBuilder("!").append(businessObjectInput.getPageDataNameSelected()).toString());
        }
        container.setDimension(11);
        Container detailsContainer = container.toContainer(dimensionFactory);
        detailsContainer.setDescription(WidgetDescription.DETAILS_CONTAINER.displayValue(businessObjectInput.formatName()));
        return detailsContainer;
    }

    @Override
    public TitleWidget createTitle(String title) {
        TitleWidget widget = super.createTitle(title);
        widget.setText(title);
        widget.setLevel("Level 4");
        return widget;
    }

    /**
     * Create an empty Container with empty row
     *
     * @param dimensionFactory
     * @return
     */
    public Container createSpacingContainer(DimensionFactory dimensionFactory, NodeBusinessObjectInput businessObjectInput) {
        WidgetContainer container = new WidgetContainer();
        if(businessObjectInput.isRootOrMultipleInput()) {
            container.setHidden(new StringBuilder("!").append(businessObjectInput.getPageDataNameSelected()).toString());
        }

        container.setDimension(1);
        return container.toContainer(dimensionFactory).addNewRow(Collections.emptyList());
    }

   @Override
    protected String getValue(ContractInput contractInput) {
        return contractInput.isMultiple()
                ? multipleInputValue(contractInput)
                : new BusinessObjectDataHandler(contractInput).inputValue();
    }

    @Override
    protected String multipleInputValue(ContractInput contractInput) {
        return ITEM_ITERATOR;
    }

}
