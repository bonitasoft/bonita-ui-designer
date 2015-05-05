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
package org.bonitasoft.web.designer.servlet;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class WidgetDirectiveLoaderTest {

    private WidgetDirectiveLoader widgetDirectiveLoader;
    private Path widgetRepositoryPath;
    private MockHttpServletResponse response;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        widgetRepositoryPath = Paths.get("src/test/resources/widgets");
        widgetDirectiveLoader = new WidgetDirectiveLoader(widgetRepositoryPath);

        request = new MockHttpServletRequest(new MockServletContext());
        response = new MockHttpServletResponse();
    }

    @Test
    public void should_load_files_on_disk_according_to_asked_path() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        request.setPathInfo("/pbLabel/pbLabel.js");

        widgetDirectiveLoader.handleRequest(request, response);

        assertThat(response.getContentAsByteArray()).isEqualTo(Files.readAllBytes(expectedFile));
        assertThat(response.getHeader("Content-Length")).isEqualTo(String.valueOf(expectedFile.toFile().length()));
        assertThat(response.getHeader("Content-Disposition")).isEqualTo("inline; filename=\"pbLabel.js\"");
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.toString());
    }

    @Test
    public void should_respond_404_not_found_when_file_is_not_found() throws Exception {
        request.setPathInfo("/unexisting/file.js");

        widgetDirectiveLoader.handleRequest(request, response);

        assertThat(response.getStatus()).isEqualTo(404);
    }
}
