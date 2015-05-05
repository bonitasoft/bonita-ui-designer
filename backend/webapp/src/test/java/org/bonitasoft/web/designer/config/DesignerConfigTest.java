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

import static java.nio.file.Files.exists;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test if the Spring Config is OK. To make this test we have to mock the ServletContext. For this we use the
 * annotation {@link org.springframework.test.context.web.WebAppConfiguration}. We can specify to the
 * MockServletContext which path to use to intialize the webapp path
 */
@ContextConfiguration(classes = { DesignerConfig.class })
@WebAppConfiguration("file:target/test-classes")
public class DesignerConfigTest {

    private static Properties prop = new Properties();

    @BeforeClass
    public static void init() throws IOException {
        //We can't use Spring to load the property file because we are before the context initialization
        prop.load(DesignerConfigTest.class.getClassLoader().getResourceAsStream("application.properties"));

        //We want to personnalize the workspace directory
        System.setProperty("repository.pages", prop.getProperty("builddirectory") + "/test-classes/workspace/pages");
        System.setProperty("repository.widgets", prop.getProperty("builddirectory") + "/test-classes/workspace/widgets");
    }

    @Test
    public void loadConfig() throws IOException {
        //When the webapp is loaded we want to verify that  files for i18n are generated
        assertThat(exists(Paths.get(prop.getProperty("builddirectory") + "/test-classes/i18n/simple.json")));

        //The workspace has to be created
        assertThat(exists(Paths.get(prop.getProperty("builddirectory") + "/test-classes/workspace/pages")));
        assertThat(exists(Paths.get(prop.getProperty("builddirectory") + "/test-classes/workspace/widgets")));
    }

}
