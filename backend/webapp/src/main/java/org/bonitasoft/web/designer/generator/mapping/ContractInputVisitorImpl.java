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

import java.util.Collections;
import java.util.Objects;

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

    public ContractInputVisitorImpl(ElementContainer container,
            ContractInputToWidgetMapper contractInputToWidgetMapper) {
        this.contractInputToWidgetMapper = contractInputToWidgetMapper;
        this.container = container;
    }

    @Override
    public void visit(NodeContractInput contractInput) {
        if (shouldCreateContainer(contractInput)) {
            Container newContainer = contractInputToWidgetMapper.toContainer(contractInput,
                    container.getRows());
            ContractInputVisitorImpl containerContractInputVisitor = new ContractInputVisitorImpl(newContainer,
                    contractInputToWidgetMapper);
            for (ContractInput childInput : contractInput.getInput()) {
                childInput.accept(containerContractInputVisitor);
            }
            if (contractInput.isMultiple() && !contractInput.isReadOnly()) {
                newContainer.getRows()
                        .add(Collections.<Element> singletonList(contractInputToWidgetMapper.createRemoveButton()));
            }
            container.getRows().add(Collections.<Element> singletonList(newContainer));
            addButtonBar(contractInput, container);
        } else {
            for (ContractInput childInput : contractInput.getInput()) {
                childInput.accept(this);
            }
        }
    }

    private boolean shouldCreateContainer(NodeContractInput contractInput) {
        return !(!contractInput.isMultiple()
                && contractInput.getInput().size() == 1
                && contractInput.getInput().stream()
                        .filter(input -> Objects.equals(ContractInputDataHandler.PERSISTENCEID_INPUT_NAME,
                                input.getName()))
                        .findFirst()
                        .filter(ContractInputDataHandler::hasAggregatedParentRef)
                        .isPresent());
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        if (contractInputToWidgetMapper.canCreateComponent(contractInput)) {
            ContractInputDataHandler dataHandler = new ContractInputDataHandler(contractInput);
            Element element = dataHandler.isDocument()
                    ? contractInputToWidgetMapper.toDocument(contractInput)
                    : contractInputToWidgetMapper.toElement(contractInput, container.getRows());
            container.getRows().add(Collections.singletonList(element));
            addButtonBar(contractInput,
                    element instanceof ElementContainer && dataHandler.isDocument()
                            ? (ElementContainer) element : container);
        }
    }

    private void addButtonBar(ContractInput contractInput, ElementContainer container) {
        if (contractInput.isMultiple() && !contractInput.isReadOnly()) {
            container.getRows()
                    .add(Lists.<Element> newArrayList(contractInputToWidgetMapper.createAddButton(contractInput)));
        }
    }
}
