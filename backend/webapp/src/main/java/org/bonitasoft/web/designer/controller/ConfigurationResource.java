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

import static org.bonitasoft.web.designer.config.UiDesignerProperties.BONITA_BDM_URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/config")
public class ConfigurationResource {

    protected static final Logger logger = LoggerFactory.getLogger(ConfigurationResource.class);

    protected final UiDesignerProperties uiDesignerProperties;

    public ConfigurationResource(UiDesignerProperties uiDesignerProperties) {
        this.uiDesignerProperties = uiDesignerProperties;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ConfigurationReport> getConfig() {

        String bdrUrl = this.uiDesignerProperties.getBonita().getBdm().getUrl();
        try {
            new URL(bdrUrl);
        } catch (MalformedURLException e) {
            logger.warn("System property " + BONITA_BDM_URL + " is not set, or not a valid URL.");
            bdrUrl = "";
        }

        return new ResponseEntity<>(new ConfigurationReport(this.uiDesignerProperties.getVersion(), this.uiDesignerProperties.getModelVersion(), bdrUrl, this.uiDesignerProperties.isExperimental()), HttpStatus.OK);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getAllConfig() {
        return new ResponseEntity(this.uiDesignerProperties, HttpStatus.OK);
    }
}
