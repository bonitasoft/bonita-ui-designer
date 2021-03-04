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

import org.bonitasoft.web.designer.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class UiDesignerPropertiesTest {

    @Inject
    UiDesignerProperties uiDesignerProperties;

    @Inject
    WorkspaceProperties workspaceProperties;

    @Inject
    WorkspaceUidProperties workspaceUidProperties;

    @Test
    public void should_uid_properties_correct() throws Exception {
        assertThat(uiDesignerProperties.getEdition()).isNotNull();
        assertThat(uiDesignerProperties.getVersion()).isNotNull();
        assertThat(uiDesignerProperties.getModelVersion()).isNotNull();
        assertThat(uiDesignerProperties.isExperimental()).isNotNull();
    }

    @Test
    public void should_portal_properties_correct() throws Exception {
        assertThat(uiDesignerProperties.getBonita().getPortal().getUrl()).isNotNull();
        assertThat(uiDesignerProperties.getBonita().getPortal().getUser()).isNotNull();
        assertThat(uiDesignerProperties.getBonita().getPortal().getPassword()).isNotNull();
    }

    @Test
    public void should_workspace_properties_correct() throws Exception {
        assertThat(workspaceProperties.getWidgetsWc()).isNotNull();
        assertThat(workspaceProperties.getWidgets()).isNotNull();
        assertThat(workspaceProperties.getFragments()).isNotNull();
        assertThat(workspaceProperties.getPages()).isNotNull();
        assertThat(workspaceProperties.getPath()).isNotNull();
    }

    @Test
    public void should_workspaceUid_properties_correct() throws Exception {
        assertThat(workspaceUidProperties.getPath()).isNotNull();
        assertThat(workspaceUidProperties.getExtractPath()).isNotNull();
    }
}
