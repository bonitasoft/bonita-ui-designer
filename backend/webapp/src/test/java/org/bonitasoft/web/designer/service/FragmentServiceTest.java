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

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bonitasoft.web.designer.service.FragmentMigrationApplyer;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FragmentServiceTest {

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private FragmentMigrationApplyer fragmentMigrationApplyer;

    @Mock
    private FragmentIdVisitor fragmentIdVisitor;

    @InjectMocks
    private FragmentService fragmentService;

    @Before
    public void setUp() {
        fragmentService = new FragmentService(fragmentRepository, fragmentMigrationApplyer, fragmentIdVisitor);
        ReflectionTestUtils.setField(fragmentService, "modelVersion", "2.0");
    }

    @Test
    public void should_migrate_found_fragment_when_get_is_called() {
        Fragment fragment = aFragment().id("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().id("myFragment").withDesignerVersion("1.5.0").withPreviousDesignerVersion("1.0.0").build();
        MigrationResult<Fragment> mr = new MigrationResult(fragmentMigrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS,"myFragmentBis")));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,false));
        when(fragmentMigrationApplyer.migrate(fragment,true)).thenReturn(mr);
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(fragment,true);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_not_save_fragment_when_migration_is_not_done() {
        Fragment fragment = aFragment().id("myFragment").withModelVersion("2.0").withPreviousDesignerVersion("1.0.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,false));

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer,never()).migrate(fragment,true);
    }

    @Test
    public void should_not_save_fragment_when_migration_is_finish_in_error() {
        Fragment fragment = aFragment().id("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().id("myFragment").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        when(fragmentRepository.get("myFragment")).thenReturn(fragment);
        MigrationResult mr = new MigrationResult(fragmentMigrated, Arrays.asList(new MigrationStepReport(MigrationStatus.ERROR,"myFragmentBis")));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,true));
        when(fragmentMigrationApplyer.migrate(fragment,true)).thenReturn(mr);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(fragment,true);
        verify(fragmentRepository, never()).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_migrate_child_fragment_when_parent_fragment_is_migrate() {
        Fragment fragment = aFragment().id("myFragmentBis").withDesignerVersion("1.0.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment parentFragment = aFragment().id("myFragment").withDesignerVersion("1.0.0").with(fragment).withPreviousDesignerVersion("1.0.0").build();
        Fragment fragmentMigrated = aFragment().id("myFragmentBis").withDesignerVersion("2.0").withPreviousDesignerVersion("1.0.0").build();
        Fragment parentFragmentMigrated = aFragment().id("myFragment").withDesignerVersion("2.0").withPreviousDesignerVersion("1.0.0").with(fragmentMigrated).build();
        MigrationResult mr = new MigrationResult(fragmentMigrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS,"myFragmentBis")));
        MigrationResult parentMigrated = new MigrationResult(parentFragmentMigrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS,"myFragment")));
        when(fragmentRepository.get("myFragment")).thenReturn(parentFragment);
        lenient().        when(fragmentRepository.get("myFragmentBis")).thenReturn(fragment);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,true));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(parentFragment)).thenReturn(new MigrationStatusReport(true,true));
        when(fragmentMigrationApplyer.migrate(parentFragment, true)).thenReturn(parentMigrated);
        when(fragmentMigrationApplyer.migrate(fragment, false)).thenReturn(mr);
        Set<String> h = new HashSet<>(Arrays.asList("myFragmentBis"));
        when(fragmentRepository.getByIds(h)).thenReturn(Arrays.asList(fragment));
        when(fragmentIdVisitor.visit(parentFragment)).thenReturn(h);

        fragmentService.get("myFragment");

        verify(fragmentMigrationApplyer).migrate(parentFragment,true);
        verify(fragmentMigrationApplyer).migrate(fragment,false);
        verify(fragmentRepository).updateLastUpdateAndSave(parentFragmentMigrated);
        verify(fragmentRepository).updateLastUpdateAndSave(fragmentMigrated);
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_to_migrate() throws Exception {
        Fragment fragment = aFragment().id("fragment").withDesignerVersion("1.10.0").build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("fragment"));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,false));
        when(fragmentRepository.getByIds(ids)).thenReturn(Arrays.asList(fragment));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, true), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_compatible() throws Exception {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Fragment fragment1 = aFragment().id("fragment1").withDesignerVersion("1.10.0").build();
        Fragment fragment2 = aFragment().id("fragment2").withModelVersion("2.1").build(); //incompatible
        Set<String> ids = new HashSet<>(Arrays.asList("fragment1", "fragment2"));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment1)).thenReturn(new MigrationStatusReport(true,false));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment2)).thenReturn(new MigrationStatusReport(false,false));
        when(fragmentRepository.getByIds(ids)).thenReturn(Arrays.asList(fragment1, fragment2));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_fragment_contains_incompatible_fragment() throws Exception {
        Fragment fragment1 = aFragment().id("fragment1").withDesignerVersion("1.10.0").build();
        Fragment fragment2 = aFragment().id("fragment2").withModelVersion("2.1").build(); //incompatible
        Set<String> ids = new HashSet<>(Arrays.asList("fragment2"));
lenient().        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment1)).thenReturn(new MigrationStatusReport(true,false));
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment2)).thenReturn(new MigrationStatusReport(true,false));
        when(fragmentIdVisitor.visit(fragment1)).thenReturn(ids);
        when(fragmentRepository.getByIds(ids)).thenReturn(Arrays.asList(fragment2));

        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(fragment1);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_to_migrate() throws Exception {
        Fragment fragment = aFragment().id("fragment").withDesignerVersion("2.0").isMigration(false).build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("fragment"));
        when(fragmentRepository.getByIds(ids)).thenReturn(Arrays.asList(fragment));
        when(fragmentIdVisitor.visit(page)).thenReturn(ids);
        when(fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment)).thenReturn(new MigrationStatusReport(true,false));
        MigrationStatusReport status = fragmentService.getMigrationStatusOfFragmentUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, false), status.toString());
    }

    private String getMigrationStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }

}
