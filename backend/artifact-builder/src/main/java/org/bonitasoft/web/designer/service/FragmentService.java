package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.ArtifactInfo;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Previewable;

import java.util.List;
import java.util.Set;

public interface FragmentService extends ArtifactService<Fragment> {

    /**
     * Return all fragments but the fragment for the given id if not null
     *
     * @param elementId if null, return all fragments
     * @return a list of {@link Fragment}
     */
    List<Fragment> getAllNotUsingFragment(String elementId);

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
    MigrationResult<Fragment> migrate(Fragment fragment, boolean migrateChildren);

    List<MigrationStepReport> migrateAllFragmentUsed(Previewable previewable);

    MigrationStatusReport getMigrationStatusOfFragmentUsed(Previewable previewable);

    ArtifactInfo getInfo(String fragmentId);

    Fragment rename(Fragment fragment, String name) throws ModelException;

    Fragment create(Fragment fragment);

    Fragment save(String fragmentId, Fragment fragment) throws ModelException;

    void delete(String fragmentId);

    Set<Asset> listAsset(Fragment fragment);
}
