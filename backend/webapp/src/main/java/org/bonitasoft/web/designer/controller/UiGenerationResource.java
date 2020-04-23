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

import javax.inject.Inject;

import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObjectContainer;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.DataManagementGenerator;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
        return dataManagementGenerator.generate(businessObject);
    }
}
