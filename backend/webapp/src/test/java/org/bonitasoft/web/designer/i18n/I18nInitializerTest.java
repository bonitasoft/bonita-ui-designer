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
package org.bonitasoft.web.designer.i18n;

import java.io.IOException;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class I18nInitializerTest {

    @Inject
    private I18nInitializer i18nInitializer;

    @MockBean
    private LanguagePackBuilder languagePackBuilder;

    @SpyBean
    private WorkspacePathResolver workspacePathResolver;

    private String tempI18Dir;

    @Before
    public void setUp() throws Exception {
        tempI18Dir = "target/test-classes/i18n";

        when(workspacePathResolver.getTmpI18nRepositoryPath()).thenReturn(Paths.get(tempI18Dir));
    }

    @Test
    public void should_start_live_build_on_po_directory() throws Exception {
        i18nInitializer.contextInitialized();

        verify(languagePackBuilder).start(eq(Paths.get(tempI18Dir).toAbsolutePath()));
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_a_runtime_exception_on_io_error() throws Exception {
        doThrow(IOException.class).when(languagePackBuilder).start(any());

        i18nInitializer.contextInitialized();
    }
}
