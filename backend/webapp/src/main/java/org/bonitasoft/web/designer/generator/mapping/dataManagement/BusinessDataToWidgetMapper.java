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

import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.generator.mapping.data.BusinessDataLazyRef;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ParametrizedDataManagementWidgetFactory;
import org.bonitasoft.web.designer.generator.parametrizedWidget.TableWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusinessDataToWidgetMapper {


    private final DimensionFactory dimensionFactory;
    private final BusinessObjectContainer businessObjectContainer;
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
        rows.add(Collections.<Element>singletonList(createTitle(nodeBusinessObjectInput.formatName())));

        if (isRootOrMultipleInput(nodeBusinessObjectInput)) {
            var table = addTableWidget(nodeBusinessObjectInput);
            rows.add(Collections.<Element>singletonList(table.toComponent(dimensionFactory)));
        } else {
            if (nodeBusinessObjectInput.getDataReference() != null && nodeBusinessObjectInput.getDataReference().getLoadingType().equals(BusinessDataReference.LoadingType.LAZY)) {
                var content = generateVariable(nodeBusinessObjectInput);
                businessObjectContainer.addVariable(nodeBusinessObjectInput.getPageDataName(), new Variable(DataType.URL, content.create().getValue().toString()));
            }
        }

        var row = createDetailsRow(nodeBusinessObjectInput);
        rows.add(row);

        return (Container) row.get(1);
    }

    private boolean isRootOrMultipleInput(NodeBusinessObjectInput nodeBusinessObjectInput) {
        return nodeBusinessObjectInput.isMultiple() || nodeBusinessObjectInput.getParent() == null;
    }

    private TableWidget addTableWidget(NodeBusinessObjectInput nodeBusinessObjectInput) {
        var table = new TableWidget();

        var columns = extractHeaders(nodeBusinessObjectInput);
        table.setHeaders(columns);
        table.setColumnsKey(columns);

        String selectedVariableName;

        if (nodeBusinessObjectInput.getParent() == null) {
            table.setContent(nodeBusinessObjectInput.getPageDataName());
            selectedVariableName = nodeBusinessObjectInput.getPageDataNameSelected();
        } else {
            var parent = (NodeBusinessObjectInput) nodeBusinessObjectInput.getParent();
            if (nodeBusinessObjectInput.getDataReference() != null && nodeBusinessObjectInput.getDataReference().getLoadingType().equals(BusinessDataReference.LoadingType.LAZY)) {
                var content = generateVariable(nodeBusinessObjectInput);
                businessObjectContainer.addVariable(nodeBusinessObjectInput.getPageDataName(), new Variable(DataType.URL, content.create().getValue().toString()));
                table.setContent(nodeBusinessObjectInput.getPageDataName());
            } else if (nodeBusinessObjectInput.getDataReference() != null && nodeBusinessObjectInput.getDataReference().getLoadingType().equals(BusinessDataReference.LoadingType.EAGER)) {
                table.setContent(parent.getPageDataNameSelected().concat(".").concat(nodeBusinessObjectInput.getBusinessObjectAttributeName()));
            }
            selectedVariableName = nodeBusinessObjectInput.getPageDataNameSelected();
        }

        // Create Selected row data
        businessObjectContainer.addVariable(selectedVariableName, new Variable(DataType.CONSTANT, ""));
        table.setSelectedRow(selectedVariableName);
        return table;
    }

    private List<String> extractHeaders(NodeBusinessObjectInput nodeBusinessObjectInput) {
        List<String> columns = new ArrayList<>();
        if (!nodeBusinessObjectInput.getInput().isEmpty()) {
            for (var cInput : nodeBusinessObjectInput.getInput()) {
                if (isSimpleAttribute(cInput)) {
                    columns.add(cInput.getName());
                }
            }
        }
        return columns;
    }

    public List<Element> toElement(LeafContractInput contractInput) {
        return contractInput.isMultiple()
                ? toMultipleComponent(contractInput)
                : toSimpleComponent(contractInput);
    }

    private boolean isSimpleAttribute(ContractInput cInput) {
        return !cInput.isMultiple() && cInput.getInput().isEmpty();
    }

    private BusinessDataLazyRef generateVariable(NodeBusinessObjectInput nodeBusinessObjectInput) {
        return new BusinessDataLazyRef(nodeBusinessObjectInput.getPageDataName(), nodeBusinessObjectInput.getDataName(nodeBusinessObjectInput), nodeBusinessObjectInput.getBusinessObjectAttributeName());
    }

    private List<Element> toSimpleComponent(ContractInput contractInput) {
        var widget = parametrizedDataManagementWidgetFactory.createParametrizedWidget(contractInput);
        return List.of(widget.toComponent(dimensionFactory));
    }

    private List<Element> toMultipleComponent(ContractInput contractInput) {
        var widgetContainer = new WidgetContainer();

        if (contractInput.isMultiple()) {
            widgetContainer.setRepeatedCollection(new BusinessObjectDataHandler(contractInput).inputValue());
        }

        var component = parametrizedDataManagementWidgetFactory.createParametrizedWidget(contractInput);
        List<Element> row = new ArrayList<>();

        row.add(component.toComponent(dimensionFactory));

        var container = widgetContainer.toContainer(dimensionFactory);
        container.getRows().add(row);

        // UpperCase first Letter
        var name = contractInput.getName().substring(0, 1).toUpperCase() + contractInput.getName().substring(1);
        container.setDescription(WidgetDescription.ATTRIBUTE_MULTIPLE.displayValue(name, contractInput.getParent().getName()));
        return List.of(createTitle(name), container);
    }

    private Element createTitle(String title) {
        return parametrizedDataManagementWidgetFactory.createTitle(title).toComponent(dimensionFactory);
    }

    private List<Element> createDetailsRow(NodeBusinessObjectInput nodeBusinessObjectInput) {
        return List.of(
                parametrizedDataManagementWidgetFactory.createSpacingContainer(dimensionFactory, nodeBusinessObjectInput),
                parametrizedDataManagementWidgetFactory.createDetailsWidgetContainer(dimensionFactory, nodeBusinessObjectInput)
        );
    }
}
