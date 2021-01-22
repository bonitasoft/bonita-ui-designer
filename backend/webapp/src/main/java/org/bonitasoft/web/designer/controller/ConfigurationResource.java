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

import java.net.MalformedURLException;
import java.net.URL;

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.config.AppProperties.BonitaDataProperties;
import org.bonitasoft.web.designer.config.AppProperties.DesignerProperties;
import org.bonitasoft.web.designer.config.AppProperties.UidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/config")
@RequiredArgsConstructor
public class ConfigurationResource {

    protected static final Logger logger = LoggerFactory.getLogger(ConfigurationResource.class);

    private final DesignerProperties designerProperties;
    private final UidProperties uidProperties;
    private final BonitaDataProperties bonitaDataProperties;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ConfigurationReport> getConfig() {
        String bdrUrl = bonitaDataProperties.getOrigin();
        try {
            new URL(bdrUrl);
        }
        catch (MalformedURLException e) {
            logger.warn("System property bonita data repository url is not set, or not a valid URL.");
            bdrUrl = "";
        }
        boolean isExperimental = uidProperties.isExperimental();
        return new ResponseEntity<>(new ConfigurationReport(this.designerProperties.getVersion(), this.designerProperties.getModelVersion(), bdrUrl, isExperimental), HttpStatus.OK);
    }
}
