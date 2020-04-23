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
package org.bonitasoft.web.designer.generator.mapping.dataManagement.databind;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.DataReference;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;

public class BusinessObjectDeserializer extends JsonDeserializer<BusinessObject> {

    private static final String REFERENCE = "reference";
    private static final String VARIABLE_NAME = "variableName";
    private static final String NAME = "name";
    private static final String BUSINESS_OBJECT_NULLABLE = "nullable";
    private static final String BUSINESS_OBJECT_COLLECTION = "collection";

    @Override
    public BusinessObject deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {
        ObjectCodec oc = parser.getCodec();
        ObjectNode treeNode = oc.readTree(parser);

        BusinessObject businessObject = new BusinessObject();
        //Create first node
        NodeBusinessObjectInput node = new NodeBusinessObjectInput(getValueIfExist(treeNode, NAME));
        node.setMultiple(true);
        node.setPageDataName(getValueIfExist(treeNode, VARIABLE_NAME));
        parseNodeContractInput(childInput(treeNode), node);
        businessObject.addInput(node);
        return businessObject;
    }

    private String getValueIfExist(ObjectNode treeNode, String value) {
        if (treeNode.has(value)) {
            JsonNode jsonNode = treeNode.get(value);
            return jsonNode.asText("");
        }
        return "";
    }

    private void parseNodeContractInput(ArrayNode inputArray, NodeBusinessObjectInput rootNodeInput) {
        for (int i = 0; i < inputArray.size(); i++) {
            JsonNode childNode = inputArray.get(i);
            if (childNode.has(REFERENCE)) {
                NodeBusinessObjectInput nodeContractInput = newNodeContractInput(childNode, rootNodeInput);
                rootNodeInput.addInput(nodeContractInput);
                parseNodeContractInput(childInput(childNode), nodeContractInput);
            } else {
                rootNodeInput.addInput(newLeafContractInput(childNode, inputType(childNode)));
            }
        }
    }

    private Class inputType(JsonNode childNode) {
        JsonNode jsonNode = childNode.get("type");
        switch (jsonNode.asText().toLowerCase()) {
            case "string":
            case "text":
                return String.class;
            case "localdatetime":
            case "offsetdatetime":
                return LocalDateTime.class;
            case "date":
            case "localdate":
                return LocalDate.class;
            case "integer":
                return Integer.class;
            case "long":
                return Long.class;
            case "double":
                return Double.class;
            case "float":
                return Float.class;
            case "boolean":
                return Boolean.class;
            default:
                throw new UnsupportedOperationException("Attribute type isn't supported");
        }
    }

    private NodeBusinessObjectInput newNodeContractInput(JsonNode childNode, NodeBusinessObjectInput parentNode) {
        NodeBusinessObjectInput nodeContractInput = new NodeBusinessObjectInput(inputReference(childNode));
        nodeContractInput.setBusinessObjectAttributeName(inputName(childNode));
        nodeContractInput.setMandatory(mandatoryValue(childNode));
        nodeContractInput.setMultiple(multipleValue(childNode));
        nodeContractInput.setDescription(descriptionValue(childNode));
        nodeContractInput.setDataReference(dataReference(childNode));
        nodeContractInput.setPageDataName(pageDataName(nodeContractInput, parentNode));
        nodeContractInput.setParent(parentNode);
        nodeContractInput.setReadonly(readOnlyValue(childNode));
        return nodeContractInput;
    }

    private String pageDataName(NodeBusinessObjectInput nodeContractInput, NodeBusinessObjectInput parentNode) {
        if (parentNode != null) {
            return parentNode.getDataReference() != null && parentNode.getDataReference().getLoadingType().equals(LoadingType.EAGER) ?
                    parentNode.getPageDataName() :
                    parentNode.getPageDataName().concat("_").concat(nodeContractInput.getBusinessObjectAttributeName());
        }
        return nodeContractInput.getDataReference().getName();
    }

    private LeafContractInput newLeafContractInput(JsonNode childNode, Class<?> inputType) {
        LeafContractInput leafContractInput = new LeafContractInput(inputName(childNode), inputType);
        leafContractInput.setMandatory(mandatoryValue(childNode));
        leafContractInput.setMultiple(multipleValue(childNode));
        leafContractInput.setDescription(descriptionValue(childNode));
        leafContractInput.setDataReference(dataReference(childNode));
        leafContractInput.setReadonly(readOnlyValue(childNode));
        return leafContractInput;
    }

    private ArrayNode childInput(JsonNode treeNode) {
        return (ArrayNode) (treeNode.has("attributes") ? treeNode.get("attributes") : new ArrayNode(JsonNodeFactory.instance));
    }

    private String descriptionValue(JsonNode contractInput) {
        JsonNode descriptionNode = contractInput.get("description");
        return descriptionNode != null ? descriptionNode.asText(null) : null;
    }

    private boolean multipleValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get(BUSINESS_OBJECT_COLLECTION);
        return jsonNode != null && jsonNode.asBoolean(false);
    }

    private boolean mandatoryValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get(BUSINESS_OBJECT_NULLABLE);
        return jsonNode != null && !jsonNode.asBoolean(true);
    }

    private String inputName(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get(NAME);
        return jsonNode != null ? jsonNode.asText("") : "";
    }

    private String inputReference(JsonNode jsonInput) {
        JsonNode jsonNode = jsonInput.get(REFERENCE);
        return jsonNode != null ? jsonNode.asText("") : "";
    }

    private DataReference dataReference(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get(REFERENCE);
        if (jsonNode != null && !jsonNode.isNull()) {
            boolean isBusinessDataRef = contractInput.get("type") != null && !contractInput.get("type").isNull();
            if (isBusinessDataRef) {
                return new BusinessDataReference(contractInput.get(NAME).asText(),
                        contractInput.get("type").asText(),
                        RelationType.valueOf(contractInput.get("type").asText()),
                        LoadingType.valueOf(contractInput.get("fetchType").asText()));
            } else {
                return new DataReference(contractInput.get(NAME).asText(),
                        contractInput.get("type").asText());
            }
        }
        return null;
    }

    private boolean readOnlyValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("readOnly");
        return jsonNode == null || jsonNode.asBoolean(true);
    }

    @Override
    public Class<?> handledType() {
        return BusinessObject.class;
    }

}
