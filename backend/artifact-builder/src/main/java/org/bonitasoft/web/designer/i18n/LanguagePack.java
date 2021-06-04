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
import org.bonitasoft.web.designer.ArtifactBuilderException;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class LanguagePack {

    private final File poFile;
    private final PoParser poParser;
    private final JsonHandler jsonHandler;

    /**
     * Transform po file into json file.
     * Using hash map ease the transformation into JSON.
     *
     * @return a JSON representation of the catalog conform to angular-gettext expectations.
     * @throws IOException
     */
    public byte[] toJson() throws IOException {
        var language = new HashMap<String, HashMap<String, Object>>();
        var translations = new HashMap<String,Object>();

        var catalog = poParser.parseCatalog(poFile);
        language.put(extractLanguageFrom(catalog.locateHeader()), translations);

        catalog.processMessages(message -> {
            if (isHeader(message)) { // ignore header
                return;
            }
            translations.put(
                    message.getMsgid(),
                    isPlural(message) ? message.getMsgstrPlural() : message.getMsgstr()
            );
        });
        return jsonHandler.toJson(language);
    }

    private String extractLanguageFrom(Message header) {
        var matcher = Pattern.compile("Language:(.*?)$", Pattern.MULTILINE).matcher(header.getMsgstr());
        if (!matcher.find()) {
            throw new ArtifactBuilderException("Couldn't find po file language.");
        }
        return matcher.group(1).trim();
    }

    private boolean isHeader(Message message) {
        return message.getMsgid() == null || message.getMsgid().isEmpty();
    }

    private boolean isPlural(Message message) {
        return message.getMsgstr() == null;
    }
}
