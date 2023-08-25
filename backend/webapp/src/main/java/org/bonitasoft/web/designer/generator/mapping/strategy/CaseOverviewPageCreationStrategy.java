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
package org.bonitasoft.web.designer.generator.mapping.strategy;

import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Form;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.generator.mapping.ContractToContainerMapper;
import org.bonitasoft.web.designer.model.contract.Contract;

import java.util.ArrayList;

public class CaseOverviewPageCreationStrategy implements PageCreationStrategy {

    private final ContractToContainerMapper contractToContainerMapper;

    public CaseOverviewPageCreationStrategy(ContractToContainerMapper contractToContainerMapper) {
        this.contractToContainerMapper = contractToContainerMapper;
    }

    @Override
    public Page create(String name, Contract contract) {
        return new Form(name)
                .addNewRow(createContainer(contract));
    }

    private Container createContainer(Contract contract) {
        var container = contractToContainerMapper.create(contract);
        if (container.getRows().isEmpty()) {
            container.getRows().add(new ArrayList<>());
        }
        return container;
    }

}
