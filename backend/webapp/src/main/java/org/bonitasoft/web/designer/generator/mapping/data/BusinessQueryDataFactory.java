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
package org.bonitasoft.web.designer.generator.mapping.data;

import static org.springframework.util.StringUtils.hasText;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.PageData;
import org.springframework.stereotype.Component;

@Component
public class BusinessQueryDataFactory {

    public Set<PageData> create(Contract contract) {
        Set<PageData> pageData = new HashSet<>();
        searchAggregatedDataReferences(contract.getInput(), pageData);
        return pageData;
    }

    private void searchAggregatedDataReferences(List<ContractInput> input, Set<PageData> pageData) {
        input.stream()
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .filter(child -> !child.isReadOnly())
                .filter(child -> child.getDataReference() != null)
                .filter(child -> hasText(child.getDataReference().getName()))
                .filter(child -> child.getDataReference().getRelationType() == RelationType.AGGREGATION)
                .map(child -> new BusinessQueryData(child.getDataReference()))
                .forEach(pageData::add);

        input.stream()
                .filter(NodeContractInput.class::isInstance)
                .map(NodeContractInput.class::cast)
                .forEach(child -> searchAggregatedDataReferences(child.getInput(), pageData));
    }
}
