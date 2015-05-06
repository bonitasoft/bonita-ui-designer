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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class RestClientPropertiesTest {

    @InjectMocks
    private RestClientProperties restClientProperties;

    @Mock
    private Environment env;

    @Test
    public void should_retrieve_rest_api_url_from_system_property()
            throws Exception {
        when(env.getProperty(RestClientProperties.WORKSPACE_API_REST_URL)).thenReturn("http://localhost:6666/workspace");

        assertThat(restClientProperties.getUrl()).isEqualTo(
                "http://localhost:6666/workspace");
    }

    @Test
    public void should_retrieve_null_url_if_system_property_not_set()
            throws Exception {
        assertThat(restClientProperties.getUrl()).isNull();
    }

    @Test
    public void should_retrieve_false_if_system_property_not_set()
            throws Exception {
        assertThat(restClientProperties.isURLSet()).isFalse();
    }

    @Test
    public void should_retrieve_true_if_system_property_is_set()
            throws Exception {
        when(env.getProperty(RestClientProperties.WORKSPACE_API_REST_URL)).thenReturn("http://localhost:6666/workspace");

        assertThat(restClientProperties.isURLSet()).isTrue();
    }
}
