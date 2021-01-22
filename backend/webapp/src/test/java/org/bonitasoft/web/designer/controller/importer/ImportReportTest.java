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
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImportReportTest {

    @Mock
    private WidgetImporter widgetImporter;
    @Mock
    private FragmentImporter fragmentImporter;
    @Mock
    private AssetImporter assetImporter;

    @Before
    public void setUp() throws Exception {
        when(widgetImporter.getComponentName()).thenReturn("widget");
        when(fragmentImporter.getComponentName()).thenReturn("fragment");
    }

    private Widget existing(Widget widget) {
        when(widgetImporter.getOriginalElementFromRepository(widget)).thenReturn(widget);
        when(widgetImporter.exists(widget)).thenReturn(true);
        return widget;
    }

    @Test
    public void should_report_imported_element_when_it_is_a_page() throws Exception {
        Page importedPage = aPage().build();

        ImportReport report = ImportReport.from(importedPage, new HashMap());

        assertThat(report.getElement()).isEqualTo(importedPage);
    }

    @Test
    public void should_report_imported_element_when_it_is_a_widget() throws Exception {
        Widget importedWidget = aWidget().build();

        ImportReport report = ImportReport.from(importedWidget, new HashMap());

        assertThat(report.getElement()).isEqualTo(importedWidget);
    }

    @Test
    public void should_include_added_and_overriden_widget_in_imported_dependencies() throws Exception {
        Widget existingWidget = existing(aWidget().id("existing").build());
        Widget newWidget = aWidget().id("newOne").build();
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(widgetImporter, asList(newWidget, existingWidget));

        ImportReport report = ImportReport.from(aPage().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("widget")).containsOnly(newWidget);
        assertThat(report.getDependencies().getOverwritten().get("widget")).containsOnly(existingWidget);
    }

    @Test
    public void should_not_include_assets_in_imported_dependencies() throws Exception {
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(assetImporter, asList(anAsset().build()));

        ImportReport report = ImportReport.from(aPage().build(), dependencies);

        assertThat(report.getDependencies().getAdded()).isNull();
        assertThat(report.getDependencies().getOverwritten()).isNull();
    }

    private Fragment mockExistsInRepository(Fragment fragment) {
        when(fragmentImporter.exists(fragment)).thenReturn(true);
        when(fragmentImporter.getOriginalElementFromRepository(fragment)).thenReturn(fragment);
        return fragment;
    }

    private Widget mockExistsInRepository(Widget widget) {
        when(widgetImporter.exists(widget)).thenReturn(true);
        when(widgetImporter.getOriginalElementFromRepository(widget)).thenReturn(widget);
        return widget;
    }

    @Test
    public void should_report_imported_element_when_it_is_a_fragment() throws Exception {
        Fragment importedFragment = aFragment().build();

        ImportReport report = ImportReport.from(importedFragment, new HashMap());

        assertThat(report.getElement()).isEqualTo(importedFragment);
    }

    @Test
    public void should_include_added_and_overwritten_widget_in_imported_dependencies() throws Exception {
        Widget newWidget = aWidget().id("newOne").build();
        Widget existingWidget = mockExistsInRepository(aWidget().id("existing").build());
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(widgetImporter, asList(newWidget, existingWidget));

        ImportReport report = ImportReport.from(aFragment().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("widget")).containsOnly(newWidget);
        assertThat(report.getDependencies().getOverwritten().get("widget")).containsOnly(existingWidget);
    }

    @Test
    public void should_include_added_and_overwritten_fragments_in_imported_dependencies() throws Exception {
        Fragment newFragment = aFragment().id("newOne").build();
        Fragment existingFragment = mockExistsInRepository(aFragment().id("existing").build());
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(fragmentImporter, asList(newFragment, existingFragment));

        ImportReport report = ImportReport.from(aFragment().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("fragment")).containsOnly(newFragment);
        assertThat(report.getDependencies().getOverwritten().get("fragment")).containsOnly(existingFragment);
    }
}
