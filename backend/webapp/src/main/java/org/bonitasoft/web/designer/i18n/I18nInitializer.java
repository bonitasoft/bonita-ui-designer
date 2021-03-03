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
package org.bonitasoft.web.designer.i18n;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;

import static org.bonitasoft.web.designer.config.WebMvcConfiguration.I18N_RESOURCES;

/**
 * Context Listener initializing bonita internationalization
 *
 * @author Vincent Elcrin
 */
@Named
public class I18nInitializer {

    @Inject
    private LanguagePackBuilder languagePackBuilder;

    @Inject
    private CopyResources copyResources;

    @Inject
    private WorkspaceUidProperties workspaceUidProperties;

    @PostConstruct
    public void contextInitialized() {
        try {
            Path extractPath = workspaceUidProperties.getExtractPath();
            copyResources.copyResources(extractPath, I18N_RESOURCES);
            languagePackBuilder.start(extractPath.resolve("i18n"));
        } catch (IOException e) {
            throw new DesignerInitializerException("Unable to convert po files into json", e);
        }
    }
}
