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

package org.bonitasoft.web.designer.experimental.mapping.data;

import static com.google.common.base.Joiner.on;
import static java.lang.String.format;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.util.List;

import org.bonitasoft.web.designer.model.contract.AbstractContractInput;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import com.google.common.base.Strings;

public class FormOutputVisitor implements ContractInputVisitor {

    private List<String> properties = newArrayList();

    @Override
    public void visit(NodeContractInput contractInput) {
        properties.add(createProperty(contractInput));
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        properties.add(createProperty(contractInput));
    }

    private String createProperty(AbstractContractInput contractInput) {
        String name = contractInput.getName();
        return isANodeContractInputWithDataRef(contractInput) ?
                        format("\t'%s': $data.%s", name, ((NodeContractInput) contractInput).getDataReference()
                                .getName())
                        : format("\t'%s': $data.formInput.%s", name, name);
    }

    private boolean isANodeContractInputWithDataRef(AbstractContractInput contractInput) {
        return contractInput instanceof NodeContractInput
                && ((NodeContractInput) contractInput).getDataReference() != null;
    }

    public String toJavascriptExpression() {
        return format("return {\n%s\n};", on(",\n").join(properties));
    }
}
