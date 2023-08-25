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

package org.bonitasoft.web.designer.controller;

import static org.bonitasoft.web.designer.builder.BusinessObjectBuilder.aBusinessObject;
import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URISyntaxException;

import org.bonitasoft.web.designer.generator.mapping.dataManagement.BusinessObject;
import org.bonitasoft.web.designer.generator.mapping.dataManagement.DataManagementGenerator;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class UiGenerationResourceTest {

    private final JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private MockMvc mockMvc;

    @InjectMocks
    private UiGenerationResource UiGenerationResource;

    @Mock
    private DataManagementGenerator dataManagementGenerator;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        mockMvc = mockServer(UiGenerationResource).build();
    }

    @Test
    void should_generate_business_object_ui() throws Exception {
        BusinessObject bo = aBusinessObject().build();

        mockMvc
                .perform(post("/rest/generation/businessobject")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonHandler.toJson(bo)))
                .andExpect(status().isOk());
        verify(dataManagementGenerator).generate(any(BusinessObject.class));

    }

}
