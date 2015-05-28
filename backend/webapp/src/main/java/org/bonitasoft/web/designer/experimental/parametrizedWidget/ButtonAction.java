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

import org.bonitasoft.web.designer.experimental.mapping.FormScope;

public enum ButtonAction {
    NONE("None"),
    SUBMIT_TASK("Submit task"),
    START_PROCESS("Start process"),
    POST("POST"),
    PUT("PUT"),
    ADD_TO_COLLECTION("Add to collection"),
    REMOVE_FROM_COLLECTION("Remove from collection");

    private String value;

    ButtonAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ButtonAction fromScope(FormScope scope) {
        switch (scope) {
            case TASK:
                return SUBMIT_TASK;
            case PROCESS:
                return START_PROCESS;
            default:
                throw new IllegalArgumentException("Unsupported form scope: " + scope);
        }
    }
}
