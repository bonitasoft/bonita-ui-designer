package org.bonitasoft.web.designer.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class AppProperties {

//  workspace.path = "${user.home}/.bonita"
// "workspace.api.url";

    @Data
    @ConfigurationProperties(prefix = "repository")
    public static class RepositoryProperties {

        private String pages = "pages";

        private String widgets = "widgets";

        private String fragments = "fragments";
    }

    @Data
    @ConfigurationProperties(prefix = "bonita.data.repository")
    public static class BonitaDataProperties {
        /**
         * System property set by the studio to target bonita repository : bonita.data.repository.origin
         */
        private String origin;
    }

    @Data
    @ConfigurationProperties(prefix = "bonita.portal")
    public static class BonitaPortalProperties {

        /**
         * System property set by the studio to target bonita portal : bonita.portal.origin
         */
        private String origin;

        /**
         * Can be set when developing using a remote bonita platform : bonita.portal.origin.user
         */
        private String user;

        /**
         * Can be set when developing using a remote bonita platform : bonita.portal.origin.password
         */
        private String password;

    }

    @Data
    @ConfigurationProperties(prefix = "designer")
    public static class DesignerProperties {

        private String edition;

        private String modelVersion;

        private String version;

    }

    @Data
    @ConfigurationProperties(prefix = "uid")
    public static class UidProperties {

        private boolean experimental;

    }
}
