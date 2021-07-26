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
package org.bonitasoft.web.designer.migration;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.UiDesignerPropertiesBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

    @Test
    public void should_compare_versions() throws Exception {

        Version version = new Version("1.2.2");

        assertThat(version.isGreaterThan("1.1.0")).isTrue();
        assertThat(version.isGreaterThan("1.2.2-SNAPSHOT")).isTrue();
        assertThat(version.isGreaterThan("1.2.1")).isTrue();

        assertThat(version.isGreaterThan("1.2.3")).isFalse();
        assertThat(version.isGreaterThan("1.2.3-SNAPSHOT")).isFalse();
        assertThat(version.isGreaterThan("2.0.0")).isFalse();
    }

    @Test
    public void should_return_version_to_string() throws Exception {

        Version version = new Version("1.2.2-SNAPHOT");

        assertThat(version.toString()).isEqualTo("1.2.2-SNAPHOT");
    }

    @Test
    public void should_return_current_model_version() throws Exception {
        UiDesignerProperties uiDesignerProperties = new UiDesignerPropertiesBuilder().build();

        // non-experimental mode by default
        String currentVersion;
        currentVersion = Version.getCurrentModelVersion("2.0", uiDesignerProperties);
        assertThat(currentVersion).isEqualTo(uiDesignerProperties.getModelVersionLegacy());
        currentVersion = Version.getCurrentModelVersion("3.0", uiDesignerProperties);
        assertThat(currentVersion).isEqualTo(uiDesignerProperties.getModelVersionLegacy());

        uiDesignerProperties.setExperimental(true);
        currentVersion = Version.getCurrentModelVersion("2.0", uiDesignerProperties);
        assertThat(currentVersion).isEqualTo(uiDesignerProperties.getModelVersionLegacy());
        currentVersion = Version.getCurrentModelVersion("3.0", uiDesignerProperties);
        assertThat(currentVersion).isEqualTo(uiDesignerProperties.getModelVersion());

    }

}
