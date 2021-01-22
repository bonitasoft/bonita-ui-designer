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
package org.bonitasoft.web.designer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.designer.config.AppProperties.BonitaDataProperties;
import org.bonitasoft.web.designer.config.AppProperties.BonitaPortalProperties;
import org.bonitasoft.web.designer.config.AppProperties.DesignerProperties;
import org.bonitasoft.web.designer.config.AppProperties.UidProperties;
import org.mitre.dsmiley.httpproxy.ProxyServlet;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import static org.bonitasoft.web.designer.PreservingCookiePathProxyServlet.P_PORTAL_PASSWORD;
import static org.bonitasoft.web.designer.PreservingCookiePathProxyServlet.P_PORTAL_USER;

/**
 * Spring WebApplicationInitializer implementation initializes Spring context by
 * adding a Spring ContextLoaderListener to the ServletContext.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SpringWebApplicationInitializer implements ServletContextInitializer {

    private final DesignerProperties designerProperties;
    private final UidProperties uidProperties;

    private final BonitaPortalProperties bonitaPortalProperties;

    private final BonitaDataProperties bonitaDataProperties;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        log.info(Strings.repeat("=", 100));
        log.info(String.format("UI-DESIGNER : %s edition v.%s", designerProperties.getEdition(), designerProperties.getVersion()));
        if (uidProperties.isExperimental()) {
            log.info("UI-DESIGNER : Running in experimental mode");
        }
        log.info(Strings.repeat("=", 100));

        // Register and map the servlets
        final String portalOrigin = getPortalOrigin();

        // Useful for REST API calls in the preview using relative URLs ../API/ and absolute /bonita/API
        registerProxy(servletContext, "bonitaAPIProxy", "/API/*", "/bonita/API", portalOrigin);
        // Useful for Resources calls in the preview using absolute URLs /bonita
        registerProxy(servletContext, "bonitaPortalProxy", "/portal/*", "/bonita/portal", portalOrigin);
        registerProxy(servletContext, "bonitaPortalJSProxy", "/portal.js/*", "/bonita/portal.js", portalOrigin);
        registerProxy(servletContext, "bonitaServicesProxy", "/services/*", "/bonita/services", portalOrigin);
        registerProxy(servletContext, "bonitaThemeProxy", "/theme/*", "/bonita/theme", portalOrigin);
        registerProxy(servletContext, "bonitaServerAPIProxy", "/serverAPI/*", "/bonita/serverAPI", portalOrigin);
        registerProxy(servletContext, "bonitaMobileProxy", "/mobile/*", "/bonita/mobile", portalOrigin);
        registerProxy(servletContext, "bonitaAppsProxy", "/apps/*", "/bonita/apps", portalOrigin);

        // Useful for repository calls editor to get dataManagement infos
        registerBdrProxy(servletContext, "bonitaDataRepository", "/bdm/*", "/bdm", getDataRepositoryOrigin());
    }

    private void registerProxy(ServletContext servletContext, String servletName, String servletMapping, String targetUri, String targetOrigin) {
        ServletRegistration.Dynamic apiProxyRegistration = servletContext.addServlet(servletName, PreservingCookiePathProxyServlet.class);
        apiProxyRegistration.setLoadOnStartup(1);
        apiProxyRegistration.setInitParameter("targetUri", targetOrigin + targetUri);
        apiProxyRegistration.setInitParameter(ProxyServlet.P_LOG, "true");
        apiProxyRegistration.setInitParameter(ProxyServlet.P_PRESERVECOOKIES, "true");
        apiProxyRegistration.setInitParameter(ProxyServlet.P_PRESERVEHOST, "true");
        apiProxyRegistration.addMapping(servletMapping);
        addCredentials(apiProxyRegistration);
    }

    private void registerBdrProxy(ServletContext servletContext, String servletName, String servletMapping, String targetUri, String targetOrigin) {
        ServletRegistration.Dynamic apiProxyRegistration = servletContext.addServlet(servletName, ProxyServlet.class);
        apiProxyRegistration.setLoadOnStartup(1);
        apiProxyRegistration.setInitParameter("targetUri", targetOrigin + targetUri);
        apiProxyRegistration.setInitParameter(ProxyServlet.P_LOG, "true");
        apiProxyRegistration.addMapping(servletMapping);
    }

    private void addCredentials(ServletRegistration.Dynamic apiProxyRegistration) {
        String portalUser = bonitaPortalProperties.getUser();
        String portalPassword = bonitaPortalProperties.getPassword();
        if (!StringUtils.isBlank(portalUser) && !StringUtils.isBlank(portalPassword)) {
            apiProxyRegistration.setInitParameter(P_PORTAL_USER, portalUser);
            apiProxyRegistration.setInitParameter(P_PORTAL_PASSWORD, portalPassword);
        }
    }

    private String getPortalOrigin() {
        String portalOrigin = bonitaPortalProperties.getOrigin();
        if (StringUtils.isNotBlank(portalOrigin)) {
            return portalOrigin;
        }
        log.warn("UI Designer property 'bonita.portal.origin' is not set. Same origin as UI Designer will be used for portal calls.");
        return "";
    }

    private String getDataRepositoryOrigin() {
        String repositoryOrigin = bonitaDataProperties.getOrigin();
        if (StringUtils.isNotBlank(repositoryOrigin)) {
            return repositoryOrigin;
        }
        log.warn("UI Designer property 'bonita.data.repository.origin' is not set. Same origin as UI Designer will be used for repository calls.");
        return "";
    }
}
