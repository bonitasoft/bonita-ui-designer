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

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.reverse;
import static java.lang.String.format;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class FormOutputVisitor implements ContractInputVisitor {

    private List<String> properties = newArrayList();

    @Override
    public void visit(NodeContractInput contractInput) {
        FormOutputVisitor visitor = new FormOutputVisitor();
        for (ContractInput input : contractInput.getInput()) {
            input.accept(visitor);
        }
        properties.add(format("'%s':{%s}", contractInput.getName(), on(",").join(visitor.properties)));
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        ImmutableList.Builder<String> path = ImmutableList.<String>builder().add(contractInput.getName());
        ContractInput parent = contractInput.getParent();
        while (parent != null) {
            path.add(parent.getName());
            parent = parent.getParent();
        }
        properties.add(format("'%s':$data.formInput.%s", contractInput.getName(), on(".").join(reverse(path.build()))));
    }


    public String toJavascriptExpression() {
        return format("return {%s};", on(",").join(properties));
    }
}
