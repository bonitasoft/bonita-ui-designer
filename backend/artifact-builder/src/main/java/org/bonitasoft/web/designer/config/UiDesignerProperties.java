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

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "designer")
public class UiDesignerProperties {

    /**
     * System property set by the studio to target bonita repository
     */
    public static final String BONITA_BDM_URL = "designer.bonita.bdm.url";
    /**
     * System property set by the studio to target bonita portal
     */
    public static final String BONITA_PORTAL_URL = "designer.bonita.portal.url";
    /**
     * Can be set when developing using a remote bonita platform
     */
    public static final String BONITA_PORTAL_USER = "designer.bonita.portal.user";
    public static final String BONITA_PORTAL_PASSWORD = "designer.bonita.portal.password";

    private String edition;
    private String version;
    private String modelVersion;
    private boolean experimental;
    private BonitaProperties bonita = new BonitaProperties();
    private WorkspaceProperties workspace = new WorkspaceProperties();
    private WorkspaceUidProperties workspaceUid = new WorkspaceUidProperties();

    public UiDesignerProperties(String version, String modelVersion) {
        this.version = version;
        this.modelVersion = modelVersion;
    }

    @Data
    public static class BonitaProperties {
        private PortalProperties portal = new PortalProperties();
        private BdmProperties bdm = new BdmProperties();

    }

    @Data
    public static class PortalProperties {

        private String url;
        private String user;
        private String password;
    }

    @Data
    public static class BdmProperties {
        private String url;
    }


}
