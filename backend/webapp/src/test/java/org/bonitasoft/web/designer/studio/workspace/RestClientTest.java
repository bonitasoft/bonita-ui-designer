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

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Romain Bioteau
 */
@ExtendWith(MockitoExtension.class)
public class RestClientTest {

    @Mock
    private RestTemplate template;

    @Mock
    private WorkspaceProperties workspaceProperties;

    private RestClient restClient;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception {
        when(workspaceProperties.getApiUrl()).thenReturn("http://localhost:6666/workspace/");
        restClient = new RestClient(template, workspaceProperties);
    }

    @Test
    public void should_createURI_append_action_to_base_url() throws Exception {
        assertThat(restClient.createURI("someAction")).isEqualTo("http://localhost:6666/workspace/someAction");
    }
}
