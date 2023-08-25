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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.NodeBusinessObjectInput;
import org.bonitasoft.web.designer.model.contract.BusinessDataReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BusinessObjectDeserializerTest {

    private ObjectMapper mapper;
    private BusinessObjectDeserializer deserializer;

    @BeforeEach
    public void setup() {
        mapper = new ObjectMapper();
        deserializer = new BusinessObjectDeserializer();
    }

    @Test
    public void deserialize_a_json_complex_businessObject_into_a_businessObject_object() throws Exception {
        String jsonValue = "{  \"name\": \"com.company.bpm.model.Order\",  \"attributes\": [    {      \"name\": \"requester\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"creationDate\",      \"type\": \"OFFSETDATETIME\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"modifyBy\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"modificationDate\",      \"type\": \"OFFSETDATETIME\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"category\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"projectName\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"projectDescription\",      \"type\": \"TEXT\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"launchDate\",      \"type\": \"LOCALDATE\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"projectCode\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"process\",      \"type\": \"STRING\",      \"nullable\": \"true\",      \"collection\": \"false\",      \"description\": \"\"    },    {      \"name\": \"comments\",      \"type\": \"AGGREGATION\",      \"nullable\": \"true\",      \"collection\": \"true\",      \"description\": \"\",      \"reference\": \"com.danone.bpm.model.line\",      \"fetchType\": \"LAZY\",      \"attributes\": [        {          \"name\": \"productName\",          \"type\": \"STRING\",          \"nullable\": \"true\",          \"collection\": \"false\",          \"description\": \"\"        },        {          \"name\": \"creationDate\",          \"type\": \"OFFSETDATETIME\",          \"nullable\": \"true\",          \"collection\": \"false\",          \"description\": \"\"        },        {          \"name\": \"comment\",          \"type\": \"TEXT\",          \"nullable\": \"true\",          \"collection\": \"false\",          \"description\": \"\"        },        {          \"name\": \"quantity\",          \"type\": \"INTEGER\",          \"nullable\": \"false\",          \"collection\": \"false\",          \"description\": \"\"        },        {          \"name\": \"price\",          \"type\": \"double\",          \"nullable\": \"false\",          \"collection\": \"false\",          \"description\": \"\"        }      ]    }  ],  \"variableName\": \"order\"}";

        BusinessObject bo = deserializer.deserialize(new JsonFactory(new ObjectMapper()).createParser(jsonValue), null);
        NodeBusinessObjectInput rootNode = (NodeBusinessObjectInput) bo.getInput().get(0);
        assertEquals("order", rootNode.getPageDataName());
        assertEquals(11, rootNode.getInput().size());

        assertTrue(rootNode.getInput().get(10) instanceof NodeBusinessObjectInput);
        NodeBusinessObjectInput nestedBusinessObject = (NodeBusinessObjectInput) rootNode.getInput().get(10);

        assertEquals("order_comments", nestedBusinessObject.getPageDataName());
        assertEquals("comments", nestedBusinessObject.getBusinessObjectAttributeName());
        assertEquals("comments", nestedBusinessObject.getDataReference().getName());
        assertEquals(BusinessDataReference.RelationType.AGGREGATION, nestedBusinessObject.getDataReference().getRelationType());
        assertEquals(BusinessDataReference.LoadingType.LAZY, nestedBusinessObject.getDataReference().getLoadingType());
        assertEquals(rootNode,nestedBusinessObject.getParent());
    }
}
