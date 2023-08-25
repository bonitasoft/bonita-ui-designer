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

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObjectContainer;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.DataManagementGenerator;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/generation")
@RequiredArgsConstructor
public class UiGenerationResource {

    private final DataManagementGenerator dataManagementGenerator;

    @PostMapping(value = "/businessobject")
    public BusinessObjectContainer dataManagementGenerator(@RequestBody BusinessObject businessObject)
            throws RepositoryException {
        return dataManagementGenerator.generate(businessObject);
    }
}
