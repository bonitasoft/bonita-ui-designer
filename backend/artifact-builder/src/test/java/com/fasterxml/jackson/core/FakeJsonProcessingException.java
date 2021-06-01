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
package com.fasterxml.jackson.core;

/**
 * Extends jackson JsonProcessingException for test purpose since JsonProcessingException constructor is protected
 */
public class FakeJsonProcessingException extends JsonProcessingException {

    public FakeJsonProcessingException(String msg, byte[] srcRef, int line, int column) {
        super(msg, new JsonLocation(srcRef, srcRef.length, line, column));
    }
}
