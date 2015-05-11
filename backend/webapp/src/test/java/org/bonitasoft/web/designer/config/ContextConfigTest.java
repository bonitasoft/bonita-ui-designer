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
package org.bonitasoft.web.designer.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Configuration;

/**
 * This configuration bean set the env variables to create repos in target
 */
@Configuration
public class ContextConfigTest {
    static Properties prop = new Properties();

    //This bloc static is executed before the Spring configuration loading
    static {
        //We can't use Spring to load the property file because we are before the context initialization
        try {
            prop.load(ContextConfigTest.class.getClassLoader().getResourceAsStream("application.properties"));
            //We want to personnalize the workspace directory
            System.setProperty("repository.pages", prop.getProperty("builddirectory") + "/test-classes/workspace/pages");
            System.setProperty("repository.widgets", prop.getProperty("builddirectory") + "/test-classes/workspace/widgets");
        }
        catch (IOException e) {
            throw new RuntimeException("Error on test environment initialization");
        }
    }


}
