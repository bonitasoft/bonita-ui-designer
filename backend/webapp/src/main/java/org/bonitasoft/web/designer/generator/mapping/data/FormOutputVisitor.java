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

import static org.bonitasoft.web.designer.generator.mapping.data.StringUtil.indent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bonitasoft.web.designer.generator.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class FormOutputVisitor implements ContractInputVisitor {

    private List<OutputProperty> properties = new ArrayList<>();

    @Override
    public void visit(NodeContractInput contractInput) {
        ContractInputDataHandler handler = new ContractInputDataHandler(contractInput);
        properties.add(new OutputProperty((handler)));
        if (handler.hasDataReference()) {
            contractInput.getInput().stream()
                    .filter(NodeContractInput.class::isInstance)
                    .map(NodeContractInput.class::cast)
                    .filter(input -> input.getDataReference() != null)
                    .forEach(this::visitLazyRef);
        }
    }

    private void visitLazyRef(NodeContractInput contractInput) {
        ContractInputDataHandler handler = new ContractInputDataHandler(contractInput);
        if (handler.hasLazyDataRef()) {
            properties.add(new OutputProperty(handler));
        }
        if (handler.hasDataReference()) {
            contractInput.getInput().stream()
                    .filter(NodeContractInput.class::isInstance)
                    .map(NodeContractInput.class::cast)
                    .forEach(this::visitLazyRef);
        }
    }

    @Override
    public void visit(LeafContractInput contractInput) {
        ContractInputDataHandler handler = new ContractInputDataHandler(contractInput);
        properties.add(new OutputProperty(handler));
    }

    public String toJavascriptExpression() {
        StringBuilder outputExpressionBuffer = new StringBuilder();

        String expressionDependencies = properties.stream()
                .map(OutputProperty::getDependency)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(" && "));
        boolean addCheckDependencies = !expressionDependencies.isEmpty();
        if (addCheckDependencies) {
            outputExpressionBuffer.append("if( ");
            outputExpressionBuffer.append(expressionDependencies);
            outputExpressionBuffer.append(" ){\n");
        }

        if (properties.stream().anyMatch(OutputProperty::isReference)) {
            outputExpressionBuffer.append(
                    indent("//attach lazy references variables to parent variables\n", addCheckDependencies ? 1 : 0));
        }
        properties.stream()
                .filter(OutputProperty::isReference)
                .map(Object::toString)
                .map(line -> indent(line, addCheckDependencies ? 1 : 0))
                .forEach(referencedProperty -> outputExpressionBuffer.append(referencedProperty + "\n"));

        outputExpressionBuffer.append(indent("return {\n", addCheckDependencies ? 1 : 0));
        outputExpressionBuffer.append(properties.stream()
                .filter(OutputProperty::isRootProperty)
                .map(Object::toString)
                .map(expression -> indent(expression, addCheckDependencies ? 1 : 0))
                .collect(Collectors.joining(",\n")));
        outputExpressionBuffer.append("\n");
        outputExpressionBuffer.append(indent("}", addCheckDependencies ? 1 : 0));

        if (addCheckDependencies) {
            outputExpressionBuffer.append("\n");
            outputExpressionBuffer.append("}");
        }
        return outputExpressionBuffer.toString();
    }

}
