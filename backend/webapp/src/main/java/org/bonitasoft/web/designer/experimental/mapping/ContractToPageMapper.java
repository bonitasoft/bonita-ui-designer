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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;

@Named
public class ContractToPageMapper {

    private static final String CONTEXT_API_URL = "/bonita/API/bpm/userTask/{{taskId}}/context";
    private static final String CONTEXT_DATA_NAME = "context";
    private static final String TASK_ID_DATA_NAME = "taskId";
    private ContractInputToWidgetMapper contractToWidgetMapper;
    private JacksonObjectMapper objectMapperWrapper;

    @Inject
    public ContractToPageMapper(ContractInputToWidgetMapper contractToWidgetMapper, JacksonObjectMapper objectMapperWrapper) {
        this.contractToWidgetMapper = contractToWidgetMapper;
        this.objectMapperWrapper = objectMapperWrapper;
    }

    public Page createFormPage(String name, Contract contract, FormScope scope) {
        Page page = createEmptyPageWithData(name, contract, scope);
        page.setType("form");
        FormContainer formContainer = new FormContainer();
        page.getRows().add(Collections.<Element>singletonList(formContainer));
        contract.accept(new ContractInputVisitorImpl(formContainer.getContainer(), contractToWidgetMapper));
        if (scope != FormScope.OVERVIEW) {
            addSubmitButton(formContainer.getContainer(), contract, scope);
        }
        return page;
    }

    private Page createEmptyPageWithData(String name, Contract contract, FormScope scope) {
        Page page = new Page();
        page.setName(name);
        if (scope != FormScope.OVERVIEW) {
            addFormInputData(contract, page);
            addFormOutputData(contract, page);
        }
        if (scope == FormScope.TASK) {
            page.addData(TASK_ID_DATA_NAME, newData(DataType.URLPARAMETER, "id"));
            page.addData(CONTEXT_DATA_NAME, newData(DataType.URL, CONTEXT_API_URL));
        }
        return page;
    }

    private void addFormOutputData(Contract contract, Page page) {
        FormOutputVisitor formOutputVisitor = new FormOutputVisitor();
        contract.accept(formOutputVisitor);
        page.addData(ContractInputToWidgetMapper.FORM_OUTPUT_DATA, newData(DataType.EXPRESSION, formOutputVisitor.toJavascriptExpression()));
    }

    private void addFormInputData(Contract contract, Page page) {
        FormInputVisitor formInputVisitor = new FormInputVisitor(objectMapperWrapper);
        contract.accept(formInputVisitor);
        try {
            page.addData(ContractInputToWidgetMapper.FORM_INPUT_DATA, newData(DataType.JSON, formInputVisitor.toJson()));
        } catch (IOException e) {
            page.addData(ContractInputToWidgetMapper.FORM_INPUT_DATA, newData(DataType.JSON, "{}"));
        }
    }

    private Data newData(DataType type, Object value) {
        Data data = new Data();
        data.setType(type);
        data.setValue(value);
        return data;
    }

    private void addSubmitButton(ElementContainer page, Contract contract, FormScope scope) {
        ArrayList<Element> row = new ArrayList<>();
        row.add(contractToWidgetMapper.createSubmitButton(contract, ButtonAction.fromScope(scope)));
        page.getRows().add(row);
    }
}
