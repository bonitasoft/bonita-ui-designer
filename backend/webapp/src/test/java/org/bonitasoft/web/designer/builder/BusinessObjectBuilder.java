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
package org.bonitasoft.web.designer.builder;

import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aBooleanContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLocalDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aLongContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aOffsetDateTimeContractInput;
import static org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder.aStringContractInput;

import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.builders.ContractInputBuilder;

public class BusinessObjectBuilder {

    private final BusinessObject businessObject;

    private BusinessObjectBuilder(BusinessObject businessObject) {
        this.businessObject = businessObject;
    }

    public static BusinessObjectBuilder aBusinessObject() {
        return new BusinessObjectBuilder(new BusinessObject());
    }

    public BusinessObjectBuilder withInput(ContractInput... contractInput) {
        for (ContractInput input : contractInput) {
            businessObject.addInput(input);
        }
        return this;
    }

    public static ContractInputBuilder aBusinessObjectNodeInput(String name, String pageDataName) {
        return new ContractInputBuilder(new NodeBusinessObjectInput(name, pageDataName));
    }


    @SuppressWarnings("deprecation")
    public static BusinessObject aSimpleBusinessObject() {
        ContractInput a = aBusinessObjectNodeInput("com.company.model.ticket", "ticket").withInput(
                aStringContractInput("title"),
                aDateContractInput("creationDate"),
                aLocalDateContractInput("creationLocalDate"),
                aLocalDateTimeContractInput("creationLocalDateTime"),
                aOffsetDateTimeContractInput("creationOffsetDateTime"),
                aLongContractInput("updateTime")).build();

        ContractInput b = aBusinessObjectNodeInput("com.company.model.person", "person").withInput(aContractInput("name").withDescription("employee name").build(), aBooleanContractInput("isValid"),a).build();

        return aBusinessObject().withInput(b).build();
    }

    public BusinessObject build() {
        return businessObject;
    }
}

