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

import com.google.common.collect.ImmutableSortedMap;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;

public class WidgetContainer extends AbstractParametrizedWidget implements ParametrizedWidget {

    private String repeatedCollection;

    public String getRepeatedCollection() {
        return repeatedCollection;
    }

    public void setRepeatedCollection(String repeatedCollection) {
        this.repeatedCollection = repeatedCollection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Element> T getAdapter(Class<T> adapterClass) {
        if (Container.class.equals(adapterClass)) {
            Container container = new Container();
            container.setDimension(ImmutableSortedMap.of("xs", getDimension()));
            container.setPropertyValues(toPropertyValues());
            return (T) container;
        }
        return null;
    }
}
