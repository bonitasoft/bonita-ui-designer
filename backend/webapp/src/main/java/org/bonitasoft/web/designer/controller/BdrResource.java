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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

import static org.bonitasoft.web.designer.SpringWebApplicationInitializer.BONITA_DATA_REPOSITORY_ORIGIN;

@RestController
@RequestMapping("/rest/bdr")
public class BdrResource {

    protected static final Logger logger = LoggerFactory.getLogger(BdrResource.class);

    @RequestMapping(value = "/url", method = RequestMethod.GET)
    public String getBdrUrl() {
        String bdrUrl = System.getProperty(BONITA_DATA_REPOSITORY_ORIGIN);
        try {
            new URL(bdrUrl);
        } catch (MalformedURLException e) {
            logger.warn("System property " + BONITA_DATA_REPOSITORY_ORIGIN + " is not set, or not a valid URL.");
            return "{}";
        }
        return String.format("{\"url\": \"%s\"}", bdrUrl);
    }

}
