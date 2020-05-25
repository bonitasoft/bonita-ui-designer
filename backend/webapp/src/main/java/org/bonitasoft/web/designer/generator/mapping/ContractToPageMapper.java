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

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.generator.mapping.data.BusinessQueryDataFactory;
import org.bonitasoft.web.designer.generator.mapping.strategy.CaseOverviewPageCreationStrategy;
import org.bonitasoft.web.designer.generator.mapping.strategy.ProcessInstantiationFormCreationStrategy;
import org.bonitasoft.web.designer.generator.mapping.strategy.TaskFormCreationStrategy;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Page;

@Named
public class ContractToPageMapper {

    private ContractInputToWidgetMapper contractToWidgetMapper;
    private ContractToContainerMapper contractToContainerMapper;
    private JacksonObjectMapper mapper;
    private DimensionFactory dimensionFactory;
    private BusinessQueryDataFactory businessQueryDataFactory;

    @Inject
    public ContractToPageMapper(ContractInputToWidgetMapper contractToWidgetMapper, ContractToContainerMapper contractToContainerMapper,
            JacksonObjectMapper mapper, DimensionFactory dimensionFactory,BusinessQueryDataFactory businessQueryDataFactory) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.contractToContainerMapper = contractToContainerMapper;
        this.mapper = mapper;
        this.dimensionFactory = dimensionFactory;
        this.businessQueryDataFactory = businessQueryDataFactory;
    }

    public Page createFormPage(String name, Contract contract, FormScope scope) {
        switch (scope) {
            case OVERVIEW:
                return new CaseOverviewPageCreationStrategy(contractToContainerMapper).create(name, contract);

            case PROCESS:
                return new ProcessInstantiationFormCreationStrategy(contractToWidgetMapper, contractToContainerMapper, mapper, businessQueryDataFactory).create(name, contract);

            case TASK:
            default:
                return new TaskFormCreationStrategy(contractToWidgetMapper, contractToContainerMapper, mapper, dimensionFactory, businessQueryDataFactory).create(name, contract);
        }
    }
}
