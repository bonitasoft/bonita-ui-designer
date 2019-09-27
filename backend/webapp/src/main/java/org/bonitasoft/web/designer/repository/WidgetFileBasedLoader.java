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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.JsonReadException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;

/**
 * This Persister is used to manage the persistence logic for a widget. Each of them are serialized in a json file
 */
public class WidgetFileBasedLoader extends JsonFileBasedLoader<Widget> {

    protected static final Logger logger = LoggerFactory.getLogger(WidgetFileBasedLoader.class);

    @Inject
    public WidgetFileBasedLoader(JacksonObjectMapper objectMapper) {
        super(objectMapper, Widget.class);
    }

    private void setWidgetTemplate(Path directory, Widget widget) throws IOException {
        if (widget.getTemplate() != null && widget.getTemplate().startsWith("@")) {
            String templateFileName = widget.getTemplate().substring(1);
            Path templateFile = directory.resolve(format("%s", templateFileName));
            widget.setTemplate(new String(readAllBytes(templateFile)));
        }
    }

    private void setWidgetController(Path directory, Widget widget) throws IOException {
        if (widget.getController() != null && widget.getController().startsWith("@")) {
            String controllerFileName = widget.getController().substring(1);
            Path controllerFile = directory.resolve(format("%s", controllerFileName));
            widget.setController(new String(readAllBytes(controllerFile)));
        }
    }

    @Override
    public Widget get(Path path) {
        try {
            Widget widget = getWigetWithView(path, null);
            Path metadata = path.getParent().getParent().resolve(format(".metadata/%s.json", path.getParent().getFileName()));
            if (exists(metadata)) {
                widget = objectMapper.assign(widget, readAllBytes(metadata));
            }
            return widget;
        } catch (IOException e) {
            throw new RepositoryException(format("Error while getting metadata (for file [%s])", path.getFileName()), e);
        }
    }

    private Widget getWigetWithView(Path path, Class<?> view) {
        try {
            Widget widget = (view == null)
                    ? objectMapper.fromJson(readAllBytes(path), type)
                    : objectMapper.fromJson(readAllBytes(path), type, view);

            Path widgetDirectoryPath = path.getParent();
            setWidgetTemplate(widgetDirectoryPath, widget);
            setWidgetController(widgetDirectoryPath, widget);
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
        List<Widget> widgets = getAll(directory);
        for (Widget otherWidget : widgets) {
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

    public List<Widget> loadAllCustom(Path widgetsFolder) throws IOException {
        return loadAll(widgetsFolder, new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path path) throws IOException {
                return !path.getFileName().toString().startsWith("pb");
            }
        });
    }

    @Override
    public Widget getByUUID(Path directory, String uuid) throws IOException {
        return null;
    }

    @Override
    public Widget load(Path path) {
        return getWigetWithView(path, JsonViewPersistence.class);
    }

}
