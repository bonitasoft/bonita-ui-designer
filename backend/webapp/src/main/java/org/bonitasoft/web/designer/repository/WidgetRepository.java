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
package org.bonitasoft.web.designer.repository;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.text.WordUtils.capitalize;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

@Named
public class WidgetRepository extends AbstractRepository<Widget> {

    @Inject
    public WidgetRepository(
            @Named("widgetPath") Path path,
            @Named("widgetFileBasedPersister") JsonFileBasedPersister<Widget> fileBasedRepository,
            WidgetLoader widgetLoader,
            BeanValidator validator,
            Watcher watcher) {
        super(path, fileBasedRepository, widgetLoader, validator, watcher);
    }

    @Override
    public String getComponentName() {
        return "widget";
    }

    @Override
    public void delete(String widgetId) throws RepositoryException, NotAllowedException, InUseException {
        Widget widget = get(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only delete a custom widget");
        }
        super.delete(widgetId);
    }

    /**
     * Create a new widget, computing id with widget name
     */
    public Widget create(Widget widget) throws IllegalArgumentException {
        String id = "custom" + trimToEmpty(capitalize(widget.getName()));
        try {
            Widget existingWidget = get(id);
            throw new NotAllowedException("Widget with name " + existingWidget.getName() + " already exists");
        } catch (NotFoundException e) {
            widget.setCustom(true);
            widget.setId(id);
            createComponentDirectory(widget);
            updateLastUpdateAndSave(widget);
            return widget;
        }

    }

    public List<Widget> getByIds(Set<String> widgetIds) {
        List<Widget> result = new ArrayList<>();
        for (String widgetId : widgetIds) {
            result.add(get(widgetId));
        }
        return result;
    }

    public List<Property> addProperty(String widgetId, Property property) {
        Widget widget = get(widgetId);
        Property existingProperty = widget.getProperty(property.getName());
        if (existingProperty != null) {
            throw new NotAllowedException(format("Widget [ %s ] has already a property named %s", widgetId, property.getName()));
        }
        widget.addProperty(property);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
    }

    public List<Property> updateProperty(String widgetId, String propertyName, Property property) {
        Widget widget = get(widgetId);
        Property existingProperty = widget.getProperty(propertyName);
        if (existingProperty == null) {
            throw new NotFoundException(format("Widget [ %s ] has no property named %s", widgetId,  propertyName));
        }
        widget.replaceProperty(existingProperty, property);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
     }

    public List<Property> deleteProperty(String widgetId, String propertyName) {
        Widget widget = get(widgetId);
        Property param = widget.getProperty(propertyName);
        if (param == null) {
            throw new NotFoundException(format("Widget [ %s ] has no property named %s", widgetId,  propertyName));
        }
        widget.deleteProperty(param);
        updateLastUpdateAndSave(widget);
        return widget.getProperties();
     }

}
