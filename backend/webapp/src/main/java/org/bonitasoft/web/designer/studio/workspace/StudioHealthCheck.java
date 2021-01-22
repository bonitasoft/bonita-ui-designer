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

import java.net.URI;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Profile("studio")
@Component
public class StudioHealthCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudioHealthCheck.class);

    private RestClient restClient;

    @Inject
    public StudioHealthCheck(RestClient restClient) {
        this.restClient = restClient;
    }

    @Scheduled(fixedRate = 20000)
    public void run() {
        if (restClient.isConfigured()) {
            String statusURI = restClient.createURI("status/");
            RestTemplate restTemplate = restClient.getRestTemplate();
            try {
                ResponseEntity<String> result = restTemplate.getForEntity(URI.create(statusURI), String.class);
                if (result.getStatusCode() != HttpStatus.OK) {
                    shutdown();
                }
            } catch (RestClientException e) {
                shutdown();
            }
        }
    }

    private void shutdown() {
        LOGGER.warn(
                "Studio API did not respond properly to healthcheck. The UI designer will terminate itself.");
        exit();
    }

    protected void exit() {
        Runtime.getRuntime().exit(0);
    }

}
