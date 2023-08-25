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
package org.bonitasoft.web.designer.studio.workspace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudioHealthCheckTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ResponseEntity<String> successResponse;
    @Mock
    private ResponseEntity<String> errorResponse;

    private String statusURI = "http://localhost/api/workspace/status/";
    private StudioHealthCheck healthCheck;

    @BeforeEach
    public void prepareMocks() throws Exception {
        lenient().when(restClient.createURI("status/")).thenReturn(statusURI);
        lenient().when(restClient.getRestTemplate()).thenReturn(restTemplate);
        lenient().when(successResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        lenient().when(errorResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        healthCheck = spy(new StudioHealthCheck(restClient));
        lenient().doNothing().when(healthCheck).exit();
    }

    @Test
    public void should_not_exit_when_studio_status_is_ok() throws Exception {
        when(restTemplate.getForEntity(URI.create(statusURI), String.class)).thenReturn(successResponse);

        healthCheck.run();

        verify(healthCheck, never()).exit();
    }

    @Test
    public void should_exit_when_studio_status_is_ok() throws Exception {
        when(restTemplate.getForEntity(URI.create(statusURI), String.class)).thenReturn(errorResponse);

        healthCheck.run();

        verify(healthCheck).exit();
    }

}
