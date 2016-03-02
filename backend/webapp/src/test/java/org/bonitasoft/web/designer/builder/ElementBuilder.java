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
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.PropertyValue;

public abstract class ElementBuilder<E extends Element> {

    public ElementBuilder<E> withDimension(int size) {
        return withDimensions(new Dimension("xs", size));
    }

    public ElementBuilder<E> withDimensions(Dimension... dimensions) {
        for (Dimension dimension : dimensions) {
            getElement().getDimension().put(dimension.getKey(), dimension.getValue());
        }
        return this;
    }

    public ElementBuilder<E> withPropertyValue(String key, Object value) {
        return withPropertyValue(key, null, value);
    }

    public ElementBuilder<E> withPropertyValue(String key, String type, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setType(type);
        propertyValue.setValue(value);
        getElement().getPropertyValues().put(key, propertyValue);
        return this;
    }

    public ElementBuilder<E> withReference(String reference) {
        getElement().setReference(reference);
        return this;

    }
    protected abstract E getElement();

    public abstract E build();
}
