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
package org.bonitasoft.web.designer.controller;

import static org.springframework.util.StringUtils.hasText;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.common.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.common.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.common.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/widgets")
public class WidgetResource extends AssetResource<Widget, WidgetService> {
    private Path widgetPath;

    @Autowired
    public WidgetResource(JsonHandler jsonHandler, WidgetService widgetService, SimpMessagingTemplate messagingTemplate, WorkspaceProperties workspaceProperties) {
        super(jsonHandler, widgetService, messagingTemplate);
        this.widgetPath = workspaceProperties.getWidgets().getDir();
    }

    @GetMapping
    public ResponseEntity<String> getAll(@RequestParam(value = "view", defaultValue = "full") String view) throws RepositoryException, IOException {

        byte[] json;
        if ("light".equals(view)) {
            var widgets = service.getAllWithUsedBy();
            json = jsonHandler.toJson(widgets, JsonViewLight.class);
        } else {
            var widgets = service.getAll();
            json = jsonHandler.toJson(widgets);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(new String(json));
    }

    @GetMapping(value = "/{widgetId}")
    public ResponseEntity<Object> get(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotAllowedException {
        var widget = service.getWithAsset(widgetId);

        if (!widget.isCompatible()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Widget " + widgetId + " is in an incompatible version. Newer UI Designer version is required.");
        }

        return ResponseEntity.ok(widget);
    }

    @PostMapping
    public Widget create(@RequestBody Widget widget, @RequestParam(value = "duplicata", required = false) String sourceWidgetId) throws IllegalArgumentException {
        Widget savedWidget;
        if (hasText(sourceWidgetId)) {
            savedWidget = service.createFrom(sourceWidgetId, widget);
        } else {
            savedWidget = service.create(widget);
        }
        return savedWidget;
    }

    @PutMapping(value = "/{widgetId}")
    public void save(@PathVariable("widgetId") String widgetId, @RequestBody Widget widget) throws RepositoryException, NotAllowedException {
        service.save(widgetId, widget);
    }

    @DeleteMapping(value = "/{widgetId}")
    public void delete(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotFoundException, NotAllowedException {
        service.delete(widgetId);
    }

    @PostMapping(value = "/{widgetId}/properties")
    public List<Property> addProperty(@PathVariable("widgetId") String widgetId, @RequestBody Property property) throws RepositoryException, NotFoundException, NotAllowedException {
        return service.addProperty(widgetId, property);
    }

    @PutMapping(value = "/{widgetId}/properties/{propertyName}")
    public List<Property> updateProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName, @RequestBody Property property) throws RepositoryException, NotFoundException, NotAllowedException {
        return service.updateProperty(widgetId, propertyName, property);
    }

    @DeleteMapping(value = "/{widgetId}/properties/{propertyName}")
    public List<Property> deleteProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName) throws RepositoryException, NotFoundException, NotAllowedException {
        return service.deleteProperty(widgetId, propertyName);
    }

    @PutMapping(value = "/{widgetId}/favorite")
    public void favorite(@PathVariable("widgetId") String widgetId, @RequestBody Boolean favorite) throws RepositoryException {
        service.markAsFavorite(widgetId, favorite);
    }

    @GetMapping(value = "/{widgetId}/help", produces = "text/html; charset=UTF-8")
    public void serveWidgetFiles(HttpServletRequest request, HttpServletResponse response, @PathVariable("widgetId") String widgetId) throws IOException {
        HttpFile.writeFileInResponse(request, response, widgetPath.resolve(widgetId + "/help.html"));
    }

}
