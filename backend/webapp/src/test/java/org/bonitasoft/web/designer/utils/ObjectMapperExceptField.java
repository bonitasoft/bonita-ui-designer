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
package org.bonitasoft.web.designer.utils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

/**
 * Custom jackson mapper that do not serialize property passed in argument
 * <p>Example: if you do not want to serialize "template" property from widget object, create a new ObjectMapperExceptField(Widget.class, "template")</p>
 *
 * @author Colin Puy
 */
public class ObjectMapperExceptField extends ObjectMapper {

    public ObjectMapperExceptField(Class<?> targetClass, String property) {
        super();
        SimpleBeanPropertyFilter theFilter = SimpleBeanPropertyFilter.serializeAllExcept(property);
        FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter", theFilter);
        addMixInAnnotations(targetClass, PropertyFilterMixIn.class);
        setFilters(filters);
    }

    @JsonFilter("myFilter")
    class PropertyFilterMixIn {
    }
}
