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
package org.bonitasoft.web.designer.experimental.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.web.designer.experimental.mapping.dataManagement.BusinessObjectContainer;
import org.bonitasoft.web.designer.experimental.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.AbstractParametrizedWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParametrizedDataManagementWidgetFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TableWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;

public class BusinessDataToWidgetMapper {

    private final DimensionFactory dimensionFactory;
    private BusinessObjectContainer businessObjectContainer;
    private final ParametrizedDataManagementWidgetFactory parametrizedDataManagementWidgetFactory;

    public BusinessDataToWidgetMapper(DimensionFactory dimensionFactory, BusinessObjectContainer businessObjectContainer) {
        this.dimensionFactory = dimensionFactory;
        this.businessObjectContainer = businessObjectContainer;
        parametrizedDataManagementWidgetFactory = new ParametrizedDataManagementWidgetFactory();
    }

    /**
     * Create pattern for master/details display.
     * Adding a TableWidget and 2 container below
     *
     * @param nodeBusinessObjectInput
     * @param rows
     * @return Container in which working
     */
    public Container generateMasterDetailsPattern(NodeBusinessObjectInput nodeBusinessObjectInput, List<List<Element>> rows) {
        TableWidget table = new TableWidget();

        List<String> columns = new ArrayList<>();
        if (!nodeBusinessObjectInput.getInput().isEmpty()) {
            for (ContractInput cInput : nodeBusinessObjectInput.getInput()) {
                columns.add(cInput.getName());
            }
        } else {
            columns = Arrays.asList("id", "name", "description", "data");
        }

        table.setHeaders(columns);
        table.setColumnsKey(columns);
        table.setContent(nodeBusinessObjectInput.getDataName());

        // Create Selected row data
        String selectedVariableName = nodeBusinessObjectInput.getDataNameSelected();
        businessObjectContainer.addVariable(selectedVariableName, new Variable(DataType.CONSTANT, ""));
        table.setSelectedRow(selectedVariableName);

        rows.add(Collections.<Element>singletonList(table.toComponent(dimensionFactory)));

        List<Element> row = createDetailsRow(nodeBusinessObjectInput);
        rows.add(row);
        return (Container) row.get(1);
    }

    public Element toElement(LeafContractInput contractInput) {
        AbstractParametrizedWidget widget = parametrizedDataManagementWidgetFactory.createParametrizedWidget(contractInput);
        return widget.toComponent(dimensionFactory);
    }

    private List<Element> createDetailsRow(NodeBusinessObjectInput nodeBusinessObjectInput) {
        return Arrays.asList(
                parametrizedDataManagementWidgetFactory.createSpacingContainer(dimensionFactory),
                parametrizedDataManagementWidgetFactory.createDetailsWidgetContainer(nodeBusinessObjectInput).toContainer(dimensionFactory));
    }


}
