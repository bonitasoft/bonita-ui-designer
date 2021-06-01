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

import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.config.DesignerInitializerException;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.workspace.ResourcesCopier;

import java.io.IOException;

/**
 * Context Listener initializing bonita internationalization
 *
 * @author Vincent Elcrin
 */
@RequiredArgsConstructor
public class I18nInitializer {

    public static final String I18N_RESOURCES = "i18n";

    private final LanguagePackBuilder languagePackBuilder;
    private final ResourcesCopier resourcesCopier;
    private final WorkspaceUidProperties workspaceUidProperties;

    public void initialize() {
        try {
            var extractPath = workspaceUidProperties.getExtractPath();
            resourcesCopier.copy(extractPath, I18N_RESOURCES);
            languagePackBuilder.start(extractPath.resolve(I18N_RESOURCES));
        } catch (IOException e) {
            throw new DesignerInitializerException("Unable to convert po files into json", e);
        }
    }
}
