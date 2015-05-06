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

import java.io.File;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.fedorahosted.tennera.jgettext.PoParser;

@Named
public class LanguagePackFactory {

    private final PoParser poParser;

    private final JacksonObjectMapper objectMapper;

    @Inject
    public LanguagePackFactory(PoParser poParser, JacksonObjectMapper objectMapper) {
        this.poParser = poParser;
        this.objectMapper = objectMapper;
    }

    public LanguagePack create(File poFile) {
        return new LanguagePack(poParser, objectMapper, poFile);
    }
}
