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

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Form;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.generator.mapping.ContractInputToWidgetMapper;
import org.bonitasoft.web.designer.generator.mapping.ContractToContainerMapper;
import org.bonitasoft.web.designer.generator.mapping.FormScope;
import org.bonitasoft.web.designer.generator.mapping.data.BusinessQueryDataFactory;
import org.bonitasoft.web.designer.generator.mapping.data.FormInputData;
import org.bonitasoft.web.designer.generator.mapping.data.FormOutputData;
import org.bonitasoft.web.designer.generator.mapping.data.SubmitErrorsListData;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.model.contract.Contract;

public class ProcessInstantiationFormCreationStrategy implements PageCreationStrategy {

    private final ContractInputToWidgetMapper contractToWidgetMapper;
    private final ContractToContainerMapper contractToContainerMapper;
    private final JsonHandler jsonHandler;
    private final BusinessQueryDataFactory businessQueryDataFactory;

    public ProcessInstantiationFormCreationStrategy(ContractInputToWidgetMapper contractToWidgetMapper,
                                                    ContractToContainerMapper contractToContainerMapper, JsonHandler jsonHandler,
                                                    BusinessQueryDataFactory businessQueryDataFactory) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.contractToContainerMapper = contractToContainerMapper;
        this.jsonHandler = jsonHandler;
        this.businessQueryDataFactory = businessQueryDataFactory;
    }

    @Override
    public Page create(String name, Contract contract) {
        var form = new Form(name)
                .addData(new FormInputData(jsonHandler, contract))
                .addData(new FormOutputData(contract))
                .addData(new SubmitErrorsListData())
                .addNewRow(createFormContainer(contract));
        businessQueryDataFactory.create(contract)
                .forEach(form::addData);
        return form;
    }

    private FormContainer createFormContainer(Contract contract) {
        var submitButton = contractToWidgetMapper.createSubmitButton(ButtonAction.fromScope(FormScope.PROCESS));
        var container = contractToContainerMapper.create(contract)
                .addNewRow(submitButton)
                .addNewRow(contractToWidgetMapper.createSubmitErrorAlert());
        return new FormContainer().setContainer(container);
    }

}
