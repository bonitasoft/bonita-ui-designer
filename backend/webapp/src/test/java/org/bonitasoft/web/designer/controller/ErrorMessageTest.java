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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorMessageTest {

    @Test
    public void should_set_exception_message_as_message() throws Exception {
        Exception exception = new Exception("an error message");

        ErrorMessage errorMessage = new ErrorMessage(exception);

        assertThat(errorMessage.getMessage()).isEqualTo("an error message");
    }

    @Test
    public void should_set_exception_type_as_type() throws Exception {
        Exception illegalArgumentException = new IllegalArgumentException("an error message");

        ErrorMessage errorMessage = new ErrorMessage(illegalArgumentException);

        assertThat(errorMessage.getType()).isEqualTo("IllegalArgumentException");
    }
}
