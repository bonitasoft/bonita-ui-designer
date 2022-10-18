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
import static java.nio.file.Files.readAllBytes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This Persister is used to manage the persistence logic for a widget. Each of them are serialized in a json file
 */
public class WidgetFileBasedLoader extends JsonFileBasedLoader<Widget> {

    protected static final Logger logger = LoggerFactory.getLogger(WidgetFileBasedLoader.class);

    public WidgetFileBasedLoader(JsonHandler jsonHandler) {
        super(jsonHandler, Widget.class);
    }

    @Override
    public Widget get(Path path) {
        try {
            var widget = getWidgetWithView(path, null);
            return applyMetadata(path, widget);
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting metadata (for file [%s])", path.getFileName()), e);
        }
    }

    private Widget getWidgetWithView(Path path, Class<?> view) {
        try {
             var widget = (view == null)
                    ? jsonHandler.fromJson(readAllBytes(path), type)
                    : jsonHandler.fromJson(readAllBytes(path), type, view);

            widget.prepareWidgetToDeserialize(path.getParent());
            return widget;
        } catch (JsonProcessingException e) {
            throw new JsonReadException(format("Could not read json file [%s]", path.getFileName()), e);
        } catch (NoSuchFileException e) {
            throw new NotFoundException(format("Could not load component, unexpected structure in the file [%s]", path.getFileName()));
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting component (on file [%s])", path.getFileName()), e);
        }
    }

    @Override
    public List<Widget> findByObjectId(Path directory, String objectId) throws IOException {
        List<Widget> findIn = new ArrayList<>();
        var widgets = getAll(directory);
        for (var otherWidget : widgets) {
            if (otherWidget.getTemplate().contains("<" + Widget.spinalCase(objectId))) {
                findIn.add(otherWidget);
            }
        }
        return findIn;
    }

    @Override
    public boolean contains(Path directory, String widgetId) throws IOException {
        return Files.exists(directory.resolve(widgetId).resolve(widgetId + ".json"));
    }

    @Override
    public Widget getByUUID(Path directory, String uuid) throws IOException {
        return null;
    }

    @Override
    public Widget load(Path path) {
        return getWidgetWithView(path, JsonViewPersistence.class);
    }

}
