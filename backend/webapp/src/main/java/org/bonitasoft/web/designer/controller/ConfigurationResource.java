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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/rest/config")
@RequiredArgsConstructor
public class ConfigurationResource {

    private final UiDesignerProperties uiDesignerProperties;

    @GetMapping
    public ResponseEntity<Configuration> getConfig() {
        String bdrUrl = uiDesignerProperties.getBonita().getBdm().getUrl();
        try {
            new URL(bdrUrl);
        } catch (MalformedURLException e) {
            log.warn("Bonita data repository url is not set, or not a valid URL.");
            bdrUrl = "";
        }
        String appServerUrl = uiDesignerProperties.getWorkspaceUid().getAppServerUrl();
        try {
            new URL(appServerUrl);
        } catch (MalformedURLException e) {
            log.warn("App server url is not set, or not a valid URL.");
            appServerUrl = "";
        }
        return new ResponseEntity<>(
                new Configuration(this.uiDesignerProperties.getVersion(),
                        this.uiDesignerProperties.getModelVersion(),
                        this.uiDesignerProperties.getModelVersionLegacy(),
                        bdrUrl,
                        appServerUrl,
                        uiDesignerProperties.isExperimental()),
                HttpStatus.OK);
    }
}
