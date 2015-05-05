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
package org.bonitasoft.web.designer.controller.utils;

public enum MimeType {

    APPLICATION_ZIP("application/zip", "application/x-zip-compressed", "application/x-zip"),
    APPLICATION_OCTETSTREAM("application/octet-stream");

    private String[] alternatives;
    private String mimeType;

    private MimeType(String mimeType, String... alternativeMimeTypes) {
        this.mimeType = mimeType;
        this.alternatives = alternativeMimeTypes;
    }

    public boolean matches(String mimeType) {
        return matches(mimeType, this.mimeType) || matches(mimeType, alternatives);
    }

    private boolean matches(String mimeType, String... alternatives) {
        for (String type : alternatives) {
            if (type.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return mimeType;
    }
}
