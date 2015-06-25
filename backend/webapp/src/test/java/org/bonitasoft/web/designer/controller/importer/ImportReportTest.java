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
package org.bonitasoft.web.designer.controller.importer;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImportReportTest {

    @Mock
    private WidgetImporter widgetImporter;
    @Mock
    private AssetImporter assetImporter;

    @Before
    public void setUp() throws Exception {
        when(widgetImporter.getComponentName()).thenReturn("widget");
    }

    @Test
    public void should_create_a_new_report() throws Exception {
        Page page = PageBuilder.aPage().build();
        List<Identifiable> widgets = Arrays.<Identifiable>asList(
                aWidget().id("first").build(),
                aWidget().id("second").build());
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(widgetImporter, widgets);
        dependencies.put(assetImporter, asList(AssetBuilder.anAsset().build()));

        ImportReport report = ImportReport.from(page, dependencies);

        // asset dependencies should not be included
        assertThat(report.getElement()).isEqualTo(page);
        assertThat(report.getDependencies().size()).isEqualTo(1);
        assertThat(report.getDependencies().get("widget")).isEqualTo(widgets);
    }
}
