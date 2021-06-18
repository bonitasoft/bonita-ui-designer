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

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PageMigrationApplyerTest {

    @Mock
    private WidgetService widgetService;

    @Mock
    private FragmentService fragmentService;

    @Test
    public void should_migrate_a_page() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration("2.0", mockMigrationStep);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(List.of(migration), widgetService, fragmentService);

        Page page = PageBuilder.aPage().withId("myPage")
                .withDesignerVersion("1.0.1")
                .withPreviousDesignerVersion("1.0.0")
                .build();
        when(mockMigrationStep.migrate(page)).thenReturn(Optional.of(new MigrationStepReport(MigrationStatus.SUCCESS, "myPage")));

        // When
        migrationApplyer.migrate(page);

        // Then
        assertThat(page.getArtifactVersion()).isEqualTo("2.0");
        assertThat(page.getPreviousArtifactVersion()).isEqualTo("1.0.1");
    }

    @Test
    public void should_migrate_a_page_with_new_model_version() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);

        Migration<Page> migration = new Migration("2.1", mockMigrationStep);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);

        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.7.11").build();
        when(mockMigrationStep.migrate(page)).thenReturn(Optional.of(new MigrationStepReport(MigrationStatus.SUCCESS, "myPage")));

        migrationApplyer.migrate(page);

        Assert.assertEquals(page.getPreviousArtifactVersion(),"2.0");
        Assert.assertEquals(page.getArtifactVersion(),"2.1");
    }

    @Test
    public void should_not_modify_previous_model_version_when_no_migration_done() throws Exception {
        Migration<Page> migration = new Migration("2.0", mock(MigrationStep.class));
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("2.0").build();

        migrationApplyer.migrate(page);

        Assert.assertEquals(page.getPreviousArtifactVersion(),"2.0");
        Assert.assertEquals(page.getArtifactVersion(),"2.0");
    }

    @Test
    public void should_migrate_all_custom_widget_uses_in_page_when_page_migration_is_done() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration("2.0", mockMigrationStep);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(page)).thenReturn(Optional.of(new MigrationStepReport(MigrationStatus.SUCCESS, "myPage")));

        MigrationResult result =  migrationApplyer.migrate(page);
        Page migratedPage = (Page) result.getArtifact();

        verify(widgetService).migrateAllCustomWidgetUsedInPreviewable(migratedPage);
        Assert.assertEquals(migratedPage.getPreviousArtifactVersion(),"1.0.0");
        Assert.assertEquals(migratedPage.getArtifactVersion(),"2.0");
    }

    @Test
    public void should_migrate_a_page_and_generate_a_report_when_two_step_is_done_and_one_is_return_warning_status() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        MigrationStep mockMigrationStepWarning = mock(MigrationStep.class);
        Migration<Page> migration = new Migration("2.0", mockMigrationStep,mockMigrationStepWarning);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.1").withPreviousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(page)).thenReturn(Optional.empty());
        when(mockMigrationStepWarning.migrate(page)).thenReturn(Optional.of(new MigrationStepReport(MigrationStatus.WARNING, "myPage", "You can remove xxx assets if you don't use it")));

        MigrationResult result =  migrationApplyer.migrate(page);

        Page migratedPage = (Page) result.getArtifact();
        Assert.assertEquals(result.getFinalStatus(),MigrationStatus.WARNING);
        Assert.assertEquals(result.getMigrationStepReportList().size(),2);
        Assert.assertEquals(result.getMigrationStepReportListFilterByFinalStatus().size(),1);
        Assert.assertEquals(((MigrationStepReport) result.getMigrationStepReportListFilterByFinalStatus().get(0)).getMigrationStatus(),MigrationStatus.WARNING);

        Assert.assertEquals(migratedPage.getPreviousArtifactVersion(),"1.0.1");
        Assert.assertEquals(migratedPage.getArtifactVersion(),"2.0");
    }

    @Test
    public void should_return_an_report_with_error_when_error_occurs_during_migration_page() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration("2.0", mockMigrationStep);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.1").withPreviousDesignerVersion("1.0.0").build();
        when(mockMigrationStep.migrate(page)).thenThrow(new Exception());

        MigrationResult result =  migrationApplyer.migrate(page);

        Page migratedPage = (Page) result.getArtifact();
        Assert.assertEquals(migratedPage.getPreviousArtifactVersion(),"1.0.1");
        Assert.assertEquals(migratedPage.getArtifactVersion(),"2.0");
        MigrationStepReport report = (MigrationStepReport) result.getMigrationStepReportList().get(0);
        Assert.assertEquals(report.getMigrationStatus(),MigrationStatus.ERROR);
        Assert.assertEquals(report.getArtifactId(),"myPage");
    }

    @Test
    public void should_migrate_widgets_and_fragment_when_parent_page_are_migrated() throws Exception {
        MigrationStep mockMigrationStep = mock(MigrationStep.class);
        Migration<Page> migration = new Migration("2.0", mockMigrationStep);
        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(migration), widgetService, fragmentService);
        Page page = aPage().withPreviousArtifactVersion("1.0.0").withDesignerVersion("1.0.0").build();
        Mockito.when(mockMigrationStep.migrate(page)).thenReturn(Optional.empty());

        migrationApplyer.migrate(page);

        verify(fragmentService).migrateAllFragmentUsed(page);
        verify(widgetService).migrateAllCustomWidgetUsedInPreviewable(page);
        Assert.assertEquals(page.getPreviousArtifactVersion(), "1.0.0");
        Assert.assertEquals(page.getArtifactVersion(), "2.0");
    }

    @Test
    public void should_get_correct_migration_status_when_one_dependency_is_to_migrate() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(widgetService.getMigrationStatusOfCustomWidgetUsed(page)).thenReturn(new MigrationStatusReport(true, true));
        when(fragmentService.getMigrationStatusOfFragmentUsed(page)).thenReturn(new MigrationStatusReport(true, false));

        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(null), widgetService, fragmentService);
        MigrationStatusReport status = migrationApplyer.getMigrationStatusDependencies(page);
        Assert.assertEquals(getMigrationStatusReport(true, true), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_one_dependency_is_incompatible() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(widgetService.getMigrationStatusOfCustomWidgetUsed(page)).thenReturn(new MigrationStatusReport(false, false));
        when(fragmentService.getMigrationStatusOfFragmentUsed(page)).thenReturn(new MigrationStatusReport(true, true));

        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(null), widgetService, fragmentService);
        MigrationStatusReport status = migrationApplyer.getMigrationStatusDependencies(page);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependencies_are_correct() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(widgetService.getMigrationStatusOfCustomWidgetUsed(page)).thenReturn(new MigrationStatusReport(true, false));
        when(fragmentService.getMigrationStatusOfFragmentUsed(page)).thenReturn(new MigrationStatusReport(true, false));

        PageMigrationApplyer migrationApplyer = new PageMigrationApplyer(Collections.singletonList(null), widgetService, fragmentService);
        MigrationStatusReport status = migrationApplyer.getMigrationStatusDependencies(page);
        Assert.assertEquals(getMigrationStatusReport(true, false), status.toString());
    }

    private String getMigrationStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }
}
