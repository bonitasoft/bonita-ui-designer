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

import org.bonitasoft.web.designer.generator.mapping.data.BusinessQueryDataFactory;
import org.bonitasoft.web.designer.generator.mapping.strategy.CaseOverviewPageCreationStrategy;
import org.bonitasoft.web.designer.generator.mapping.strategy.ProcessInstantiationFormCreationStrategy;
import org.bonitasoft.web.designer.generator.mapping.strategy.TaskFormCreationStrategy;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContractToPageMapper {

    private final ContractInputToWidgetMapper contractToWidgetMapper;
    private final ContractToContainerMapper contractToContainerMapper;
    private final JsonHandler jsonHandler;
    private final DimensionFactory dimensionFactory;
    private final BusinessQueryDataFactory businessQueryDataFactory;

    @Autowired
    public ContractToPageMapper(ContractInputToWidgetMapper contractToWidgetMapper, ContractToContainerMapper contractToContainerMapper,
                                JsonHandler jsonHandler, DimensionFactory dimensionFactory, BusinessQueryDataFactory businessQueryDataFactory) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.contractToContainerMapper = contractToContainerMapper;
        this.jsonHandler = jsonHandler;
        this.dimensionFactory = dimensionFactory;
        this.businessQueryDataFactory = businessQueryDataFactory;
    }

    public Page createFormPage(String name, Contract contract, FormScope scope) {
        switch (scope) {
            case OVERVIEW:
                return new CaseOverviewPageCreationStrategy(contractToContainerMapper).create(name, contract);

            case PROCESS:
                return new ProcessInstantiationFormCreationStrategy(contractToWidgetMapper, contractToContainerMapper, jsonHandler, businessQueryDataFactory).create(name, contract);

            case TASK:
            default:
                return new TaskFormCreationStrategy(contractToWidgetMapper, contractToContainerMapper, jsonHandler, dimensionFactory, businessQueryDataFactory).create(name, contract);
        }
    }
}
