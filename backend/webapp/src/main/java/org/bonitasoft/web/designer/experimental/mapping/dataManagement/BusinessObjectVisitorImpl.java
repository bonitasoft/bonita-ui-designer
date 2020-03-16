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
package org.bonitasoft.web.designer.experimental.mapping.dataManagement;

import java.util.Collections;

import org.bonitasoft.web.designer.experimental.mapping.BusinessDataToWidgetMapper;
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessObjectVisitorImpl implements ContractInputVisitor {

    private static final Logger logger = LoggerFactory.getLogger(BusinessObjectVisitorImpl.class);

    private ElementContainer container;

    private BusinessDataToWidgetMapper businessDataToWidgetMapper;


    public BusinessObjectVisitorImpl(ElementContainer container,
                                     BusinessDataToWidgetMapper businessDataToWidgetMapper) {
        this.businessDataToWidgetMapper = businessDataToWidgetMapper;
        this.container = container;
    }

    @Override
    public void visit(NodeContractInput contractInput) {
        // Create Table Widget and 2 container
        container = businessDataToWidgetMapper.generateMasterDetailsPattern((NodeBusinessObjectInput) contractInput, container.getRows());
        for (ContractInput childInput : contractInput.getInput()) {
            childInput.accept(this);
        }
    }

    public void visit(LeafContractInput contractInput) {
        Element element = businessDataToWidgetMapper.toElement(contractInput);
        container.getRows().add(Collections.singletonList(element));
    }
}
