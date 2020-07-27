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
package org.bonitasoft.web.designer.service;

import static java.util.Collections.singletonList;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.controller.MigrationResource;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetServiceTest {

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private BondsTypesFixer bondsTypesFixer;

    @Mock
    private WidgetMigrationApplyer widgetMigrationApplyer;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    @InjectMocks
    private WidgetService widgetService;

    @Before
    public void setUp() throws Exception {
        widgetService = new WidgetService(widgetRepository, singletonList(bondsTypesFixer), widgetMigrationApplyer, widgetIdVisitor);
    }

    @Test
    public void should_fix_bonds_types_on_save() {
        Property constantTextProperty = aProperty().name("text").bond(CONSTANT).build();
        Property interpolationTextProperty = aProperty().name("text").bond(INTERPOLATION).build();
        Widget persistedWidget = aWidget().id("labelWidget").property(constantTextProperty).build();
        when(widgetRepository.get("labelWidget")).thenReturn(persistedWidget);

        widgetService.updateProperty("labelWidget", "text", interpolationTextProperty);

        verify(bondsTypesFixer).fixBondsTypes("labelWidget", singletonList(interpolationTextProperty));
    }

    @Test
    public void should_migrate_found_widget_when_get_is_called() {
        Widget widget = aWidget().id("widget").designerVersion("1.0.0").build();
        Widget widgetMigrated = aWidget().id("widget").modelVersion("2.0").previousArtifactVersion("1.0.0").build();
        when(widgetRepository.get("widget")).thenReturn(widget);
        MigrationResult<Widget> mr = new MigrationResult(widgetMigrated,  Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository).updateLastUpdateAndSave(mr.getArtifact());
    }

    @Test
    public void should_not_update_and_save_widget_if_no_migration_done() {
        Widget widget = aWidget().id("widget").modelVersion("2.0").build();
        Widget widgetMigrated = aWidget().id("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult mr = new MigrationResult(widget, Arrays.asList(any(MigrationStepReport.class)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }


    @Test
    public void should_migrate_all_custom_widget() throws Exception {
        Widget widget1 = aWidget().id("widget1").designerVersion("1.0.0").build();
        Widget widget2 = aWidget().id("widget2").designerVersion("1.0.0").build();
        Widget widget1Migrated = aWidget().id("widget1").designerVersion("2.0").build();
        Widget widget2Migrated = aWidget().id("widget2").designerVersion("2.0").build();
        when(widgetRepository.get("widget1")).thenReturn(widget1);
        when(widgetRepository.get("widget2")).thenReturn(widget2);
        when(widgetMigrationApplyer.migrate(widget1)).thenReturn(new MigrationResult(widget1Migrated,  Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS,"widget1" ))));
        when(widgetMigrationApplyer.migrate(widget2)).thenReturn(new MigrationResult(widget2Migrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS,"widget2" ))));

        Set<String> h = new HashSet<>(Arrays.asList("widget1", "widget1"));
        when(widgetRepository.getByIds(h)).thenReturn(Arrays.asList(widget1, widget2));
        Page page = aPage().with(
                aComponent("widget1"),
                aComponent("widget2"))
                .build();

        when(widgetIdVisitor.visit(page)).thenReturn(h);

        widgetService.migrateAllCustomWidgetUsedInPreviewable(page);

        verify(widgetMigrationApplyer).migrate(widget1);
        verify(widgetMigrationApplyer).migrate(widget2);
    }

    @Test
    public void should_not_update_and_save_widget_if_migration_finish_on_error() {
        Widget widget = aWidget().id("widget").modelVersion("2.0").build();
        Widget widgetMigrated = aWidget().id("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult mr = new MigrationResult(widget, Arrays.asList(Arrays.asList(new MigrationStepReport(MigrationStatus.ERROR))));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_to_migrate() throws Exception {
        Widget widget = aWidget().id("widget").designerVersion("1.10.0").build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, true), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_compatible() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Widget widget1 = aWidget().id("widget1").designerVersion("1.10.0").build();
        Widget widget2 = aWidget().id("widget2").modelVersion("2.1").isCompatible(false).isMigration(false).build(); //incompatible
        Set<String> ids = new HashSet<>(Arrays.asList("widget1", "widget2"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget1, widget2));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_to_migrate() throws Exception {
        Widget widget = aWidget().id("widget").designerVersion("2.0").isCompatible(true).isMigration(false).build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, false), status.toString());
    }

    private String getMigrationStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }

}
