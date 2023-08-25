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

import org.bonitasoft.web.designer.common.repository.exception.InUseException;
import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.model.exception.ConstraintValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.bonitasoft.web.designer.utils.UIDesignerMockMvcBuilder.mockServer;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de {@link org.bonitasoft.web.designer.controller.ResourceControllerAdvice}
 */
@ExtendWith(MockitoExtension.class)
public class ResourceControllerAdviceTest {

    private MockMvc mockMvc;

    @Mock
    protected FakeService fakeService;

    @InjectMocks
    private FakeResource fakeResource;

    @BeforeEach
    public void setUp() {
        mockMvc = mockServer(fakeResource).build();
    }

    @Test
    public void should_respond_forbidden_with_json_error() throws Exception {
        doThrow(new NotAllowedException("you are not allowed to do that")).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("{\"type\":\"NotAllowedException\",\"message\":\"you are not allowed to do that\"}"));
    }

    @Test
    public void should_respond_notfound_with_json_error() throws Exception {
        doThrow(new NotFoundException("not found")).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"type\":\"NotFoundException\",\"message\":\"not found\"}"));
    }

    @Test
    public void should_respond_server_error_with_json_error_on_IOException() throws Exception {
        doThrow(new IOException("Can't read file")).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"type\":\"IOException\",\"message\":\"Can't read file\"}"));
    }

    @Test
    public void should_respond_internal_error_with_json_error() throws Exception {
        doThrow(new RepositoryException("something went bad on server side", new Exception())).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"type\":\"RepositoryException\",\"message\":\"something went bad on server side\"}"));
    }

    @Test
    public void should_respond_bad_request_with_json_error() throws Exception {
        doThrow(new IllegalArgumentException("something went bad on server side")).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"type\":\"IllegalArgumentException\",\"message\":\"something went bad on server side\"}"));
    }

    @Test
    public void should_respond_bad_request_when_handling_ConstraintValidationException() throws Exception {
        Set<ConstraintViolation<Object>> set = new HashSet<>();
        set.add(aConstraintViolation("here goes bad"));
        doThrow(new ConstraintValidationException(set)).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"type\":\"ConstraintValidationException\",\"message\":\"here goes bad\"}"));
    }

    private ConstraintViolation<Object> aConstraintViolation(String message) {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    @Test
    public void should_respond_conflict_with_json_error() throws Exception {
        doThrow(new InUseException("conflict")).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isConflict())
                .andExpect(content().string("{\"type\":\"InUseException\",\"message\":\"conflict\"}"));
    }

    @Test
    public void should_respond_accepted_with_json_error_on_ImportException() throws Exception {
        ImportException exception = new ImportException(ImportException.Type.CANNOT_OPEN_ZIP, "an error occurs");
        doThrow(exception).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isAccepted())
                .andExpect(content().json(
                        "{\"type\":\"CANNOT_OPEN_ZIP\",\"message\":\"an error occurs\"}"));
    }

    @Test
    public void should_respond_accepted_with_json_error_containing_additionnal_infos_on_ImportExceptio() throws Exception {
        ImportException exception = new ImportException(ImportException.Type.CANNOT_OPEN_ZIP, "an error occurs");
        exception.addInfo("additionnalInfo", "here is something");
        doThrow(exception).when(fakeService).doSomething();

        mockMvc.perform(get("/fake/resource"))
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(status().isAccepted())
                .andExpect(
                        content().json("{\"type\":\"CANNOT_OPEN_ZIP\",\"message\":\"an error occurs\",\"infos\":{\"additionnalInfo\":\"here is something\"}}"));
    }
}
