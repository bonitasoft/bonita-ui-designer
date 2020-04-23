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
package org.bonitasoft.web.designer.generator.parametrizedWidget;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bonitasoft.web.designer.generator.mapping.DimensionFactory;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.PropertyValue;

@Widget
public abstract class AbstractParametrizedWidget implements ParametrizedWidget {

    private String widgetId;

    @WidgetProperty
    private String cssClasses = "";
    @WidgetProperty
    private String hidden = "";
    @WidgetProperty
    private Integer dimension = 12;
    @WidgetProperty
    private String description = "";

    protected static final Map<String, ParameterType> propertyParameters = new HashMap<>();

    static {
        propertyParameters.put(COLLECTION_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(REMOVE_ITEM_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(VALUE_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(DATA_TO_SEND_PARAMETER, ParameterType.EXPRESSION);
        propertyParameters.put(VALUE_TO_ADD_PARAMETER, ParameterType.EXPRESSION);
        propertyParameters.put(REPEATED_COLLECTION_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(TARGET_URL_ON_SUCCESS_PARAMETER, ParameterType.INTERPOLATION);
        propertyParameters.put(TEXT_PARAMETER, ParameterType.INTERPOLATION);
        propertyParameters.put(LABEL_PARAMETER, ParameterType.INTERPOLATION);
        propertyParameters.put(URL_PARAMETER, ParameterType.EXPRESSION);
        propertyParameters.put(AVAILABLE_VALUES_PARAMETER, ParameterType.EXPRESSION);
        propertyParameters.put(PLACEHOLDER_PARAMETER, ParameterType.CONSTANT);
        propertyParameters.put(LABEL_HIDDEN_PARAMETER, ParameterType.CONSTANT);
        propertyParameters.put(READONLY_PARAMETER, ParameterType.CONSTANT);
        propertyParameters.put(HIDDEN_PARAMETER, ParameterType.EXPRESSION);
    }

    private Map<String, PropertyValue> propertyValues = new HashMap<>();

    public AbstractParametrizedWidget(String widgetId) {
        this.widgetId = widgetId;
    }

    public AbstractParametrizedWidget() {
    }

    @Override
    public String getWidgetId() {
        return widgetId;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    @Override
    public Integer getDimension() {
        return dimension;
    }

    public String getCssClasses() {
        return cssClasses;
    }

    public void setCssClasses(String cssClasses) {
        this.cssClasses = cssClasses;
    }

    public String getHidden() {
        return hidden;
    }

    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public Component toComponent(DimensionFactory dimensionFactory) {
        Component component = new Component();
        component.setId(getWidgetId());
        component.setDimension(dimensionFactory.create(dimension));
        component.setPropertyValues(toPropertyValues());
        component.setDescription(description);
        return component;
    }

    protected Map<String, PropertyValue> toPropertyValues() {
        Map<String, PropertyValue> values = toMap().entrySet()
                .stream()
                .collect(Collectors.toMap(this::getEntryKey, this::createPropertyValue));
        values.putAll(propertyValues);
        return values;
    }

    protected String getEntryKey(Entry<String, Object> entry) {
        return entry.getKey();
    }

    private Map<String, Object> toMap() {
        try {
            Map<String, Object> params = new HashMap<>();
            ArrayList<String> availableProperties = new ArrayList<>();
            availableProperties.addAll(classesIntrospection(this.getClass()));

            BeanInfo info = Introspector.getBeanInfo(this.getClass());
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null && availableProperties.contains(pd.getName())) {
                    params.put(pd.getName(), reader.invoke(this));
                }
            }
            return params;
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Collections.emptyMap();
        }
    }

    private List<String> classesIntrospection(Class<?> aClass) {
        ArrayList<String> list = new ArrayList<>();
        for (Field field : aClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(WidgetProperty.class)) {
                list.add(field.getName());
            }
        }
        if (aClass.getSuperclass().isAnnotationPresent(Widget.class)) {
            list.addAll(classesIntrospection(aClass.getSuperclass()));
        }
        return list;
    }

    private ParameterType getParameterType(String paramName) {
        return propertyParameters.containsKey(paramName) ? propertyParameters.get(paramName) : ParameterType.CONSTANT;
    }

    protected PropertyValue createPropertyValue(Entry<String, Object> entry) {
        if (Objects.equals(entry.getKey(), HIDDEN_PARAMETER)) {
            if (hidden == null || hidden.isEmpty()) {
                return createPropertyValue(ParameterType.CONSTANT, false);
            }
        }
        return createPropertyValue(getParameterType(entry.getKey()), entry.getValue());
    }

    protected PropertyValue createPropertyValue(ParameterType type, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setType(type.getValue());
        propertyValue.setValue(value);
        return propertyValue;
    }

    public void setPropertyValue(String paramName, ParameterType type, Object value) {
        propertyValues.put(paramName, createPropertyValue(type, value));
    }
}
