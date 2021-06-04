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
package org.bonitasoft.web.designer.localization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.rendering.TemplateEngine;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.visitor.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

public class LocalizationFactory implements PageFactory {

    private static final Logger logger = LoggerFactory.getLogger(LocalizationFactory.class);

    private final PageRepository pageRepository;

    public LocalizationFactory(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Override
    public <P extends Previewable & Identifiable> String generate(P previewable) {
        if (previewable instanceof Assetable) {
            return new TemplateEngine("localizationFactory.hbs.js")
                    .with("localization", getLocalization(previewable))
                    .build(new Object());
        }
        return "";
    }

    private <P extends Previewable & Identifiable> Object getLocalization(P previewable) {
        if (previewable instanceof Page) {
            try {
                var localizationPath = pageRepository.resolvePath(previewable.getId()).resolve("assets/json/localization.json");
                return new ObjectMapper().readValue(localizationPath.toFile(), JsonNode.class);
            } catch (FileNotFoundException e) {
                logger.warn("{} <{}> has no localization.json file.", previewable.getType(), previewable.getId());
                return "{}";
            } catch (IOException e) {
                logger.error("An error occurred while loading assets/json/localization.json for page <{}>", previewable.getId(), e);
                return "{}";
            }
        } else {
            return "{}";
        }
    }
}
