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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Romain Bioteau
 */
@Named
public class StudioWorkspaceResourceHandler implements WorkspaceResourceHandler {

    protected static final String GET_LOCK_STATUS = "lockStatus";

    private RestClient restClient;

    @Inject
    public StudioWorkspaceResourceHandler(RestClient restClient) {
        this.restClient = restClient;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#preOpen(java.nio.file.Path)
     */
    @Override
    public void preOpen(final Path filePath) throws LockedResourceException, ResourceNotFoundException {
        try {
            doPost(filePath, WorkspaceResourceEvent.PRE_OPEN);
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException) {
                if (HttpStatus.LOCKED.equals(((HttpClientErrorException) e).getStatusCode())) {
                    throw new LockedResourceException(filePath.toString(), e);
                }
            }
            handleResourceException(filePath, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#postClose(java.nio.file.Path)
     */
    @Override
    public void postClose(final Path filePath) throws ResourceNotFoundException {
        try {
            doPost(filePath, WorkspaceResourceEvent.POST_CLOSE);
        } catch (Exception e) {
            handleResourceException(filePath, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#postDelete(java.nio.file.Path)
     */
    @Override
    public void postDelete(final Path filePath) throws ResourceNotFoundException {
        try {
            doPost(filePath, WorkspaceResourceEvent.POST_DELETE);
        } catch (Exception e) {
            handleResourceException(filePath, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#postSave(java.nio.file.Path)
     */
    @Override
    public void postSave(final Path filePath) throws ResourceNotFoundException {
        try {
            doPost(filePath, WorkspaceResourceEvent.POST_SAVE);
        } catch (Exception e) {
            handleResourceException(filePath, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#preImport()
     */
    @Override
    public void preImport() {
        doPost(null, WorkspaceResourceEvent.PRE_IMPORT);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#postImport()
     */
    @Override
    public void postImport() {
        doPost(null, WorkspaceResourceEvent.POST_IMPORT);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.workspace.WorkspaceResourceHandler#getLockStatus(java.nio.file.Path)
     */
    @Override
    public LockStatus getLockStatus(final Path filePath) throws ResourceNotFoundException {
        try {
            ResponseEntity<String> lockStatusResponse = doGet(filePath, GET_LOCK_STATUS);
            if (lockStatusResponse.hasBody()) {
                return LockStatus.valueOf(lockStatusResponse.getBody());
            }
        } catch (Exception e) {
            handleResourceException(filePath, e);
        }

        return LockStatus.UNLOCKED;
    }

    protected ResponseEntity<String> doGet(final Path filePath, String action) {
        if (restClient.isConfigured()) {
            final String url = createGetURL(filePath, action);
            RestTemplate restTemplate = restClient.getRestTemplate();
            return restTemplate.getForEntity(URI.create(url), String.class);
        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    protected ResponseEntity<String> doPost(final Path filePath, WorkspaceResourceEvent actionEvent) {
        if (actionEvent == null) {
            throw new IllegalArgumentException("actionEvent is null");
        }
        if (restClient.isConfigured()) {
            final String url = createPostURL(actionEvent);
            RestTemplate restTemplate = restClient.getRestTemplate();
            return restTemplate.postForEntity(URI.create(url), filePath != null ? filePath.toString() : null, String.class);
        }
        return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String createGetURL(final Path filePath, final String action) {
        String encodedURL;
        try {
            encodedURL = URLEncoder.encode(filePath.toFile().toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Failed to encode: " + filePath + " with charset: " + StandardCharsets.UTF_8.name(), e);
        }
        return restClient.createURI(encodedURL + "/"
                + action);
    }

    private String createPostURL(final WorkspaceResourceEvent action) {
        return restClient.createURI(action.name());
    }

    private void handleResourceException(Path filePath, Exception e) throws ResourceNotFoundException {
        if (e instanceof HttpClientErrorException) {
            if (HttpStatus.NOT_FOUND.equals(((HttpClientErrorException) e).getStatusCode())) {
                throw new ResourceNotFoundException(filePath.toString(), e);
            }
        }
        throw new RuntimeException("Unhandled exception", e);
    }

}
