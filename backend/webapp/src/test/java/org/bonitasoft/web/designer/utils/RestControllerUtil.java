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
package org.bonitasoft.web.designer.utils;

import static org.bonitasoft.web.designer.config.WebMvcConfiguration.supportedMediaTypes;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.IOException;

import org.bonitasoft.web.designer.config.DesignerConfig;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.controller.ResourceControllerAdvice;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


/**
 * Use in RestControllerTest
 */
public class RestControllerUtil {

    /**
     * Convert an object in Json byte
     */
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        JacksonObjectMapper mapper = new DesignerConfig().objectMapperWrapper();
        return mapper.toJson(object);
    }

    /**
     * Convert an object in String
     */
    public static String convertObjectToJsonString(Object object) throws IOException {
        JacksonObjectMapper mapper = new DesignerConfig().objectMapperWrapper();
        return new String(mapper.toJson(object));
    }

    /**
     * Create a context with the controller advice which contains errors resolvers
     */
    public static WebMvcConfigurationSupport createContextForTest() {
        final StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("resourceControllerAdvice", ResourceControllerAdvice.class);

        final WebMvcConfigurationSupport webMvcConfigurationSupport = new TestWebMvcConfigurationSupport();
        webMvcConfigurationSupport.setApplicationContext(applicationContext);

        return webMvcConfigurationSupport;
    }

    public static MappingJackson2HttpMessageConverter createMessageConverter() {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(new DesignerConfig().objectMapper());
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(supportedMediaTypes());
        return mappingJackson2HttpMessageConverter;
    }

    public static MockMvcBuilder uiDesignerStandaloneSetup(Object... controllers) {
        return standaloneSetup(controllers)
                .setMessageConverters(createMessageConverter())
                .setHandlerExceptionResolvers(createContextForTest().handlerExceptionResolver());
    }

    /**
     * Convert Json in object
     */
    public static <T> T convertJsonByteToObject(byte[] json, Class<T> objectClass) throws IOException {
        JacksonObjectMapper mapper = new DesignerConfig().objectMapperWrapper();
        return mapper.fromJson(json, objectClass);
    }

}
