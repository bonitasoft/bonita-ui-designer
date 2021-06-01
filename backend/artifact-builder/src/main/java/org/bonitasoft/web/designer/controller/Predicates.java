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

import com.google.common.base.Predicate;

import java.util.Objects;

import static org.springframework.beans.BeanUtils.getPropertyDescriptor;

public class Predicates {

    private Predicates() {
        // Utility class
    }

    /**
     * Create a predicate which test if a property of both object are equal
     *
     * @param propertyName  of the property to be checked
     * @param propertyValue that the property should have
     * @param <T>           Predicate type
     * @return a predicate which is true if property value of both object are equal
     */
    public static <T> Predicate<T> propertyEqualTo(final String propertyName, final Object propertyValue) {

        return object -> {
            try {
                var propertyDescriptor = getPropertyDescriptor(object.getClass(), propertyName);
                return propertyDescriptor != null && Objects.equals(propertyValue, propertyDescriptor.getReadMethod().invoke(object));
            } catch (ReflectiveOperationException e) {
                return false;
            }
        };
    }
}
