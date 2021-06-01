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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WidgetDependencyImporterTest {

    @Mock
    private WidgetRepository widgetRepository;

    @InjectMocks
    private WidgetDependencyImporter widgetDependencyImporter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_verify_that_a_widget_exists_in_repository() throws Exception {
        when(widgetRepository.exists("existingWidget")).thenReturn(true);

        boolean exists = widgetDependencyImporter.exists(aWidget().withId("existingWidget").build());

        assertThat(exists).isTrue();
    }

    @Test
    public void should_verify_that_a_widget_does_not_exists_in_repository() throws Exception {
        when(widgetRepository.exists("unknownWidget")).thenReturn(false);

        boolean exists = widgetDependencyImporter.exists(aWidget().withId("unknownWidget").build());

        assertThat(exists).isFalse();
    }

    @Test
    public void should_load_custom_widgets() throws Exception {
        File widgetsFolder = temporaryFolder.newFolder("widgets");

        widgetDependencyImporter.load(null, temporaryFolder.toPath());

        ArgumentCaptor<DirectoryStream.Filter<Path>> captor = ArgumentCaptor.forClass(DirectoryStream.Filter.class);

        verify(widgetRepository).loadAll(eq(widgetsFolder.toPath()),captor.capture());
        assertThat(captor.getValue()).isEqualTo(WidgetDependencyImporter.CUSTOM_WIDGET_FILTER);
    }
}
