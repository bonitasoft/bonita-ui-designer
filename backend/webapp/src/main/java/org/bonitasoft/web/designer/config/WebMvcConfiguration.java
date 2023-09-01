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


import static org.bonitasoft.web.designer.controller.preview.PreservingCookiePathProxyServlet.P_PORTAL_PASSWORD;
import static org.bonitasoft.web.designer.controller.preview.PreservingCookiePathProxyServlet.P_PORTAL_USER;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.web.angularjs.GeneratorProperties;
import org.bonitasoft.web.designer.controller.preview.PreservingCookiePathProxyServlet;
import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;
import lombok.SneakyThrows;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    public static final String BACKEND_RESOURCES = "classpath:/META-INF/resources/";

    public static final String FRONTEND_RESOURCES = "classpath:/static/";

    public static final String WIDGETS_RESOURCES = "widgets";

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {BACKEND_RESOURCES, FRONTEND_RESOURCES};

    /**
     * Jackson object mapper used to serialize or deserialize Json objects
     */
    @Autowired
    //Settable for testing purpose
    @Setter
    private ObjectMapper objectMapper;

    @Autowired
    private WorkspaceUidProperties workspaceUidProperties;

    @Autowired
    private UiDesignerProperties uiDesignerProperties;

    public static List<MediaType> supportedMediaTypes() {
        return List.of(MediaType.APPLICATION_JSON_UTF8, new MediaType("text", "plain", StandardCharsets.UTF_8));
    }

    /**
     * To use multipart (based on Servlet 3.0) we need to mark the DispatcherServlet with a {@link javax.servlet.MultipartConfigElement} in programmatic Servlet
     * registration. Configuration settings such as maximum sizes or storage locations need to be applied at that Servlet registration level as Servlet 3.0 does
     * not allow for those settings to be done from the MultipartResolver.
     */
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /**
     * Add resources handler to help Spring to manage our static resources (from frontend and backend)
     */
    @SneakyThrows
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/i18n/*")) {
            registry.addResourceHandler("/i18n/*")
                    .addResourceLocations(new GeneratorProperties(workspaceUidProperties.getPath()).getTmpI18nPath().toUri().toString());
        }

        if (!registry.hasMappingForPattern("/widgets/**")) {
            registry.addResourceHandler("/widgets/**")
                    .addResourceLocations(WIDGETS_RESOURCES);
        }

        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**")
                    .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        }
    }

    /**
     * Spring MVC use a default objectMapper. Objects passed to and returned from the controllers are converted to and from HTTP messages by HttpMessageConverter
     * instances. We must use our {{@link #objectMapper}} because of the subtypes.... So we declare two message converters
     * <ul>
     * <li>StringHttpMessageConverter to format the String sent by HTTP like a JSON object representation</li>
     * <li>MappingJackson2HttpMessageConverter to use our {{@link #objectMapper}}</li>
     * </ul>To declare a JacksonHttpMessageConvet
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        //Add a converter for the String sent via HTTP
        var stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);  // see SPR-7316
        converters.add(stringConverter);

        //Use our custom Jackson serializer
        var mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes());
        converters.add(mappingJackson2HttpMessageConverter);

    }


    /**
     * Useful for REST API calls in the preview using relative URLs ../API/ and absolute /bonita/API
     */
    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaAPIProxy() {
        return newProxyServlet("API");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaPortalProxy() {
        return newProxyServlet("portal");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaPortalJSProxy() {
        return newProxyServlet("portal.js");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaServicesProxy() {
        return newProxyServlet("services");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaThemeProxy() {
        return newProxyServlet("theme");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaServerAPIProxy() {
        return newProxyServlet("serverAPI");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaMobileProxy() {
        return newProxyServlet("mobile");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bonitaAppsProxy() {
        return newProxyServlet("apps");
    }

    @Bean
    public ServletRegistrationBean<PreservingCookiePathProxyServlet> bdrProxyServlet() {

        ServletRegistrationBean<PreservingCookiePathProxyServlet> servletRegistration =
                new ServletRegistrationBean<>(new PreservingCookiePathProxyServlet(), "/bdm/*");
        servletRegistration.setLoadOnStartup(1);
        final String url = Optional.ofNullable(uiDesignerProperties.getBonita().getBdm().getUrl()).orElse("");
        servletRegistration.addInitParameter("targetUri", url + "/bdm");
        servletRegistration.addInitParameter(ProxyServlet.P_LOG, "true");
        servletRegistration.setName("bonitaDataRepository");

        return servletRegistration;
    }

    private String getPortalUrl() {
        return uiDesignerProperties.getBonita().getPortal().getUrl();
    }

    public ServletRegistrationBean<PreservingCookiePathProxyServlet> newProxyServlet(String resourceName) {
        ServletRegistrationBean<PreservingCookiePathProxyServlet> servletRegistration =
                new ServletRegistrationBean<>(new PreservingCookiePathProxyServlet(), "/" + resourceName + "/*");
        servletRegistration.setLoadOnStartup(1);
        servletRegistration.addInitParameter("targetUri", getPortalUrl() + "/bonita/" + resourceName);
        servletRegistration.addInitParameter(ProxyServlet.P_LOG, "true");
        servletRegistration.addInitParameter(ProxyServlet.P_PRESERVECOOKIES, "true");
        servletRegistration.addInitParameter(ProxyServlet.P_PRESERVEHOST, "true");
        servletRegistration.setName("bonita-" + resourceName + "-Proxy");
        addCredentials(servletRegistration);

        return servletRegistration;
    }

    private void addCredentials(ServletRegistrationBean<?> servletRegistration) {
        String portalUser = uiDesignerProperties.getBonita().getPortal().getUser();
        String portalPassword = uiDesignerProperties.getBonita().getPortal().getPassword();
        if (!StringUtils.isBlank(portalUser) && !StringUtils.isBlank(portalPassword)) {
            servletRegistration.addInitParameter(P_PORTAL_USER, portalUser);
            servletRegistration.addInitParameter(P_PORTAL_PASSWORD, portalPassword);
        }
    }

}
