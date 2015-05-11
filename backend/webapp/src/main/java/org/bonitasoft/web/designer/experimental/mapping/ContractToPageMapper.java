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

import java.util.ArrayList;

import org.bonitasoft.web.designer.model.ElementContainer;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractType;
import org.bonitasoft.web.designer.model.data.Data;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.Page;

public class ContractToPageMapper {

    private static final String CONTEXT_API_URL = "/bonita/API/bpm/userTask/{{taskId}}/context";
    private static final String CONTEXT_DATA_NAME = "context";
    private static final String TASK_ID_DATA_NAME = "taskId";
    private ContractInputToWidgetMapper contractToWidgetMapper;

    public ContractToPageMapper(ContractInputToWidgetMapper contractToWidgetMapper) {
        this.contractToWidgetMapper = contractToWidgetMapper;
    }

    public Page createPage(String name, Contract contract) {
        Page page = createEmptyPageWithData(name, contract);
        contract.accept(new ContractInputVisitorImpl(page, contractToWidgetMapper));
        addSubmitButton(page, contract);
        return page;
    }

    private Page createEmptyPageWithData(String name, Contract contract) {
        Page page = new Page();
        page.setName(name);
        page.addData(ContractInputToWidgetMapper.SENT_DATA_NAME, newData(DataType.JSON, "{}"));
        if (contract.getContractType() == ContractType.TASK) {
            page.addData(TASK_ID_DATA_NAME, newData(DataType.URLPARAMETER, "id"));
            page.addData(CONTEXT_DATA_NAME, newData(DataType.URL, CONTEXT_API_URL));
        }
        return page;
    }

    private Data newData(DataType type, Object value) {
        Data data = new Data();
        data.setType(type);
        data.setValue(value);
        return data;
    }

    private void addSubmitButton(ElementContainer page, Contract contract) {
        ArrayList<Element> row = new ArrayList<>();
        row.add(contractToWidgetMapper.createSubmitButton(contract));
        page.getRows().add(row);
    }

}
