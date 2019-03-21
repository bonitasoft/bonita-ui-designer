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

import org.bonitasoft.web.designer.experimental.mapping.ContractInputDataHandler;

public class OutputProperty {

    private ContractInputDataHandler dataHandler;
    
    public OutputProperty(ContractInputDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    @Override
    public String toString() {
        if(dataHandler.hasLazyDataRef()) {
            return format("output.%s = $data.%s;", dataHandler.getPath(), dataHandler.inputValue());
         }else if (dataHandler.hasDataReference() && !dataHandler.isDocumentEdition()){
             return format("\t'%s': $data.%s", dataHandler.getInputName(), dataHandler.getRefName());
         }else if(dataHandler.isDocumentEdition() ) {
             if(dataHandler.isMultiple()) {
                 return mapToFileInputValues(dataHandler);
             }else {
                 return mapToFileInputValue(dataHandler);
             }
         }else {
             return format("\t'%s': $data.formInput.%s", dataHandler.getInputName(), dataHandler.getInputName());
         }
    }

    public boolean isRootProperty() {
        return !isReference();
    }

    public boolean isReference() {
        return dataHandler.hasLazyDataRef() || dataHandler.isDocumentEdition();
    }
    
    public String getDependency() {
        if(dataHandler.isDocumentEdition()) {
            return "$data.context";
        }
        if(dataHandler.hasLazyDataRef()) {
            return format("$data.%s", dataHandler.inputValue());
        }
        return dataHandler.hasDataReference() ? format("$data.%s", dataHandler.getRefName()) : null;
    }

    private String mapToFileInputValues(ContractInputDataHandler handler) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("output.%s = $data.context.%s_ref.map( doc => {\n",handler.getInputName(),handler.getRefName()));
        sb.append("\treturn {\n");
        sb.append("\t\t'id' : doc.id ? doc.id.toString() : null,\n");
        sb.append("\t\t'filename' : doc.newValue && doc.newValue.filename ? doc.newValue.filename : null,\n");
        sb.append("\t\t'tempPath' : doc.newValue && doc.newValue.tempPath ? doc.newValue.tempPath : null,\n");
        sb.append("\t\t'contentType' : doc.newValue && doc.newValue.contentType ? doc.newValue.contentType : null\n");
        sb.append("\t};\n");
        sb.append("});");
        return sb.toString();
    }
    
    private String mapToFileInputValue(ContractInputDataHandler handler) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("output.%s = {\n",handler.getInputName()));
        sb.append("\t'id' : $data.context.%s_ref && $data.context.%s_ref.id ? $data.context.%s_ref.id.toString() : null,\n".replaceAll("%s", handler.getRefName()));
        sb.append("\t'filename' : $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.filename ? $data.context.%s_ref.newValue.filename : null,\n".replaceAll("%s", handler.getRefName()));
        sb.append("\t'tempPath' : $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.tempPath ? $data.context.%s_ref.newValue.tempPath : null,\n".replaceAll("%s", handler.getRefName()));
        sb.append("\t'contentType' : $data.context.%s_ref.newValue && $data.context.%s_ref.newValue.contentType ? $data.context.%s_ref.newValue.contentType : null\n".replaceAll("%s", handler.getRefName()));
        sb.append("};");
        return sb.toString();
    }
    
}
