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
package org.bonitasoft.web.designer.experimental.mapping.strategy;

import static java.lang.String.format;

import java.util.Collections;

import org.bonitasoft.web.designer.experimental.mapping.ContractInputToWidgetMapper;
import org.bonitasoft.web.designer.experimental.mapping.ContractToContainerMapper;
import org.bonitasoft.web.designer.experimental.mapping.DimensionFactory;
import org.bonitasoft.web.designer.experimental.mapping.Form;
import org.bonitasoft.web.designer.experimental.mapping.FormScope;
import org.bonitasoft.web.designer.experimental.mapping.data.ContextData;
import org.bonitasoft.web.designer.experimental.mapping.data.FormInputData;
import org.bonitasoft.web.designer.experimental.mapping.data.FormOutputData;
import org.bonitasoft.web.designer.experimental.mapping.data.TaskData;
import org.bonitasoft.web.designer.experimental.mapping.data.TaskIdData;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Alignment;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TextWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.TitleWidget;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;

public class TaskFormCreationStrategy implements PageCreationStrategy {

    private ContractInputToWidgetMapper contractToWidgetMapper;
    private ContractToContainerMapper contractToContainerMapper;
    private JacksonObjectMapper mapper;
    private DimensionFactory dimensionFactory;

    public TaskFormCreationStrategy(ContractInputToWidgetMapper contractToWidgetMapper, ContractToContainerMapper contractToContainerMapper,
            JacksonObjectMapper mapper, DimensionFactory dimensionFactory) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.contractToContainerMapper = contractToContainerMapper;
        this.mapper = mapper;
        this.dimensionFactory = dimensionFactory;
    }

    @Override
    public Page create(String name, Contract contract) {
        return new Form(name)
                .addData(new TaskIdData())
                .addData(new TaskData())
                .addData(new ContextData())
                .addData(new FormInputData(mapper, contract))
                .addData(new FormOutputData(contract))
                .addNewRow(createTaskInformation())
                .addNewRow(createFormContainer(contract));
    }

    private Container createTaskInformation() {
        Container container = new Container();

        TitleWidget title = new TitleWidget();
        title.setLevel("Level 1");
        title.setText(format("{{ %s.displayName }}", TaskData.NAME));
        title.setAlignment(Alignment.CENTER);
        container.getRows().add(Collections.<Element>singletonList(title.toComponent(dimensionFactory)));

        TextWidget description = new TextWidget();
        description.setText(format("{{ %s.displayDescription }}", TaskData.NAME));
        container.getRows().add(Collections.<Element>singletonList(description.toComponent(dimensionFactory)));

        return container;
    }

    private FormContainer createFormContainer(Contract contract) {
        Component submitButton = contractToWidgetMapper.createSubmitButton(contract, ButtonAction.fromScope(FormScope.TASK));
        Container container = contractToContainerMapper.create(contract).addNewRow(submitButton);
        return new FormContainer().setContainer(container);
    }

}
