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

import static java.lang.String.format;
import static org.bonitasoft.web.designer.generator.mapping.data.StringUtil.indent;

import org.bonitasoft.web.designer.generator.mapping.ContractInputDataHandler;

public class OutputProperty {

    private ContractInputDataHandler dataHandler;

    public OutputProperty(ContractInputDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public String toString() {
        if (dataHandler.hasLazyDataRef()) {
            return format("%s = $data.%s;", dataHandler.getDataPath(), dataHandler.inputValue());
        } else if (dataHandler.hasDataReference() && !dataHandler.isDocumentEdition()) {
            return mapDataToContractExpression(dataHandler);
        } else if (dataHandler.isDocumentEdition()) {
            if (dataHandler.isMultiple()) {
                return mapToFileInputValues(dataHandler);
            } else {
                return mapToFileInputValue(dataHandler);
            }
        } else {
            return format("\t%s: $data.formInput.%s", dataHandler.getInputName(), dataHandler.getInputName());
        }
    }

    public boolean isRootProperty() {
        return !isReference();
    }

    public boolean isReference() {
        return dataHandler.hasLazyDataRef();
    }

    public String getDependency() {
        if (dataHandler.isDocumentEdition()) {
            return "$data.context";
        }
        if (dataHandler.hasLazyDataRef()) {
            return format("$data.%s", dataHandler.inputValue());
        }
        return dataHandler.hasDataReference() ? format("$data.%s", dataHandler.getRefName()) : null;
    }

    private String mapDataToContractExpression(ContractInputDataHandler handler) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\t//map %s variable to expected task contract input\n", handler.getRefName()));
        sb.append(String.format("\t%s: ", handler.getInputName()));
        mapDataToContract(handler, sb, 2);
        return sb.toString();
    }

    private void mapDataToContract(ContractInputDataHandler handler, StringBuffer sb, int indentSize) {
        if (handler.isMultiple()) {
            sb.append(String.format("%s.map( %s => ({\n", handler.getDataPath(),ContractInputDataHandler.ITERATOR_NAME));
            for (int i = 0; i < handler.getNonReadOnlyChildren().size(); i++) {
                sb.append(indent(dataToContractInput(handler.getNonReadOnlyChildren().get(i)), indentSize));
                if (i != handler.getNonReadOnlyChildren().size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append(indent("}))",indentSize - 1));
        } else {
            toSimpleObjectMapping(handler, sb, indentSize);
        }
    }

    private void toSimpleObjectMapping(ContractInputDataHandler handler, StringBuffer sb, int indentSize) {
        sb.append("{\n");
        for (int i = 0; i < handler.getNonReadOnlyChildren().size(); i++) {
            sb.append(indent(dataToContractInput(handler.getNonReadOnlyChildren().get(i)), indentSize));
            if (i != handler.getNonReadOnlyChildren().size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(indent("}", indentSize - 1));
    }

    private String dataToContractInput(ContractInputDataHandler dataHandler) {
        if (!dataHandler.hasDataReference()) {
            return String.format("%s: %s !== undefined ? %s : null", dataHandler.getInputName(), dataHandler.getDataPath(), dataHandler.getDataPath());
        }
        StringBuffer sb = new StringBuffer();
        if (dataHandler.isMultiple()) {
            sb.append(String.format("%s: %s.map( %s => (", dataHandler.getInputName(), dataHandler.getDataPath(), ContractInputDataHandler.ITERATOR_NAME));
            toSimpleObjectMapping(dataHandler, sb, 1);
            sb.append("))");
        } else {
            sb.append(String.format("%s: %s ? ", dataHandler.getInputName(), dataHandler.getDataPath()));
            mapDataToContract(dataHandler, sb, 1);
            sb.append(" : null");
        }
        return sb.toString();
    }

    private String mapToFileInputValues(ContractInputDataHandler handler) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\t%s: $data.context.%s_ref.map( doc => ({\n", handler.getInputName(),
                handler.getRefName()));
        sb.append("\t\tid : doc.id ? doc.id.toString() : null,\n");
        sb.append("\t\tfilename : doc.newValue && doc.newValue.filename ? doc.newValue.filename : null,\n");
        sb.append("\t\ttempPath : doc.newValue && doc.newValue.tempPath ? doc.newValue.tempPath : null,\n");
        sb.append("\t\tcontentType : doc.newValue && doc.newValue.contentType ? doc.newValue.contentType : null\n");
        sb.append("\t}))");
        return sb.toString();
    }

    private String mapToFileInputValue(ContractInputDataHandler handler) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("\t%s: {\n", handler.getInputName()));
        sb.append(
                "\t\\tid: $data.context.%s_ref && $data.context.%s_ref.id ? $data.context.%s_ref.id.toString() : null,\n"
                        .replaceAll("%s", handler.getRefName()));
        sb.append(
                "\t\tfilename: $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.filename ? $data.context.%s_ref.newValue.filename : null,\n"
                        .replaceAll("%s", handler.getRefName()));
        sb.append(
                "\t\ttempPath: $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.tempPath ? $data.context.%s_ref.newValue.tempPath : null,\n"
                        .replaceAll("%s", handler.getRefName()));
        sb.append(
                "\t\tcontentType: $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.contentType ? $data.context.%s_ref.newValue.contentType : null\n"
                        .replaceAll("%s", handler.getRefName()));
        sb.append("\t}");
        return sb.toString();
    }

}
