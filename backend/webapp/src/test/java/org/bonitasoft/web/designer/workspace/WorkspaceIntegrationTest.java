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
package org.bonitasoft.web.designer.workspace;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.web.designer.ApplicationConfig;
import org.bonitasoft.web.designer.config.ContextConfigTest;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * This test load all the Spring config
 */
@ContextConfiguration(classes = {ContextConfigTest.class, ApplicationConfig.class })
@WebAppConfiguration("file:target/test-classes")
@RunWith(SpringJUnit4ClassRunner.class)
public class WorkspaceIntegrationTest {

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private WidgetRepository widgetRepository;
    @Autowired
    public WorkspaceMigrator workspaceMigrator;

    /**
     * The page repository <code>/test/resources/workspace/pages</code> contains a page named <i>page_1_0_0</i> written in format 1.0.0.
     * In 1.0.0 assets haven't id, and the active property was called inactive. When Spring initializes the workspace, the old files are converted
     */
    @Test
    public void should_migrate_page_json_file_oninit() throws IOException {
        Page page = pageRepository.get("page_1_0_0");
        assertThat(page.getDesignerVersion()).isEqualTo("1.0.2-SNAPSHOT");
        assertThat(page.getAssets().iterator().next().getId()).isNotEmpty();
    }

    /**
     * The widget repository <code>/test/resources/workspace/widgets</code> contains a page named <i>widget_1_0_0</i> written in format 1.0.0.
     * In 1.0.0 assets haven't id, and the active property was called inactive. When Spring initializes the workspace, the old files are converted
     */
    @Test
    public void should_migrate_widget_json_file_oninit() throws IOException {
        Widget widget = widgetRepository.get("widget_1_0_0");
        assertThat(widget.getDesignerVersion()).isEqualTo("1.0.2-SNAPSHOT");
        assertThat(widget.getAssets().iterator().next().getId()).isNotEmpty();
    }
}
