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

package org.bonitasoft.web.designer.controller;

import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObject;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObjectAttribute;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObjectAttributeType;
import org.bonitasoft.web.designer.experimental.mapping.data.BusinessObjectRelationAttribute;
import org.bonitasoft.web.designer.experimental.mapping.dataManagement.BusinessObjectContainer;
import org.bonitasoft.web.designer.experimental.mapping.dataManagement.DataManagementGenerator;
import org.bonitasoft.web.designer.experimental.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/rest/generation")
public class UiGenerationResource {

    private DataManagementGenerator dataManagementGenerator;

    @Inject
    public UiGenerationResource(DataManagementGenerator dataManagementGenerator, PageRepository pageRepository) {
        this.dataManagementGenerator = dataManagementGenerator;
    }

    @RequestMapping(value = "/businessobject", method = RequestMethod.POST)
    @ResponseBody
    public BusinessObjectContainer dataManagementGenerator(@RequestBody BusinessObject businessObject)
            throws RepositoryException {
        String businessObjectName = businessObject.getName();
        NodeBusinessObjectInput nbi = new NodeBusinessObjectInput(businessObjectName);
        nbi.setReadonly(true);
        nbi.setDataName(businessObject.getVariableName());

        for (BusinessObjectAttribute att : businessObject.getAttributes()) {
            if (att instanceof BusinessObjectRelationAttribute) {
                BusinessObjectRelationAttribute relAtt = (BusinessObjectRelationAttribute) att;
                NodeBusinessObjectInput nbiChild = new NodeBusinessObjectInput(relAtt.getReference());
                nbiChild.setDataName(nbi.getDataNameSelected());
                nbiChild.setReadonly(true);
                nbi.addInput(nbiChild);
                //TODO: Here call BDR POST to retrieve attribute for child object and add attributes to nbiChild
            } else {
                LeafContractInput lci = new LeafContractInput(att.getName(), getAttributeClass(att.getType()));
                lci.setReadonly(true);
                nbi.addInput(lci);
            }
        }

        return dataManagementGenerator.generate(nbi);
    }

    private Class getAttributeClass(String attType) {
        switch (attType) {
            case "STRING":
                return String.class;
            case "LOCALDATETIME":
            case "OFFSETDATETIME":
                return LocalDateTime.class;
            case "LOCALDATE":
            case "DATE":
                return LocalDate.class;
            case "INTEGER":
                return Integer.class;
            case "LONG":
                return Long.class;
            case "DOUBLE":
                return Double.class;
            case "FLOAT":
                return Float.class;
            case "BOOLEAN":
                return Boolean.class;
            default:
                throw new UnsupportedOperationException("Attribute type isn't supported");
        }
    }
}
