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
package org.bonitasoft.web.designer.rest;

import static org.bonitasoft.web.designer.utils.RestControllerUtil.createContextForTest;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;

import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de {@link ResourceControllerAdvice}
 */
public class ResourceControllerAdviceTest {

    private MockMvc mockMvc;

    @Mock
    protected FakeService fakeService;

    @InjectMocks
    private FakeResource fakeResource;

    @Before
    public void setUp() {

        initMocks(this);
        mockMvc = standaloneSetup(fakeResource)
                .setHandlerExceptionResolvers(createContextForTest().handlerExceptionResolver())
                .build();
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
}
