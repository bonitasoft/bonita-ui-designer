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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;


@Named
public class FragmentService extends AbstractArtifactService {

    private final FragmentMigrationApplyer fragmentMigrationApplyer;
    private final FragmentIdVisitor fragmentIdVisitor;
    private FragmentRepository fragmentRepository;

    @Inject
    public FragmentService(FragmentRepository fragmentRepository, FragmentMigrationApplyer fragmentMigrationApplyer, FragmentIdVisitor fragmentIdVisitor, UiDesignerProperties uiDesignerProperties) {
        super(uiDesignerProperties);
        this.fragmentRepository = fragmentRepository;
        this.fragmentMigrationApplyer = fragmentMigrationApplyer;
        this.fragmentIdVisitor = fragmentIdVisitor;
    }


    @Override
    public Fragment get(String id) {
        Fragment fragment = this.fragmentRepository.get(id);
        return migrate(fragment);
    }

    @Override
    public Fragment migrate(Identifiable artifact) {
        MigrationResult<Fragment> result = migrate(artifact, true);
        return result.getArtifact();
    }

    @Override
    public MigrationResult migrateWithReport(Identifiable artifact) {
        return migrate(artifact, true);
    }

    @Override
    public MigrationStatusReport getStatus(Identifiable identifiable) {
        MigrationStatusReport fragmentStatusReport = super.getStatus(identifiable);
        MigrationStatusReport depWidgetReport = fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed((Fragment) identifiable);
        MigrationStatusReport depFragmentReport = getMigrationStatusOfFragmentUsed((Fragment) identifiable);
        return mergeStatusReport(fragmentStatusReport, mergeStatusReport(depWidgetReport, depFragmentReport));
    }

    /**
     * Migrate a fragment. Most of the time, we would not migrate the fragments and the widgets used in the current
     * fragment. (As this is done at the page level, so 'migrateChildren' will be false)
     * But in case of a migration triggered by 'open fragment in editor', it will be required to migrate the fragments
     * and the widgets used in the current fragment. (so 'migrateChildren' will be true)
     *
     * @param fragment        The fragment to migrate.
     * @param migrateChildren A boolean to indicate if we need to trigger migration of the widgets and fragments used
     *                        in the current fragment.
     * @return Returns the migrated Fragment.
     */
    public MigrationResult migrate(Identifiable fragment, boolean migrateChildren) {
        Fragment fragmentToMigrate = (Fragment)fragment;
        fragmentToMigrate.setStatus(getStatus(fragment));

        if(!fragmentToMigrate.getStatus().isMigration()){
            return new MigrationResult<>(fragmentToMigrate, Collections.emptyList());
        }

        MigrationResult<Fragment> migratedResult = fragmentMigrationApplyer.migrate((Fragment) fragment, migrateChildren);
        Fragment fragmentMigrated = migratedResult.getArtifact();
        if (!migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            fragmentRepository.updateLastUpdateAndSave(fragmentMigrated);
        }
        if (migrateChildren) {
            migratedResult.getMigrationStepReportList().addAll(migrateAllFragmentUsed((Previewable) fragment));
        }
        return migratedResult;
    }

    public List<MigrationStepReport> migrateAllFragmentUsed(Previewable previewable) {
        List<MigrationStepReport> report = new ArrayList<>();
        fragmentRepository.getByIds(fragmentIdVisitor.visit(previewable))
                .stream()
                .forEach(p -> {
                    MigrationResult migratedResult = migrate(p, false);
                    report.addAll(migratedResult.getMigrationStepReportListFilterByFinalStatus());
                });
        return report;
    }

    public MigrationStatusReport getMigrationStatusOfFragmentUsed(Previewable previewable) {
        List<MigrationStatusReport> reports = new ArrayList<>();
        fragmentRepository.getByIds(fragmentIdVisitor.visit(previewable))
                .stream()
                .forEach(fragment -> {
                    reports.add(getStatus(fragment));
                });

        boolean migration = false;
        for (MigrationStatusReport report : reports) {
            if (!report.isCompatible()) {
                return report;
            }
            if (!migration && report.isMigration()) {
                migration = true;
            }
        }
        return new MigrationStatusReport(true, migration);
    }
}
