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

import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.workspace.ResourcesCopier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class I18nInitializerTest {

    @Mock
    private WorkspaceUidProperties workspaceUidProperties;
    @Mock
    private ResourcesCopier resourcesCopier;
    @Mock
    private LanguagePackBuilder languagePackBuilder;

    @InjectMocks
    private I18nInitializer i18nInitializer;

    @Before
    public void setUp() throws Exception {
        when(workspaceUidProperties.getExtractPath()).thenReturn(Paths.get("target/test-classes"));
    }

    @Test
    public void should_start_live_build_on_po_directory() throws Exception {

        i18nInitializer.initialize();

        verify(languagePackBuilder).start(eq(workspaceUidProperties.getExtractPath().resolve("i18n")));
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_a_runtime_exception_on_io_error() throws Exception {
        doThrow(new IOException()).when(languagePackBuilder).start(any(Path.class));

        i18nInitializer.initialize();
    }
}
