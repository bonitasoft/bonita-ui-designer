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

import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.DependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.FragmentDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.dependencies.WidgetDependencyImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportReportTest {

    @Mock
    private WidgetDependencyImporter widgetDependencyImporter;
    @Mock
    private FragmentDependencyImporter fragmentDependencyImporter;
    @Mock
    private AssetDependencyImporter assetDependencyImporter;

    @Before
    public void setUp() throws Exception {
        when(widgetDependencyImporter.getComponentName()).thenReturn("widget");
        when(fragmentDependencyImporter.getComponentName()).thenReturn("fragment");
    }

    private Widget existing(Widget widget) {
        when(widgetDependencyImporter.getOriginalElementFromRepository(widget)).thenReturn(widget);
        when(widgetDependencyImporter.exists(widget)).thenReturn(true);
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
        Widget existingWidget = existing(aWidget().withId("existing").build());
        Widget newWidget = aWidget().withId("newOne").build();
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(widgetDependencyImporter, asList(newWidget, existingWidget));

        ImportReport report = ImportReport.from(aPage().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("widget")).containsOnly(newWidget);
        assertThat(report.getDependencies().getOverwritten().get("widget")).containsOnly(existingWidget);
    }

    @Test
    public void should_not_include_assets_in_imported_dependencies() throws Exception {
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(assetDependencyImporter, asList(anAsset().build()));

        ImportReport report = ImportReport.from(aPage().build(), dependencies);

        assertThat(report.getDependencies().getAdded()).isNull();
        assertThat(report.getDependencies().getOverwritten()).isNull();
    }

    private Fragment mockExistsInRepository(Fragment fragment) {
        when(fragmentDependencyImporter.exists(fragment)).thenReturn(true);
        when(fragmentDependencyImporter.getOriginalElementFromRepository(fragment)).thenReturn(fragment);
        return fragment;
    }

    private Widget mockExistsInRepository(Widget widget) {
        when(widgetDependencyImporter.exists(widget)).thenReturn(true);
        when(widgetDependencyImporter.getOriginalElementFromRepository(widget)).thenReturn(widget);
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
        Widget newWidget = aWidget().withId("newOne").build();
        Widget existingWidget = mockExistsInRepository(aWidget().withId("existing").build());
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(widgetDependencyImporter, asList(newWidget, existingWidget));

        ImportReport report = ImportReport.from(aFragment().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("widget")).containsOnly(newWidget);
        assertThat(report.getDependencies().getOverwritten().get("widget")).containsOnly(existingWidget);
    }

    @Test
    public void should_include_added_and_overwritten_fragments_in_imported_dependencies() throws Exception {
        Fragment newFragment = aFragment().withId("newOne").build();
        Fragment existingFragment = mockExistsInRepository(aFragment().withId("existing").build());
        Map<DependencyImporter, List<?>> dependencies = new HashMap<>();
        dependencies.put(fragmentDependencyImporter, asList(newFragment, existingFragment));

        ImportReport report = ImportReport.from(aFragment().build(), dependencies);

        assertThat(report.getDependencies().getAdded().get("fragment")).containsOnly(newFragment);
        assertThat(report.getDependencies().getOverwritten().get("fragment")).containsOnly(existingFragment);
    }
}
