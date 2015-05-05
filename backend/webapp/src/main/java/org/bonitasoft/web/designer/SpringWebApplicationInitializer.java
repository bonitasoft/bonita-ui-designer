/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Spring WebApplicationInitializer implementation initializes Spring context by
 * adding a Spring ContextLoaderListener to the ServletContext.
 */
public class SpringWebApplicationInitializer implements WebApplicationInitializer {

    private static final String[] BANNER = { "",
            "d8888b.  .d88b.  d8b   db d888888b d888888b  .d8b.    .d8888.  .d88b.  d88888b d888888b",
            "88  `8D .8P  Y8. 888o  88   `88'   `~~88~~' d8' `8b   88'  YP .8P  Y8. 88'     `~~88~~'",
            "88oooY' 88    88 88V8o 88    88       88    88ooo88   `8bo.   88    88 88ooo      88  ",
            "88~~~b. 88    88 88 V8o88    88       88    88~~~88     `Y8b. 88    88 88~~~      88  ",
            "88   8D `8b  d8' 88  V888   .88.      88    88   88   db   8D `8b  d8' 88         88   ",
            "Y8888P'  `Y88P'  VP   V8P Y888888P    YP    YP   YP   `8888Y'  `Y88P'  YP         YP   " };

    private static final Logger logger = LoggerFactory.getLogger(SpringWebApplicationInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        for (String line : BANNER) {
            logger.info(line);
        }
        logger.info(Strings.repeat("=", 100));

        // Create the root context Spring
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(new Class<?>[] { ApplicationConfig.class });

        // Manage the lifecycle of the root application context
        servletContext.addListener(new ContextLoaderListener(rootContext));

        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", new DispatcherServlet(rootContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir")));
        dispatcher.setAsyncSupported(true);
        //We have to define a global context. If we want to use "/" we have a conflict with the static resources.
        dispatcher.addMapping("/");

        //We need a servlet to resolve the resources generated at runtime. The widgets are not in the webapp and their resources
        // have to be exported in preview or user's uploads. We can't use a resource handler on the webapp load because the resources
        // added can't be added after the init at runtime. We use a servlet and not an api call because we want to distinguish api calls
        // for user's actions and  calls of backend to load a static resource.
        // Register and map the widgetDirectiveLoader servlet
        ServletRegistration.Dynamic redirect = servletContext.addServlet("widgetDirectiveLoaderServlet", new HttpRequestHandlerServlet());
        redirect.setLoadOnStartup(2);
        redirect.addMapping("/generator/widgets/*");
    }
}
