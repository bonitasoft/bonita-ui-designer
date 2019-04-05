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
package org.bonitasoft.web.designer.model.contract.databind;

import java.io.IOException;

import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.LoadingType;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference.RelationType;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInputContainer;
import org.bonitasoft.web.designer.model.contract.DataReference;
import org.bonitasoft.web.designer.model.contract.EditMode;
import org.bonitasoft.web.designer.model.contract.LeafContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ContractDeserializer extends JsonDeserializer<Contract> {

    @Override
    public Contract deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectCodec oc = parser.getCodec();
        ObjectNode treeNode = oc.readTree(parser);
        Contract contract = new Contract();
        parseNodeContractInput(childInput(treeNode), contract);
        return contract;
    }

    private void parseNodeContractInput(ArrayNode inputArray, ContractInputContainer rootNodeInput) throws IOException {
        for (int i = 0; i < inputArray.size(); i++) {
            JsonNode childNode = inputArray.get(i);
            Class<?> inputType = inputType(childNode);
            if (inputType.equals(NodeContractInput.class)) {
                NodeContractInput nodeContractInput = newNodeContractInput(childNode);
                rootNodeInput.addInput(nodeContractInput);
                parseNodeContractInput(childInput(childNode), nodeContractInput);
            } else {
                rootNodeInput.addInput(newLeafContractInput(childNode, inputType));
            }
        }
    }

    private NodeContractInput newNodeContractInput(JsonNode childNode) {
        NodeContractInput nodeContractInput = new NodeContractInput(inputName(childNode));
        nodeContractInput.setMandatory(mandatoryValue(childNode));
        nodeContractInput.setMultiple(multipleValue(childNode));
        nodeContractInput.setDescription(descriptionValue(childNode));
        nodeContractInput.setDataReference(dataReference(childNode));
        nodeContractInput.setMode(inputMode(childNode));
        nodeContractInput.setReadonly(readOnlyValue(childNode));
        return nodeContractInput;
    }

    private LeafContractInput newLeafContractInput(JsonNode childNode, Class<?> inputType) {
        LeafContractInput leafContractInput = new LeafContractInput(inputName(childNode), inputType);
        leafContractInput.setMandatory(mandatoryValue(childNode));
        leafContractInput.setMultiple(multipleValue(childNode));
        leafContractInput.setDescription(descriptionValue(childNode));
        leafContractInput.setDataReference(dataReference(childNode));
        leafContractInput.setMode(inputMode(childNode));
        leafContractInput.setReadonly(readOnlyValue(childNode));
        return leafContractInput;
    }

    private Class<?> inputType(JsonNode childNode) throws IOException {
        Class<?> inputType;
        try {
            inputType = Class.forName(classNameValue(childNode));
        } catch (ClassNotFoundException e) {
            throw new IOException(
                    String.format("Failed to create LeafContractInput with type %s", classNameValue(childNode)), e);
        }
        return inputType;
    }

    private ArrayNode childInput(JsonNode treeNode) {
        return (ArrayNode) (treeNode.has("input") ? treeNode.get("input") : new ArrayNode(JsonNodeFactory.instance));
    }

    private String classNameValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("type");
        return jsonNode != null ? jsonNode.asText(String.class.getName()) : String.class.getName();
    }

    private String descriptionValue(JsonNode contractInput) {
        JsonNode descriptionNode = contractInput.get("description");
        return descriptionNode != null ? descriptionNode.asText(null) : null;
    }

    private boolean multipleValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("multiple");
        return jsonNode != null ? jsonNode.asBoolean(false) : false;
    }

    private boolean mandatoryValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("mandatory");
        return jsonNode != null ? jsonNode.asBoolean(false) : false;
    }

    private String inputName(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("name");
        return jsonNode != null ? jsonNode.asText("") : "";
    }

    private DataReference dataReference(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("dataReference");
        if (jsonNode != null && !jsonNode.isNull()) {
            boolean isBusinessDataRef = jsonNode.get("relationType") != null && !jsonNode.get("relationType").isNull();
            return isBusinessDataRef
                    ? new BusinessDataReference(jsonNode.get("name").asText(),
                            jsonNode.get("type").asText(),
                            RelationType.valueOf(jsonNode.get("relationType").asText()),
                            LoadingType.valueOf(jsonNode.get("loadingType").asText()))
                    : new DataReference(jsonNode.get("name").asText(),
                            jsonNode.get("type").asText());
        }
        return null;
    }

    private EditMode inputMode(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("mode");
        return jsonNode != null ? EditMode.valueOf(jsonNode.asText("")) : EditMode.CREATE;
    }

    private boolean readOnlyValue(JsonNode contractInput) {
        JsonNode jsonNode = contractInput.get("readOnly");
        return jsonNode != null ? jsonNode.asBoolean(false) : false;
    }

    @Override
    public Class<?> handledType() {
        return Contract.class;
    }

}
