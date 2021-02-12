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

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
@Named
public class UiDesignerProperties {

    public UiDesignerProperties(String version, String modelVersion) {
        this.version = version;
        this.modelVersion = modelVersion;
    }

    public UiDesignerProperties() {}

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


    @Value("${designer.edition}")
    private String edition;

    @Value("${designer.version}")
    private String version;

    @Value("${designer.modelVersion}")
    private String modelVersion;

    @Value("${designer.experimental}")
    private boolean experimental;

    @Inject
    private BonitaProperties bonita = new BonitaProperties();
    @Inject
    private WorkspaceProperties workspaceProperties = new WorkspaceProperties();
    @Inject
    private WorkspaceUidProperties workspaceUidProperties = new WorkspaceUidProperties();

    @Data
    @Named
    public static class BonitaProperties {
        @Inject
        private PortalProperties portal = new PortalProperties();
        @Inject
        private BdmProperties bdm = new BdmProperties();

    }

    @Data
    @Named
    public static class PortalProperties {

        @Value("${designer.bonita.portal.url}")
        private String bonitaPortalUrl;

        @Value("${designer.bonita.portal.user}")
        private String bonitaPortalUser;

        @Value("${designer.bonita.portal.password}")
        private String bonitaPortalPassword;

    }

    @Data
    @Named
    public static class BdmProperties {
        @Value("${designer.bonita.bdm.url}")
        private String url;
    }

}
