/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.web.designer.visitor;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bonitasoft.web.designer.controller.export.properties.BonitaBusinessDataResourcePredicate;
import org.bonitasoft.web.designer.controller.export.properties.BonitaResourceTransformer;
import org.bonitasoft.web.designer.controller.export.properties.BonitaVariableResourcePredicate;
import org.bonitasoft.web.designer.controller.export.properties.ResourceURLFunction;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.page.PropertyValue;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;
import org.bonitasoft.web.designer.model.page.WebResource;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import lombok.RequiredArgsConstructor;

/**
 * An element visitor which traverses the tree of elements recursively to collect property values in a page
 */
@RequiredArgsConstructor
public class WebResourcesVisitor implements ElementVisitor<Map<String, WebResource>> {

    static final String SUBMIT_TASK = "Submit task";
    private static final String START_PROCESS = "Start process";
    private static final String BUSINESS_DATA_RESOURCE = "GET|bdm/businessData";
    private static final String BUSINESS_DATA_QUERY_RESOURCE = "GET|bdm/businessDataQuery";
    
    private final FragmentRepository fragmentRepository;

    private final WidgetRepository widgetRepository;
    public static final String BONITA_RESOURCE_REGEX = ".+/API/(?!extension)([^ /]*)/([^ /(?|{)]*)[\\S+]*";// matches ..... /API/{}/{}?...

    public static final String EXTENSION_RESOURCE_REGEX = ".+/API/(?=extension)([^ /]*)/([^ (?|{)]*).*";
  

    @Override
    public Map<String, WebResource> visit(FragmentElement fragmentElement) {
        Map<String, WebResource> mapResources = new HashMap<>();
        var fragment = fragmentRepository.get(fragmentElement.getId());
        addResourceScope(mapResources, getResourcesFromVariables(fragment.getVariables()), "Variable");
        fragment.getWebResources().stream()
                .forEach(wr -> overrideScopeToKeepOnlyOneLevel(fragmentElement, mapResources, wr));
        fragment.toContainer(fragmentElement).accept(this).values().stream()
                .forEach(wr -> overrideScopeToKeepOnlyOneLevel(fragmentElement, mapResources, wr));

        return mapResources;
    }

    @Override
    public Map<String, WebResource> visit(Component component) {
        Map<String, WebResource> mapResources = new HashMap<>();
        // Start process and submit task Button
        if (withAction(component, START_PROCESS)) {
            mapResources.put("POST|bpm/process", new WebResource("POST", "bpm/process", component.getId()));
        }
        if (withAction(component, SUBMIT_TASK)) {
            mapResources.put("POST|bpm/userTask", new WebResource("POST", "bpm/userTask", component.getId()));
        }

        addIfExist(component, "GET", "url").ifPresent(wr -> mapResources.put(wr.toDefinition(), wr));
        addIfExist(component, "POST", "url").ifPresent(wr -> mapResources.put(wr.toDefinition(), wr));
        addIfExist(component, "PUT", "url").ifPresent(wr -> mapResources.put(wr.toDefinition(), wr));
        addIfExist(component, "DELETE", "url").ifPresent(wr -> mapResources.put(wr.toDefinition(), wr));

        // Api url on DataTable
        findResourcesIn(component, "apiUrl", "GET").ifPresent(
                s -> mapResources.put(String.format("GET|%s", s), new WebResource("GET", s, component.getId())));

        if ("pbUpload".equals(component.getId())) {
            findResourcesIn(component, "url", "POST").ifPresent(
                    s -> mapResources.put(String.format("POST|%s", s), new WebResource("POST", s, component.getId())));
        }

        // Load WebResources declare manually on custom-widget
        Widget widget = widgetRepository.get(component.getId());
        widget.getWebResources().stream().forEach(wr -> {
            wr.setScopes(Set.of(component.getId()));
            wr.setAutomatic(true);
            mapResources.put(wr.toDefinition(), wr);
        });

        return mapResources;
    }

    private Optional<WebResource> addIfExist(Component component, String httpVerb, String propertyName) {
        if (withAction(component, httpVerb)) {
            var resources = findResourcesIn(component, propertyName, httpVerb);
            if (resources.isPresent()) {
                return Optional.of(new WebResource(httpVerb, resources.get(), component.getId()));
            }
        }
        return Optional.empty();
    }

    private Optional<String> findResourcesIn(Component component, String propertyName, String httpVerb) {
        return Optional.ofNullable(component)
                .map(propertyValue(propertyName))
                .filter(Objects::nonNull)
                .filter(propertyType(ParameterType.CONSTANT).or(propertyType(ParameterType.INTERPOLATION)))
                .filter(notNullOrEmptyValue())
                .map(toPageResource(httpVerb))
                .filter(Objects::nonNull);
    }

    private Predicate<? super PropertyValue> notNullOrEmptyValue() {
        return propertyValue -> propertyValue.getValue() != null && !propertyValue.getValue().toString().isEmpty();
    }

    private Function<PropertyValue, String> toPageResource(String httpVerb) {
        return propertyValue -> {
            String value = propertyValue.getValue().toString();
            return value.matches(BONITA_RESOURCE_REGEX)
                    ? new ResourceURLFunction(BONITA_RESOURCE_REGEX, httpVerb).applyApi(value)
                    : value.matches(EXTENSION_RESOURCE_REGEX)
                            ? new ResourceURLFunction(EXTENSION_RESOURCE_REGEX, httpVerb).applyApi(value) : null;
        };
    }

    private Function<Component, PropertyValue> propertyValue(String propertyName) {
        return component -> component.getPropertyValues().get(propertyName);
    }

    private Predicate<PropertyValue> propertyType(ParameterType type) {
        return propertyValue -> Objects.equals(type.getValue(), propertyValue.getType());
    }

    @Override
    public Map<String, WebResource> visit(Container container) {
        Map<String, WebResource> data = new HashMap<>();
        container.getRows().forEach(rows -> rows.forEach(el -> data.putAll(el.accept(this))));
        return data;
    }

    @Override
    public Map<String, WebResource> visit(FormContainer formContainer) {
        return formContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, WebResource> visit(TabsContainer tabsContainer) {
        Map<String, WebResource> data = new HashMap<>();
        tabsContainer.getTabList().forEach(tabContainer -> data.putAll(tabContainer.accept(this)));
        return data;
    }

    @Override
    public Map<String, WebResource> visit(TabContainer tabContainer) {
        return tabContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, WebResource> visit(ModalContainer modalContainer) {
        return modalContainer.getContainer().accept(this);
    }

    @Override
    public Map<String, WebResource> visit(Previewable previewable) {
        Map<String, WebResource> mapResources = new HashMap<>();

        // Resource from variable
        addResourceScope(mapResources, getResourcesFromVariables(previewable.getVariables()), "Variable");

        // Resource from Child component/fragment
        previewable.getRows().stream()
                .forEach(row -> row.stream().forEach(element -> addWebResourceScope(mapResources,
                        element.accept(this).values().stream().collect(Collectors.toList()))));

        return mapResources;
    }

    private List<String> getResourcesFromVariables(Map<String, Variable> variables) {
        List<String> resources = variables.values().stream()
                .filter(new BonitaVariableResourcePredicate(BONITA_RESOURCE_REGEX))
                .map(new BonitaResourceTransformer(BONITA_RESOURCE_REGEX))
                .collect(toList());

        variables.values().stream()
                .filter(new BonitaVariableResourcePredicate(EXTENSION_RESOURCE_REGEX))
                .map(new BonitaResourceTransformer(EXTENSION_RESOURCE_REGEX))
                .forEach(resources::add);

        if (containsBusinessDataVariable(variables.values())) {
            resources.add(BUSINESS_DATA_RESOURCE);
            resources.add(BUSINESS_DATA_QUERY_RESOURCE);
        }

        return resources;
    }

    private static boolean containsBusinessDataVariable(Collection<Variable> variables) {
        return variables.stream().anyMatch(new BonitaBusinessDataResourcePredicate());
    }

    private boolean withAction(Component component, String action) {
        return component.getPropertyValues().containsKey("action") && Objects.equals(action,
                String.valueOf(component.getPropertyValues().get("action").getValue()));
    }

    private void addResourceScope(Map<String, WebResource> mapResources, List<String> entryResources, String scope) {
        entryResources.forEach(r -> {
            if (mapResources.containsKey(r)) {
                mapResources.get(r).addToScopes(scope);
            } else {
                var wr = new WebResource(r.split("\\|")[0], r.split("\\|")[1], scope);
                mapResources.put(r, wr);
            }
        });
    }

    private void addWebResourceScope(Map<String, WebResource> mapResources, List<WebResource> wr) {
        wr.forEach(r -> {
            if (mapResources.containsKey(r.toDefinition())) {
                mapResources.get(r.toDefinition()).getScopes().addAll(r.getScopes());
            } else {
                mapResources.put(r.toDefinition(), r);
            }
        });
    }

    private void overrideScopeToKeepOnlyOneLevel(FragmentElement fragmentElement, Map<String, WebResource> mapResources,
            WebResource wr) {
        wr.setScopes(Arrays.asList(fragmentElement.getId()).stream().collect(Collectors.toSet()));
        wr.setAutomatic(true);
        mapResources.put(wr.toDefinition(), wr);
    }
}
