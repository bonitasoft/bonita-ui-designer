/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import javax.servlet.http.HttpServletRequest;

import org.bonitasoft.web.designer.controller.preview.Previewer;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class PreviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private Previewer previewer;

    @Mock
    private PageRepository pageRepository;

    @InjectMocks
    private PreviewController previewController;

    @Before
    public void beforeEach() throws Exception {
        mockMvc = standaloneSetup(previewController).build();
    }

    @Test
    public void should_call_the_previewer() throws Exception {
        ResponseEntity<String> response = new ResponseEntity<String>("Everything ok", HttpStatus.OK);
        when(previewer.render(eq("my-page"), eq(pageRepository), any(HttpServletRequest.class))).thenReturn(response);

        mockMvc
                .perform(get("/preview/page/my-page"))
                .andExpect(status().isOk())
                .andExpect(content().string("Everything ok"))
                .andExpect(content().encoding("UTF-8"));

    }
}
