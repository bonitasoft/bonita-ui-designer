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
package org.bonitasoft.web.designer.i18n;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.config.DesignerInitializerException;

/**
 * Context Listener initializing bonita internationalization
 *
 * @author Vincent Elcrin
 */
@Named
public class I18nInitializer {

    @Inject
    private LanguagePackBuilder languagePackBuilder;

    @PostConstruct
    public void contextInitialized() {
        try {
            languagePackBuilder.start(Paths.get(getClass().getResource("/i18n").toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new DesignerInitializerException("Unable to convert po files into json", e);
        }
    }
}
