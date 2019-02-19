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

import java.util.Collections;

import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;

import com.google.common.collect.Lists;

public class ContractInputVisitorImpl implements ContractInputVisitor {

    private ElementContainer container;

    private ContractInputToWidgetMapper contractInputToWidgetMapper;

    public ContractInputVisitorImpl(ElementContainer container, ContractInputToWidgetMapper contractInputToWidgetMapper) {
        this.contractInputToWidgetMapper = contractInputToWidgetMapper;
        this.container = container;
    }

    @Override
    public void visit(NodeContractInput contractInput) {
        Container newContainer = contractInputToWidgetMapper.toContainer(contractInput,
                container.getRows());
        ContractInputVisitorImpl containerContractInputVisitor = new ContractInputVisitorImpl(newContainer,
                contractInputToWidgetMapper);
        for (ContractInput childInput : contractInput.getInput()) {
            childInput.accept(containerContractInputVisitor);
        }
        if (contractInput.isMultiple()) {
            newContainer.getRows()
                    .add(Collections.<Element> singletonList(contractInputToWidgetMapper.createRemoveButton()));
        }
        container.getRows().add(Collections.<Element> singletonList(newContainer));
        addButtonBar(contractInput);
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        if (contractInputToWidgetMapper.canCreateComponent(contractInput)) {
            Element element = contractInputToWidgetMapper.isDocumentToEdit(contractInput)
                    ? contractInputToWidgetMapper.toEditableDocument(contractInput)
                    : contractInputToWidgetMapper.toElement(contractInput, container.getRows());
            container.getRows().add(Collections.singletonList(element));
            addButtonBar(contractInput);
        }
    }

    private void addButtonBar(ContractInput contractInput) {
        if (contractInput.isMultiple()) {
            container.getRows()
                    .add(Lists.<Element> newArrayList(contractInputToWidgetMapper.createAddButton(contractInput)));
        }
    }
}
