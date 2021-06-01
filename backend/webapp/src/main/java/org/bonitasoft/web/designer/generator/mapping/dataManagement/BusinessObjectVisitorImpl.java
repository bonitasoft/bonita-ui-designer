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

import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Container;

import java.util.Collections;

import static org.bonitasoft.web.designer.generator.mapping.dataManagement.WidgetDescription.BUSINESS_OBJECT_CONTAINER;

public class BusinessObjectVisitorImpl implements ContractInputVisitor {

    private final ElementContainer container;

    private final BusinessDataToWidgetMapper businessDataToWidgetMapper;

    public BusinessObjectVisitorImpl(ElementContainer container,
                                     BusinessDataToWidgetMapper businessDataToWidgetMapper) {
        this.businessDataToWidgetMapper = businessDataToWidgetMapper;
        this.container = container;
    }

    @Override
    public void visit(NodeContractInput contractInput) {
        var node = (NodeBusinessObjectInput) contractInput;

        // In some case, node don't have children: for example, if node has exceeded limit relation
        // So we dont want get empty container
        if (!node.hasChildren()) {
            return;
        }

        var c = new Container();
        var description = node.getParent() == null ? node.getPageDataName() : node.getPageDataNameSelected().concat(".").concat(node.getBusinessObjectAttributeName());
        c.setDescription(BUSINESS_OBJECT_CONTAINER.displayValue(node.getName(), description));
        container.getRows().add(Collections.singletonList(c));

        var newContainer = businessDataToWidgetMapper.generateMasterDetailsPattern(node, c.getRows());
        var containerContractInputVisitor = new BusinessObjectVisitorImpl(newContainer, businessDataToWidgetMapper);

        for (var childInput : node.getInput()) {
            childInput.accept(containerContractInputVisitor);
        }
    }

    public void visit(LeafContractInput contractInput) {
        var element = businessDataToWidgetMapper.toElement(contractInput);
        container.getRows().add(element);
    }
}
