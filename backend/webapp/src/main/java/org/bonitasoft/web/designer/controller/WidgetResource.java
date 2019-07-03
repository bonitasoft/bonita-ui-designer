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

import com.google.common.base.Optional;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.WidgetContainerRepository;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@RestController
@RequestMapping("/rest/widgets")
public class WidgetResource extends AssetResource<Widget>{

    private JacksonObjectMapper objectMapper;
    private WidgetRepository widgetRepository;
    private WidgetService widgetService;
    private Path widgetPath;
    private List<WidgetContainerRepository> widgetContainerRepositories;
    private Map<String, Set<String>> widgetDependencies; // Map of widget -> dependencies (pages/fragments)

    @Inject
    public WidgetResource(JacksonObjectMapper objectMapper,
                          WidgetRepository widgetRepository,
                          WidgetService widgetService,
                          AssetService<Widget> widgetAssetService,
                          @Named("widgetPath") Path widgetPath,
                          List<WidgetContainerRepository> widgetContainerRepositories,
                          AssetVisitor assetVisitor) {
        super(widgetAssetService, widgetRepository, assetVisitor, Optional.<SimpMessagingTemplate>absent());
        this.widgetRepository = widgetRepository;
        this.objectMapper = objectMapper;
        this.widgetService = widgetService;
        this.widgetPath = widgetPath;
        this.widgetContainerRepositories = widgetContainerRepositories;
    }

    @Override
    protected void checkArtifactId(String artifactId) {
        checkWidgetIdIsNotAPbWidget(artifactId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getAll(@RequestParam(value = "view", defaultValue = "full") String view) throws RepositoryException, IOException {
        byte[] json;
        List<Widget> widgets = widgetRepository.getAll();
        if ("light".equals(view)) {
            fillWithUsedBy(widgets);
            json = objectMapper.toJson(widgets, JsonViewLight.class);
        } else {
            json = objectMapper.toJson(widgets);
        }
        //In our case we don't know the view asked outside this method. So like we can't know which JsonView used, I
        //build the json manually but in the return I must specify the mime-type in the header
        //{@link ResourceControllerAdvice#getHttpHeaders()}
        return new ResponseEntity<>(new String(json), ResourceControllerAdvice.getHttpHeaders(), HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    private void fillWithUsedBy(Widget widget) {
        for (WidgetContainerRepository<Identifiable> repository : widgetContainerRepositories) {
            widget.addUsedBy(repository.getComponentName(), repository.getArtifactsUsingWidget(widget.getId()));
        }
    }

    @SuppressWarnings("unchecked")
    private void fillWithUsedBy(List<Widget> widgets) {
        List<String> widgetIds = new ArrayList<>();
        for (Widget widget : widgets) {
            widgetIds.add(widget.getId());
        }
        Map<String, List<Identifiable>> map = new HashMap<>();
        for (WidgetContainerRepository<Identifiable> repository : widgetContainerRepositories) {
            map = repository.getArtifactsUsingWidgets(widgetIds);
            for (Widget widget : widgets) {
                widget.addUsedBy(repository.getComponentName(), map.get(widget.getId()));
            }
        }
    }

    @RequestMapping(value = "/{widgetId}", method = RequestMethod.GET)
    public Widget get(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotAllowedException {
        Widget widget = widgetService.get(widgetId);
        widget.setAssets(assetVisitor.visit(widget));
        return widget;
    }

    @RequestMapping(method = RequestMethod.POST)
    public Widget create(@RequestBody Widget widget, @RequestParam(value = "duplicata", required = false) String sourceWidgetId) throws IllegalArgumentException {
        Widget newWidget = widgetRepository.create(widget);
        if(isNotEmpty(sourceWidgetId)) {
            assetService.duplicateAsset(widgetPath, widgetRepository.resolvePath(sourceWidgetId), sourceWidgetId, newWidget.getId());
        }
        return newWidget;
    }

    @RequestMapping(value = "/{widgetId}", method = RequestMethod.PUT)
    public void save(@PathVariable("widgetId") String widgetId, @RequestBody Widget widget) throws RepositoryException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only save a custom widget");
        }
        widget.setId(widgetId);
        widgetRepository.updateLastUpdateAndSave(widget);
    }

    @RequestMapping(value = "/{widgetId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotFoundException, NotAllowedException {
        Widget widget = widgetRepository.get(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only delete a custom widget");
        }

        fillWithUsedBy(widget);
        //if this widget is used elsewhere we prevent the deletion.
        if (widget.isUsed()) {
            throw new InUseException(buildErrorMessage(widget));
        }

        widgetRepository.delete(widgetId);
    }

    private String buildErrorMessage(Widget widget) {
        //if an error occurred it's useful for user to know which components use this widget
        StringBuilder msg = new StringBuilder("The widget cannot be deleted because it is used in");

        for (Entry<String, List<Identifiable>> entry : widget.getUsedBy().entrySet()) {
            List<? extends Identifiable> elements = entry.getValue();
            if (!elements.isEmpty()) {
                msg.append(" ").append(elements.size()).append(" " + entry.getKey()).append(elements.size() > 1 ? "s" : "");
                for (Identifiable element : elements) {
                    msg.append(", <").append(element.getName()).append(">");
                }
            }
        }
        return msg.toString();
    }

    @RequestMapping(value = "/{widgetId}/properties", method = RequestMethod.POST)
    public List<Property> addProperty(@PathVariable("widgetId") String widgetId, @RequestBody Property property) throws RepositoryException, NotFoundException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        return widgetRepository.addProperty(widgetId, property);
    }

    @RequestMapping(value = "/{widgetId}/properties/{propertyName}", method = RequestMethod.PUT)
    public List<Property> updateProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName, @RequestBody Property property) throws RepositoryException, NotFoundException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        return widgetService.updateProperty(widgetId, propertyName, property);
    }

    @RequestMapping(value = "/{widgetId}/properties/{propertyName}", method = RequestMethod.DELETE)
    public List<Property> deleteProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName) throws RepositoryException, NotFoundException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        return widgetRepository.deleteProperty(widgetId, propertyName);
    }

    @RequestMapping(value = "/{widgetId}/favorite", method = RequestMethod.PUT)
    public void favorite(@PathVariable("widgetId") String pageId, @RequestBody Boolean favorite) throws RepositoryException {
        if (favorite) {
            widgetRepository.markAsFavorite(pageId);
        } else {
            widgetRepository.unmarkAsFavorite(pageId);
        }
    }

    @RequestMapping(value = "/{widgetId}/help", method = RequestMethod.GET,  produces = "text/html; charset=UTF-8")
    public void serveWidgetFiles(HttpServletRequest request, HttpServletResponse response, @PathVariable("widgetId") String widgetId) throws IOException {
        HttpFile.writeFileInResponse(request, response, widgetPath.resolve(widgetId+"/help.html"));
    }

    private void checkWidgetIdIsNotAPbWidget(String widgetId) {
        if (isPbWidgetId(widgetId)) {
            throw new NotAllowedException("Not allowed to modify a non custom widgets");
        }
    }

    private boolean isPbWidgetId(String widgetId) {
        return widgetId.startsWith("pb");
    }
}
