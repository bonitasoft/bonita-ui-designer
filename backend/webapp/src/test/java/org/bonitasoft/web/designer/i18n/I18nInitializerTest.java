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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration("file:target/test-classes")
public class I18nInitializerTest {

    @Mock
    LanguagePackBuilder languagePackBuilder;

    @InjectMocks
    I18nInitializer i18nInitializer;

    @Test
    public void should_start_live_build_on_po_directory() throws Exception {

        i18nInitializer.contextInitialized();

        verify(languagePackBuilder).start(eq(Paths.get("target/test-classes/i18n").toAbsolutePath()));
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_a_runtime_exception_on_io_error() throws Exception {
        doThrow(new IOException()).when(languagePackBuilder).start(any(Path.class));

        i18nInitializer.contextInitialized();
    }
}
