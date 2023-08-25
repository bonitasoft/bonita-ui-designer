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

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.designer.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class)
@WebAppConfiguration("classpath:/")
class UiDesignerPropertiesTest {

    @Autowired
    UiDesignerProperties uiDesignerProperties;

    @Autowired
    WorkspaceProperties workspaceProperties;

    @Autowired
    WorkspaceUidProperties workspaceUidProperties;

    @Test
    void should_uid_properties_correct() throws Exception {
        assertThat(uiDesignerProperties.getEdition()).isNotNull();
        assertThat(uiDesignerProperties.getVersion()).isNotNull();
        assertThat(uiDesignerProperties.getModelVersion()).isNotNull();
        assertThat(uiDesignerProperties.isExperimental()).isNotNull();
    }

    @Test
    void should_portal_properties_correct() throws Exception {
        assertThat(uiDesignerProperties.getBonita().getPortal().getUrl()).isNotNull();
        assertThat(uiDesignerProperties.getBonita().getPortal().getUser()).isNotNull();
        assertThat(uiDesignerProperties.getBonita().getPortal().getPassword()).isNotNull();
    }

    @Test
    void should_workspace_properties_correct() throws Exception {
        assertThat(workspaceProperties.getWidgets()).isNotNull();
        assertThat(workspaceProperties.getFragments()).isNotNull();
        assertThat(workspaceProperties.getPages()).isNotNull();
        assertThat(workspaceProperties.getPath()).isNotNull();
    }

    @Test
    void should_workspaceUid_properties_correct() throws Exception {
        assertThat(workspaceUidProperties.getPath()).isNotNull();
        assertThat(workspaceUidProperties.getExtractPath()).isNotNull();
    }
}


