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


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;

@Configuration
@EnableWebMvc
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfiguration.class);
    public final static String BACKEND_RESOURCES = "classpath:/META-INF/resources/";
    public final static String FRONTEND_RESOURCES = "classpath:/static/";
    public final static String WIDGETS_RESOURCES = "classpath:/widgets/";

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {BACKEND_RESOURCES, FRONTEND_RESOURCES};
    
    @Autowired
    private ResourceLoader resourceLoader;

    /**
     * Jackson object mapper used to serialize or deserialize Json objects
     *
     * @see DesignerConfig#objectMapper()
     */
    @Inject
    public ObjectMapper objectMapper;

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
     * This allows for mapping the DispatcherServlet to "/" (thus overriding the mapping of the container’s default Servlet), while still allowing static resource
     * requests to be handled by the container’s default Servlet. It configures a DefaultServletHttpRequestHandler with a URL mapping of "/**" and the lowest priority
     * relative to other URL mappings.
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * In Internet Explorer http requests are cached by default. It's a problem when we want to provide a REST API. This interceptor
     * adds headers in the responses to desactivate the cache. NB :  static resources are cached but managed by the resource handlers
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor interceptor = new WebContentInterceptor();
        interceptor.setCacheSeconds(0);
        interceptor.setUseExpiresHeader(true);
        interceptor.setUseCacheControlHeader(true);
        interceptor.setUseCacheControlNoStore(true);

        registry.addInterceptor(interceptor);
    }

    /**
     * Add resources handler to help Spring to manage our static resources (from frontend and backend)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        if (!registry.hasMappingForPattern("/i18n/*")) {
            registry.addResourceHandler("/i18n/*")
                    .addResourceLocations("classpath:/i18n/");
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

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (this.resourceLoader.getResource("classpath:/static/index.html").exists()) {
            // Use forward: prefix so that no view resolution is done
            try {
                logger.info("Adding welcome page: " + this.resourceLoader.getResource(FRONTEND_RESOURCES + "index.html").getURL());
                registry.addViewController("/").setViewName("forward:/index.html");
            } catch (IOException e) {
                logger.error("The home page index.html was not found", e);
            }
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
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        stringConverter.setWriteAcceptCharset(false);  // see SPR-7316
        converters.add(stringConverter);

        //Use our custom Jackson serializer
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes());
        converters.add(mappingJackson2HttpMessageConverter);

    }

    public static List<MediaType> supportedMediaTypes(){
        return Arrays.asList(new MediaType("application", "json", Charset.forName("UTF-8")), new MediaType("text", "plain", Charset.forName("UTF-8")));
    }

}
