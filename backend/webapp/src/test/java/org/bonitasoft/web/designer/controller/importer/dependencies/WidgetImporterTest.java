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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetImporterTest {

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private WidgetFileBasedLoader widgetLoader;

    @InjectMocks
    private WidgetImporter widgetImporter;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_verify_that_a_widget_exists_in_repository() throws Exception {
        when(widgetRepository.exists("existingWidget")).thenReturn(true);

        boolean exists = widgetImporter.exists(aWidget().id("existingWidget").build());

        assertThat(exists).isTrue();
    }

    @Test
    public void should_verify_that_a_widget_does_not_exists_in_repository() throws Exception {
        when(widgetRepository.exists("unknownWidget")).thenReturn(false);

        boolean exists = widgetImporter.exists(aWidget().id("unknownWidget").build());

        assertThat(exists).isFalse();
    }

    @Test
    public void should_load_custom_widgets() throws Exception {
        File widgetsFolder = temporaryFolder.newFolder("widgets");

        widgetImporter.load(null, temporaryFolder.toPath());

        verify(widgetLoader).loadAllCustom(widgetsFolder.toPath());
    }
}
