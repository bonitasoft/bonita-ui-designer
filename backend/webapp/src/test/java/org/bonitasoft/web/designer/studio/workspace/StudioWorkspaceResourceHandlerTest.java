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

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.Runner.runner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

/**
 * @author Romain Bioteau
 */
@RunWith(MockitoJUnitRunner.class)
public class StudioWorkspaceResourceHandlerTest {

    private StudioWorkspaceResourceHandler studioWorkspaceResourceHandler;

    private RestClient restClient;

    private static Runner runner;

    private HttpServer server;

    private Path filePath;

    @InjectMocks
    private RestClientProperties restClientProperties;

    @Mock
    private Environment env;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        if(server == null){
            server = httpserver();
            runner = runner(server);
            runner.start();
        }

        restClient = new RestClient(new RestTemplate(), restClientProperties);

        filePath = Paths.get("path", "to", "file");

        studioWorkspaceResourceHandler = new StudioWorkspaceResourceHandler(
                restClient);

        when(env.getProperty(RestClientProperties.WORKSPACE_API_REST_URL)).thenReturn("http://localhost:"+server.port()+"/workspace");
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (runner != null) {
            runner.stop();
        }
    }

    @Test
    public void should_doPost_doNothing_if_rest_client_is_not_configured()
            throws Exception {
        when(env.getProperty(RestClientProperties.WORKSPACE_API_REST_URL)).thenReturn(null);

        studioWorkspaceResourceHandler.doPost(filePath, WorkspaceResourceEvent.POST_CLOSE);
    }

    @Test
    public void should_doGet_doNothing_if_rest_client_is_not_configured()
            throws Exception {
        when(env.getProperty(RestClientProperties.WORKSPACE_API_REST_URL)).thenReturn(null);

        studioWorkspaceResourceHandler.doGet(filePath, StudioWorkspaceResourceHandler.GET_LOCK_STATUS);
    }

    @Test
    public void should_preOpen_post_filePath_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/PRE_OPEN"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.preOpen(filePath);
    }

    @Test(expected = LockedResourceException.class)
    public void should_preOpen_throw_a_LockedResourceException_if_status_is_locked()
            throws Exception {
        server.request(by(uri("/workspace/PRE_OPEN"))).response(status(HttpStatus.LOCKED.value()));

        studioWorkspaceResourceHandler.preOpen(filePath);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void should_preOpen_throw_a_ResourceNotFoundException_if_status_is_NOT_FOUND()
            throws Exception {
        server.request(by(uri("/workspace/PRE_OPEN"))).response(status(HttpStatus.NOT_FOUND.value()));

        studioWorkspaceResourceHandler.preOpen(filePath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_doPost_throw_an_IllegalArgumentException_if_actionEvent_is_null()
            throws Exception {
        Path filePath = Paths.get("path", "to", "file");
        studioWorkspaceResourceHandler.doPost(filePath, null);
    }

    @Test
    public void should_postClose_post_filePath_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/POST_CLOSE"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.postClose(filePath);
    }

    @Test
    public void should_postDelete_post_filePath_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/POST_DELETE"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.postDelete(filePath);
    }

    @Test
    public void should_postSave_post_filePath_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/POST_SAVE"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.postSave(filePath);
    }

    @Test
    public void should_preImport_post_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/PRE_IMPORT"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.preImport();
    }

    @Test
    public void should_postImport_post_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/POST_IMPORT"))).response(status(HttpStatus.OK.value()));

        studioWorkspaceResourceHandler.postImport();
    }

    @Test
    public void should_getLockStatus_get_to_workspace_rest_api()
            throws Exception {
        server.request(by(uri("/workspace/" + URLEncoder.encode(filePath.toString(), "UTF-8") + "/lockStatus"))).response(LockStatus.LOCKED_BY_ME.name());

        LockStatus lockStatus = studioWorkspaceResourceHandler.getLockStatus(filePath);

        assertThat(lockStatus).isEqualTo(LockStatus.LOCKED_BY_ME);
    }

    @Test
    public void should_getLockStatus_return_UNLOCKED_as_default_value()
            throws Exception {
        server.request(by(uri("/workspace/" + URLEncoder.encode(filePath.toString(), "UTF-8") + "/lockStatus"))).response(status(HttpStatus.OK.value()));

        LockStatus lockStatus = studioWorkspaceResourceHandler.getLockStatus(filePath);

        assertThat(lockStatus).isEqualTo(LockStatus.UNLOCKED);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void should_getLockStatus_throw_a_ResourceNotFoundException_if_status_NOT_FOUND()
            throws Exception {
        server.request(by(uri("/workspace/" + URLEncoder.encode(filePath.toString(), "UTF-8") + "/lockStatus"))).response(status(HttpStatus.NOT_FOUND.value()));

        studioWorkspaceResourceHandler.getLockStatus(filePath);
    }

}
