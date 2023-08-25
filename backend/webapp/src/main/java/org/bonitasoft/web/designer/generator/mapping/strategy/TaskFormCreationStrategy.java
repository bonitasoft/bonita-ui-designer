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
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.generator.mapping.*;
import org.bonitasoft.web.designer.generator.mapping.data.*;
import org.bonitasoft.web.designer.generator.parametrizedWidget.Alignment;
import org.bonitasoft.web.designer.generator.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.generator.parametrizedWidget.TextWidget;
import org.bonitasoft.web.designer.generator.parametrizedWidget.TitleWidget;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.springframework.util.StringUtils.hasText;

public class TaskFormCreationStrategy implements PageCreationStrategy {

    private final ContractInputToWidgetMapper contractToWidgetMapper;
    private final ContractToContainerMapper contractToContainerMapper;
    private final JsonHandler jsonHandler;
    private final DimensionFactory dimensionFactory;
    private final BusinessQueryDataFactory businessQueryDataFactory;

    public TaskFormCreationStrategy(ContractInputToWidgetMapper contractToWidgetMapper,
                                    ContractToContainerMapper contractToContainerMapper,
                                    JsonHandler jsonHandler,
                                    DimensionFactory dimensionFactory,
                                    BusinessQueryDataFactory businessQueryDataFactory) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.contractToContainerMapper = contractToContainerMapper;
        this.jsonHandler = jsonHandler;
        this.dimensionFactory = dimensionFactory;
        this.businessQueryDataFactory = businessQueryDataFactory;
    }

    @Override
    public Page create(String name, Contract contract) {
        var form = new Form(name)
                .addData(new TaskIdData())
                .addData(new TaskData())
                .addData(new ContextData())
                .addData(new FormOutputData(contract))
                .addData(new SubmitErrorsListData())
                .addNewRow(createTaskInformation())
                .addNewRow(createFormContainer(contract));
        if (shouldAddFormInputData(contract)) {
            form.addData(new FormInputData(jsonHandler, contract));
        }

        findBusinessData(contract)
                .map(BusinessData::new)
                .forEach(form::addData);

        List<BusinessDataLazyRef> lazyRefData = new ArrayList<>();
        contract.getInput().stream()
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .filter(input -> input.getMode() == EditMode.EDIT)
                .filter(input -> input.getDataReference() != null)
                .filter(input -> hasText(input.getDataReference().getName()))
                .flatMap(input -> input.getInput().stream())
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .forEach(input -> findBusinessDataLazyReferences(
                        ((NodeContractInput) input.getParent()).getDataReference().getName(), input, lazyRefData));

        lazyRefData.forEach(form::addData);

        businessQueryDataFactory.create(contract)
                .forEach(form::addData);

        return form;
    }

    private boolean shouldAddFormInputData(Contract contract) {
        var visitor = new FormInputVisitor(jsonHandler);
        contract.accept(visitor);
        return !visitor.isEmpty();
    }

    private Stream<String> findBusinessData(Contract contract) {
        return contract.getInput().stream() //Only search for business data at the root level inputs
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .filter(in -> in.getMode() == EditMode.EDIT)
                .map(NodeContractInput::getDataReference)
                .filter(Objects::nonNull)
                .map(BusinessDataReference::getName)
                .filter(data -> hasText(data))
                .distinct();
    }

    private void findBusinessDataLazyReferences(String path, NodeContractInput input,
                                                List<BusinessDataLazyRef> lazyRefData) {
        var handler = new ContractInputDataHandler(input);
        if (handler.hasLazyDataRef()
                && handler.getParent() != null
                && !handler.getParent().isMultiple()) { //Cannot retrieve lazy references if parent is multiple
            lazyRefData.add(new BusinessDataLazyRef(path.replace(".", "_"), path, input.getDataReference().getName()));
        }

        input.getInput().stream()
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .forEach(child -> {
                    String newPath = handler.inputValue();
                    findBusinessDataLazyReferences(newPath, child,
                            lazyRefData);
                });
    }

    private Container createTaskInformation() {
        var container = new Container();

        var title = new TitleWidget();
        title.setLevel("Level 1");
        title.setText(format("{{ %s.displayName }}", TaskData.DATA_NAME));
        title.setAlignment(Alignment.CENTER);
        container.getRows().add(Collections.<Element>singletonList(title.toComponent(dimensionFactory)));

        var description = new TextWidget();
        description.setText(format("{{ %s.displayDescription }}", TaskData.DATA_NAME));
        container.getRows().add(Collections.<Element>singletonList(description.toComponent(dimensionFactory)));

        return container;
    }

    private FormContainer createFormContainer(Contract contract) {
        var submitButton = contractToWidgetMapper.createSubmitButton(ButtonAction.fromScope(FormScope.TASK));
        var container = contractToContainerMapper.create(contract)
                .addNewRow(submitButton)
                .addNewRow(contractToWidgetMapper.createSubmitErrorAlert());
        return new FormContainer().setContainer(container);
    }

}
