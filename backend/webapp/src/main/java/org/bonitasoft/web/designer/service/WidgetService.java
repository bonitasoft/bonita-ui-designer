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
package org.bonitasoft.web.designer.service;

import static java.util.Collections.singletonList;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.joda.time.Instant;

@Named
public class WidgetService {

    private WidgetRepository widgetRepository;
    private List<BondsTypesFixer> bondsTypesFixers;

    @Inject
    public WidgetService(WidgetRepository widgetRepository, List<BondsTypesFixer> bondsTypesFixers) {
        this.widgetRepository = widgetRepository;
        this.bondsTypesFixers = bondsTypesFixers;
    }

    public List<Property> updateProperty(String widgetId, String propertyName, Property property) {
        for (BondsTypesFixer bondsTypesFixer : bondsTypesFixers) {
            bondsTypesFixer.fixBondsTypes(widgetId, singletonList(property));
        }
        return widgetRepository.updateProperty(widgetId, propertyName, property);
    }
}
