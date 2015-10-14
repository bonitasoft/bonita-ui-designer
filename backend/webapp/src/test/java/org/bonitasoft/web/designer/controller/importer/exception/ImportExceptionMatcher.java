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
package org.bonitasoft.web.designer.controller.importer.exception;

import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportException.Type;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ImportExceptionMatcher extends TypeSafeMatcher<ImportException> {

    public static ImportExceptionMatcher hasType(Type type) {
        return new ImportExceptionMatcher(type);
    }

    private Type expectedtype;
    private Type foundType;

    private ImportExceptionMatcher(Type expectedtype) {
        this.expectedtype = expectedtype;
    }

    @Override
    protected boolean matchesSafely(final ImportException exception) {
        foundType = exception.getType();
        return expectedtype.equals(foundType);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected type ").appendValue(expectedtype).appendText(" but was ").appendValue(foundType);
    }
}
