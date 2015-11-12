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

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/widgets")
public class WidgetResource extends AssetResource<Widget>{
    private static final Logger logger = LoggerFactory.getLogger(WidgetResource.class);
    private JacksonObjectMapper objectMapper;
    private WidgetRepository repository;
    private List<Repository> usedByRepositories;
    private Path widgetPath;

    @Inject
    public WidgetResource(JacksonObjectMapper objectMapper, WidgetRepository repository, AssetService<Widget> widgetAssetService, @Named("widgetPath") Path widgetPath) {
        super(widgetAssetService, repository, null);
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.widgetPath = widgetPath;
    }

    @Override
    protected void checkArtifactId(String artifactId) {
        checkWidgetIdIsNotAPbWidget(artifactId);
    }

    /**
     * List cannot be injected in constructor with @Inject so we use setter and @Resource to inject them
     */
    @Resource(name = "widgetsUsedByRepositories")
    public void setUsedByRepositories(List<Repository> usedByRepositories) {
        this.usedByRepositories = usedByRepositories;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getAll(@RequestParam(value = "view", defaultValue = "full") String view) throws RepositoryException, IOException {
        byte[] json;
        List<Widget> widgets = repository.getAll();
        if ("light".equals(view)) {
            for (Widget widget : widgets) {
                fillWithUsedBy(widget);
            }
            json = objectMapper.toJson(widgets, JsonViewLight.class);
        } else {
            json = objectMapper.toJson(widgets);
        }
        //In our case we don't know the view asked outside this method. So like we can't know which JsonView used, I
        //build the json manually but in the return I must specify the mime-type in the header
        //{@link ResourceControllerAdvice#getHttpHeaders()}
        return new ResponseEntity<>(new String(json), ResourceControllerAdvice.getHttpHeaders(), HttpStatus.OK);
    }

    private void fillWithUsedBy(Widget widget) {
        for (Repository repo : usedByRepositories) {
            widget.addUsedBy(repo.getComponentName(), repo.findByObjectId(widget.getId()));
        }
    }

    @RequestMapping(value = "/{widgetId}", method = RequestMethod.GET)
    public Widget get(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotAllowedException {
        return repository.get(widgetId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public Widget create(@RequestBody Widget widget, @RequestParam(value = "duplicata", required = false) String sourceWidgetId) throws IllegalArgumentException {
        Widget newWidget = repository.create(widget);
        if(isNotEmpty(sourceWidgetId)) {
            assetService.duplicateAsset(widgetPath, repository.resolvePath(sourceWidgetId), sourceWidgetId, newWidget.getId());
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
        repository.updateLastUpdateAndSave(widget);
    }

    @RequestMapping(value = "/{widgetId}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("widgetId") String widgetId) throws RepositoryException, NotFoundException, NotAllowedException {
        Widget widget = repository.get(widgetId);
        if (!widget.isCustom()) {
            throw new NotAllowedException("We can only delete a custom widget");
        }

        fillWithUsedBy(widget);
        //if this widget is used elsewhere we prevent the deletion.
        if (widget.isUsed()) {
            throw new InUseException(buildErrorMessage(widget));
        }

        repository.delete(widgetId);
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
        return repository.addProperty(widgetId, property);
    }

    @RequestMapping(value = "/{widgetId}/properties/{propertyName}", method = RequestMethod.PUT)
    public List<Property> updateProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName, @RequestBody Property property) throws RepositoryException, NotFoundException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        return repository.updateProperty(widgetId, propertyName, property);
    }

    @RequestMapping(value = "/{widgetId}/properties/{propertyName}", method = RequestMethod.DELETE)
    public List<Property> deleteProperty(@PathVariable("widgetId") String widgetId, @PathVariable("propertyName") String propertyName) throws RepositoryException, NotFoundException, NotAllowedException {
        checkWidgetIdIsNotAPbWidget(widgetId);
        return repository.deleteProperty(widgetId, propertyName);
    }

    @RequestMapping(value = "/{widgetId}/favorite", method = RequestMethod.PUT)
    public void favorite(@PathVariable("widgetId") String pageId, @RequestBody Boolean favorite) throws RepositoryException {
        if (favorite) {
            repository.markAsFavorite(pageId);
        } else {
            repository.unmarkAsFavorite(pageId);
        }
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
