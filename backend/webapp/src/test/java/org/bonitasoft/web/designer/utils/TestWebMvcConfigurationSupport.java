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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.config.WebMvcConfiguration;
import org.bonitasoft.web.designer.controller.ResourceControllerAdvice;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.JsonHandlerFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// FIXME: use spring test fwk for this ! (do not hack internal classes yourself ...)
public class TestWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    public TestWebMvcConfigurationSupport() {
        StaticApplicationContext applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("resourceControllerAdvice", ResourceControllerAdvice.class);
        setApplicationContext(applicationContext);
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        Collections.addAll(converters, createMessageConverters());
    }

    public HttpMessageConverter<?>[] createMessageConverters() {
        WebMvcConfiguration webMvcConfiguration = new WebMvcConfiguration();
        ObjectMapper objectMapper = ((JacksonJsonHandler) new JsonHandlerFactory().create()).getObjectMapper();
        webMvcConfiguration.setObjectMapper(objectMapper);

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        webMvcConfiguration.configureMessageConverters(converters);
        return converters.toArray(new HttpMessageConverter<?>[converters.size()]);
    }
}
