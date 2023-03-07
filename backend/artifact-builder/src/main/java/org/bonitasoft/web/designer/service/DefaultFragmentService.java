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


import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.Predicates;
import org.bonitasoft.web.designer.controller.asset.PageAssetPredicate;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.*;
import org.bonitasoft.web.designer.repository.AbstractRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.visitor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.bonitasoft.web.designer.repository.exception.NotAllowedException.checkNotAllowed;
import static org.springframework.util.StringUtils.hasText;


public class DefaultFragmentService extends AbstractArtifactService<FragmentRepository, Fragment> implements FragmentService {

    private final FragmentMigrationApplyer fragmentMigrationApplyer;

    private final FragmentIdVisitor fragmentIdVisitor;

    private final FragmentChangeVisitor fragmentChangeVisitor;

    private final PageHasValidationErrorVisitor pageHasValidationErrorVisitor;

    private final AssetVisitor assetVisitor;

    private final PageRepository pageRepository;

    private final List<Repository<?>> usedByRepositories;
    private WebResourcesVisitor webResourcesVisitor;

    public DefaultFragmentService(FragmentRepository fragmentRepository, PageRepository pageRepository,
                                  FragmentMigrationApplyer fragmentMigrationApplyer,
                                  FragmentIdVisitor fragmentIdVisitor, FragmentChangeVisitor fragmentChangeVisitor, PageHasValidationErrorVisitor pageHasValidationErrorVisitor,
                                  AssetVisitor assetVisitor,
                                  UiDesignerProperties uiDesignerProperties, WebResourcesVisitor webResourcesVisitor) {
        super(uiDesignerProperties, fragmentRepository);
        this.pageRepository = pageRepository;
        this.fragmentMigrationApplyer = fragmentMigrationApplyer;
        this.fragmentIdVisitor = fragmentIdVisitor;
        this.fragmentChangeVisitor = fragmentChangeVisitor;
        this.pageHasValidationErrorVisitor = pageHasValidationErrorVisitor;
        this.assetVisitor = assetVisitor;
        // fragmentsUsedBy Repositories
        this.usedByRepositories = asList(pageRepository, repository);
        this.webResourcesVisitor = webResourcesVisitor;
    }


    @Override
    public Fragment get(String id) {
        var fragment = repository.get(id);
        return migrate(fragment);
    }

    @Override
    public List<Fragment> getAllNotUsingFragment(String elementId) {
        List<Fragment> fragments;
        if (hasText(elementId)) {
            fragments = repository.getAllNotUsingElement(elementId);
        } else {
            fragments = repository.getAll();
        }
        fragments = fragments.stream().map(f -> {
            f.setStatus(this.getStatus(f));
            return f;
        }).collect(toList());

        fillWithUsedBy(fragments);

        return fragments;
    }

    @Override
    public Fragment getWithAsset(String id) {
        var fragment = repository.get(id);
        fragment = migrate(fragment);
        fragment.setAssets(assetVisitor.visit(fragment));
        return fragment;
    }

    @Override
    public Fragment migrate(Fragment fragment) {
        var result = migrate(fragment, true);
        return result.getArtifact();
    }

    @Override
    public MigrationResult<Fragment> migrateWithReport(Fragment fragment) {
        return migrate(fragment, true);
    }

    @Override
    public MigrationStatusReport getStatus(Fragment fragment) {
        var fragmentStatusReport = super.getStatus(fragment);
        var depWidgetReport = fragmentMigrationApplyer.getMigrationStatusOfCustomWidgetsUsed(fragment);
        var depFragmentReport = getMigrationStatusOfFragmentUsed(fragment);
        return mergeStatusReport(fragmentStatusReport, mergeStatusReport(depWidgetReport, depFragmentReport));
    }

    @Override
    public MigrationResult<Fragment> migrate(Fragment fragmentToMigrate, boolean migrateChildren) {
        fragmentToMigrate.setStatus(getStatus(fragmentToMigrate));

        if (!fragmentToMigrate.getStatus().isMigration()) {
            return new MigrationResult<>(fragmentToMigrate, Collections.emptyList());
        }

        var migratedResult = fragmentMigrationApplyer.migrate(fragmentToMigrate, migrateChildren);
        var fragmentMigrated = migratedResult.getArtifact();
        if (!migratedResult.getFinalStatus().equals(MigrationStatus.ERROR)) {
            repository.updateLastUpdateAndSave(fragmentMigrated);
        }
        if (migrateChildren) {
            migratedResult.getMigrationStepReportList().addAll(migrateAllFragmentUsed(fragmentToMigrate));
        }
        return migratedResult;
    }

    @Override
    public List<MigrationStepReport> migrateAllFragmentUsed(Previewable previewable) {
        List<MigrationStepReport> report = new ArrayList<>();
        repository.getByIds(fragmentIdVisitor.visit(previewable))
                .forEach(p -> {
                    var migratedResult = migrate(p, false);
                    report.addAll(migratedResult.getMigrationStepReportListFilterByFinalStatus());
                });
        return report;
    }

    @Override
    public MigrationStatusReport getMigrationStatusOfFragmentUsed(Previewable previewable) {
        List<MigrationStatusReport> reports = new ArrayList<>();
        repository.getByIds(fragmentIdVisitor.visit(previewable))
                .forEach(fragment -> reports.add(getStatus(fragment)));

        var migration = false;
        for (var report : reports) {
            if (!report.isCompatible()) {
                return report;
            }
            if (!migration && report.isMigration()) {
                migration = true;
            }
        }
        return new MigrationStatusReport(true, migration);
    }

    @Override
    public Fragment rename(Fragment fragment, String name) throws ModelException {
        checkIfFragmentIsCompatible(fragment);

        var fragmentId = fragment.getId();
        var newFragmentId = repository.getNextAvailableId(name);

        updateReferencesParentArtifacts(fragment, newFragmentId, fragment.getHasValidationError());

        fragment.setId(newFragmentId);
        fragment.setName(name);

        checkNameIsUnique(
                // make sure we don't check against the fragment itself
                repository.getAll().stream().filter(Predicates.propertyEqualTo("id", fragmentId).negate()).collect(toList()),
                fragment);

        var savedFragment = repository.updateLastUpdateAndSave(fragment);
        repository.delete(fragmentId);

        return savedFragment;
    }

    @Override
    public Fragment create(Fragment fragment) {
        checkNameIsUnique(repository.getAll(), fragment);

        fragment.setId(repository.getNextAvailableId(fragment.getName()));
        fragment.setAssets(fragment.getAssets().stream().filter(new PageAssetPredicate()).collect(toSet()));
        return repository.updateLastUpdateAndSave(fragment);
    }

    @Override
    public Fragment save(String fragmentId, Fragment fragment) throws ModelException {
        checkNameIsUnique(
                // make sure we don't check against the fragment itself
                repository.getAll().stream().filter(
                        Predicates.propertyEqualTo("id", fragment.getId()).negate()
                ).collect(toList()),
                fragment);

        String newFragmentId;
        try {
            var currentFragment = this.get(fragmentId);
            checkIfFragmentIsCompatible(currentFragment);

            var nameChanged = !currentFragment.getName().equals(fragment.getName());
            var validationErrorStateChanged = currentFragment.getHasValidationError() != fragment.getHasValidationError();
            if (validationErrorStateChanged || nameChanged) {
                if (nameChanged) {
                    newFragmentId = repository.getNextAvailableId(fragment.getName());
                } else {
                    newFragmentId = fragmentId;
                }
                updateReferencesParentArtifacts(currentFragment, newFragmentId, fragment.getHasValidationError());
            } else {
                newFragmentId = fragmentId;
            }

        } catch (NotFoundException e) {
            newFragmentId = repository.getNextAvailableId(fragment.getName());
        }

        fragment.setId(newFragmentId);
        fragment.setAssets(fragment.getAssets().stream().filter(new PageAssetPredicate()).collect(toSet()));

        var savedFragment = repository.updateLastUpdateAndSave(fragment);
        if (!fragmentId.equals(savedFragment.getId())) {
            // Rename link to Fragment in other artifact
            repository.delete(fragmentId);
        }
        return savedFragment;
    }

    @Override
    public void delete(String fragmentId) {
        var fragment = new Fragment();
        fragment.setId(fragmentId);
        fillWithUsedBy(fragment);

        //if this fragment is used elsewhere we prevent the deletion. A fragment can be used in a page
        //or in an another fragment
        if (fragment.isUsed()) {
            throw new InUseException(buildErrorMessage(fragment));
        }
        repository.delete(fragmentId);
    }


    private String buildErrorMessage(Fragment fragment) {
        //if an error occurred it's useful for user to know which components use this widget
        var msg = new StringBuilder("The fragment cannot be deleted because it is used in");
        //loop on component (page, fragment,...) using this fragment
        fragment.getUsedBy().forEach((key, elements) -> {
            //loop on elements using this fragment
            if (!elements.isEmpty()) {
                msg.append(" ").append(elements.size())
                        .append(" ")
                        .append(key).append(elements.size() > 1 ? "s" : "")
                        .append(" ")
                        .append(
                                elements.stream().map(elem -> "<" + elem.getName() + ">").collect(joining(", "))
                        );
            }
        });
        return msg.toString();
    }

    private void checkNameIsUnique(Collection<Fragment> fragments, Fragment fragment) {
        checkNotAllowed(
                fragments.stream().anyMatch(Predicates.propertyEqualTo("name", fragment.getName())),
                format("A fragment with name %s already exists", fragment.getName())
        );
    }

    private void checkIfFragmentIsCompatible(Fragment fragment) throws ModelException {
        if (fragment.getStatus() != null && !fragment.getStatus().isCompatible()) {
            var message = format("Fragment %s is in an incompatible version. Newer UI Designer version is required.", fragment.getId());
            throw new ModelException(message);
        }
    }

    private void updateReferencesParentArtifacts(Fragment currentFragment, String newFragmentId, boolean newHasValidationError) {
        fillWithUsedBy(currentFragment);
        if (currentFragment.getUsedBy() == null) {
            return;
        }
        var oldFragmentId = currentFragment.getId();

        for (Map.Entry<String, List<Identifiable>> entry : currentFragment.getUsedBy().entrySet()) {
            var identifiables = entry.getValue();

            var pages = identifiables
                    .stream()
                    .filter(Page.class::isInstance)
                    .collect(toList());
            updateReference(pages, newFragmentId, oldFragmentId, pageRepository, newHasValidationError);

            var fragments = identifiables
                    .stream()
                    .filter(identifiable -> identifiable.getType().equals("fragment"))
                    .collect(toList());

            updateReference(fragments, newFragmentId, oldFragmentId, repository, newHasValidationError);
        }
    }

    private <T extends AbstractPage> void updateReference(List<? extends Identifiable> artifacts, String newFragmentId, String oldFragmentId, AbstractRepository repo, boolean newHasValidationError) {

        fragmentChangeVisitor.setNewFragmentId(newFragmentId);
        fragmentChangeVisitor.setFragmentToReplace(oldFragmentId);

        // set the has valid error
        artifacts.forEach(identifiable -> {
            var artifact = (T) repo.get(identifiable.getId());

            if (newHasValidationError != artifact.getHasValidationError()) {
                updateArtifactValidationError(artifact, newHasValidationError, newFragmentId);
                if (artifact.getClass().equals(Fragment.class)) {
                    updateReferencesParentArtifacts((Fragment) artifact, artifact.getId(), newHasValidationError);
                }
            }

            // Traverse tree to detect fragment
            List<List<Element>> rows = artifact.getRows();
            fragmentChangeVisitor.visitRows(rows);

            repo.updateLastUpdateAndSave(artifact);
        });
    }

    private void updateArtifactValidationError(AbstractPage artifact, boolean newHasValidationError, String newFragmentId) {
        if (newHasValidationError) {
            artifact.setHasValidationError(true);
        } else if (!containsValidationError(artifact, newFragmentId)) {
            artifact.setHasValidationError(false);
        }
    }

    private boolean containsValidationError(AbstractPage page, String newFragmentId) {
        var hasValidationError = false;
        for (var row : page.getRows()) {
            for (var element : row) {
                if (element.getClass().equals(FragmentElement.class)) {
                    if (!((FragmentElement) element).getId().equals(newFragmentId)) {
                        hasValidationError = hasValidationError || element.getHasValidationError();
                    }
                } else {
                    hasValidationError = hasValidationError || pageHasValidationErrorVisitor.visit((Component) element);
                }
            }
        }
        return hasValidationError;
    }

    private void fillWithUsedBy(Fragment fragment) {
        fillWithUsedBy(List.of(fragment));
    }

    private void fillWithUsedBy(List<Fragment> fragments) {
        List<String> fragmentIds = new ArrayList<>();
        for (Fragment fragment : fragments) {
            fragmentIds.add(fragment.getId());
        }

        for (var repo : usedByRepositories) {
            var map = repo.findByObjectIds(fragmentIds);
            for (Fragment fragment : fragments) {
                fragment.addUsedBy(repo.getComponentName(), (List<Identifiable>) map.get(fragment.getId()));
            }
        }
    }

    @Override
    public Set<Asset> listAsset(Fragment fragment) {
        return assetVisitor.visit(fragment);
    }

    @Override
    public List<WebResource> detectAutoWebResources(Fragment fragment) {
        var resources = this.webResourcesVisitor.visit(fragment);
        return resources.values().stream().collect(toList());
    }

}
