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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpFileTest {

    @Test
    public void should_throw_NullPointerException_when_filename_is_null(){
        assertThrows(NullPointerException.class, () -> HttpFile.getOriginalFilename(null));
    }

    @Test
    public void should_find_filename_in_full_linux_path(){
        assertThat(HttpFile.getOriginalFilename("/tmp/test/myfile.js")).isEqualTo("myfile.js");
    }

    @Test
    public void should_find_filename_in_full_windows_path(){
        assertThat(HttpFile.getOriginalFilename("C:\\Users\\frontend\\myfile.js")).isEqualTo("myfile.js");
    }

    @Test
    public void should_find_filename_when_no_full_path(){
        assertThat(HttpFile.getOriginalFilename("myfile.js")).isEqualTo("myfile.js");
    }
}
