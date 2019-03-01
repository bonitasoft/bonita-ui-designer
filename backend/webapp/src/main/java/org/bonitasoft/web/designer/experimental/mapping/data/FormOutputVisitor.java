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

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.web.designer.experimental.mapping.ContractInputDataHandler;
import org.bonitasoft.web.designer.model.contract.ContractInputVisitor;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

public class FormOutputVisitor implements ContractInputVisitor {

    private List<OutputProperty> properties = new ArrayList<>();

    @Override
    public void visit(NodeContractInput contractInput) {
        ContractInputDataHandler handler = new ContractInputDataHandler(contractInput);
        properties.add(createProperty(handler));
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
            properties.add(createProperty(handler));
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
        properties.add(createProperty(handler));
    }

    private OutputProperty createProperty(ContractInputDataHandler handler) {
        return handler.hasLazyDataRef()
                ? new OutputProperty(format("output.%s = $data.%s;", handler.getPath(), handler.inputValue()), true)
                : handler.hasDataReference()
                        ? new OutputProperty(format("\t'%s': $data.%s", handler.getInputName(), handler.getRefName()))
                        : new OutputProperty(
                                format("\t'%s': $data.formInput.%s", handler.getInputName(), handler.getInputName()));
    }

    public String toJavascriptExpression() {
        StringBuilder outputExpressionBuffer = new StringBuilder();
        outputExpressionBuffer.append(format("var output = {\n%s\n};\n",
                properties.stream()
                        .filter(OutputProperty::isRootProperty)
                        .map(Object::toString)
                        .collect(Collectors.joining(",\n"))));

        properties.stream()
                .filter(OutputProperty::isReference)
                .map(Object::toString)
                .forEach(referencedProperty -> outputExpressionBuffer.append(referencedProperty + "\n"));

        outputExpressionBuffer.append("return output;");
        return outputExpressionBuffer.toString();
    }

    private class OutputProperty {

        private String property;
        private boolean reference = false;

        OutputProperty(String property, boolean reference) {
            this.property = property;
            this.reference = reference;
        }

        OutputProperty(String property) {
            this(property, false);
        }

        @Override
        public String toString() {
            return property;
        }

        public boolean isRootProperty() {
            return !reference;
        }

        public boolean isReference() {
            return reference;
        }

    }
}
