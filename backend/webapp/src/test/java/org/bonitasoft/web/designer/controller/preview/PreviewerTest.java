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
package org.bonitasoft.web.designer.controller.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

import junit.framework.TestCase;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class PreviewerTest extends TestCase {

    @Mock
    private PageRepository pageRepository;

    @Mock
    private HtmlGenerator generator;

    @InjectMocks
    private Previewer previewer;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private Page page = aPage().withId("a-page").build();

    @Test
    public void should_generate_html_and_print_it_in_response() throws Exception {
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(generator.generateHtml("/runtime/", page)).thenReturn("foobar");

        ResponseEntity<String> response = previewer.render(page.getId(), pageRepository, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("foobar");
    }

    @Test
    public void should_return_error_response_when_error_occur_on_generation() throws Exception {
        when(pageRepository.get(page.getId())).thenReturn(page);
        when(generator.generateHtml("/runtime/", page)).thenThrow(new GenerationException("error", new Exception()));

        ResponseEntity<String> response = previewer.render(page.getId(), pageRepository, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error during page generation");
    }

    @Test
    public void should_return_error_response_when_page_is_not_found() throws Exception {
        when(pageRepository.get("unexisting-page")).thenThrow(new NotFoundException("page not found"));

        ResponseEntity<String> response = previewer.render("unexisting-page", pageRepository, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Page <unexisting-page> not found");
    }
}
