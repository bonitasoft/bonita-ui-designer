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
package org.bonitasoft.web.designer.experimental.parametrizedWidget;

import static com.google.common.collect.Maps.transformEntries;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps.EntryTransformer;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;

public abstract class AbstractParametrizedWidget implements ParametrizedWidget {

    private String widgetId;
    private Integer dimension = 12;
    private String label;
    private boolean isDisplayed = true;
    private boolean labelHidden = false;
    private boolean readOnly = false;
    private String alignment = Alignment.LEFT.getValue();
    private String cssClasses = "";

    private static final Map<String, ParameterType> propertyParameters = new HashMap<>();
    static {
        propertyParameters.put(COLLECTION_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(VALUE_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(DATA_TO_SEND_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(REPEATED_COLLECTION_PARAMETER, ParameterType.VARIABLE);
        propertyParameters.put(TARGET_URL_ON_SUCCESS_PARAMETER, ParameterType.INTERPOLATION);
    }

    private Map<String, PropertyValue> propertyValues = new HashMap<>();

    public AbstractParametrizedWidget(String widgetId) {
        this.widgetId = widgetId;
    }

    public AbstractParametrizedWidget() {
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Integer getDimension() {
        return dimension;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean getIsDisplayed() {
        return isDisplayed;
    }

    public void setIsDisplayed(boolean isDisplayed) {
        this.isDisplayed = isDisplayed;
    }

    public boolean isLabelHidden() {
        return labelHidden;
    }

    public void setLabelHidden(boolean labelHidden) {
        this.labelHidden = labelHidden;
    }

    public boolean isReadonly() {
        return readOnly;
    }

    public void setReadonly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment.getValue();
    }

    public String getCssClasses() {
        return cssClasses;
    }

    public void setCssClasses(String cssClasses) {
        this.cssClasses = cssClasses;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Element> T getAdapter(Class<T> adapterClass) {
        if (Component.class.equals(adapterClass)) {
            Component component = new Component();
            component.setId(getWidgetId());
            component.setDimension(ImmutableSortedMap.of("xs", dimension));
            component.setPropertyValues(toPropertyValues());
            return (T) component;
        }
        return null;
    }

    protected Map<String, PropertyValue> toPropertyValues() {
        return transformEntries(toMap(), toPropertyValue());
    }

    private Map<String, Object> toMap() {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            BeanInfo info = Introspector.getBeanInfo(this.getClass());
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null) {
                    params.put(pd.getName(), reader.invoke(this));
                }
            }
            return params;
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return Collections.emptyMap();
        }
    }

    private EntryTransformer<String, Object, PropertyValue> toPropertyValue() {
        return new EntryTransformer<String, Object, PropertyValue>() {

            @Override
            public PropertyValue transformEntry(String paramName, Object value) {
                return propertyValues.containsKey(paramName) ? propertyValues.get(paramName) : createPropertyValue(getParameterType(paramName), value);
            }
        };
    }

    private ParameterType getParameterType(String paramName) {
        return propertyParameters.containsKey(paramName) ? propertyParameters.get(paramName) : ParameterType.CONSTANT;
    }

    private PropertyValue createPropertyValue(ParameterType type, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setType(type.getValue());
        propertyValue.setValue(value);
        return propertyValue;
    }

    public void setPropertyValue(String paramName, ParameterType type, Object value) {
        propertyValues.put(paramName, createPropertyValue(type, value));
    }
}
