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
package org.bonitasoft.web.designer.controller.asset;

import org.bonitasoft.web.designer.controller.importer.dependencies.AssetDependencyImporter;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class AssetService<T extends Assetable> {

    public static final String ASSET_TYPE_IS_REQUIRED = "Asset type is required";

    public static final String ASSET_URL_IS_REQUIRED = "Asset URL is required";

    public static final String ASSET_ID_IS_REQUIRED = "Asset id is required";

    protected static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    private final Repository<T> repository;

    private final AssetRepository<T> assetRepository;

    private final AssetDependencyImporter<T> assetDependencyImporter;

    public AssetService(Repository<T> repository, AssetRepository<T> assetRepository, AssetDependencyImporter<T> assetDependencyImporter) {
        this.repository = repository;
        this.assetRepository = assetRepository;
        this.assetDependencyImporter = assetDependencyImporter;
    }

    public Path findAssetPath(String id, String filename, String type) {
        return assetRepository.findAssetPath(id, filename, AssetType.getAsset(type));
    }

    private void deleteComponentAsset(T component, Predicate<Asset> assetPredicate) {
        component.getAssets().stream().filter(assetPredicate).findFirst().ifPresent(existingAsset -> {
            if (!existingAsset.isExternal()) {
                try {
                    existingAsset.setComponentId(component.getId());
                    assetRepository.delete(existingAsset);
                } catch (NotFoundException | IOException e) {
                    logger.warn("Asset to delete {} was not found", existingAsset.getName(), e);
                }
            }
            component.getAssets().remove(existingAsset);
        });
    }

    /**
     * Save an external asset
     */
    public Asset save(T component, final Asset asset) {
        if (isEmpty(asset.getName())) {
            throw new IllegalArgumentException(ASSET_URL_IS_REQUIRED);
        }
        if (asset.getType() == null) {
            throw new IllegalArgumentException(ASSET_TYPE_IS_REQUIRED);
        }

        if (asset.getId() != null) {
            //We find the existing asset and change the name and the type
            component.getAssets().stream()
                    .filter(element -> asset.getId().equals(element.getId())).findFirst()
                    .ifPresent(
                            element -> {
                                element.setName(asset.getName());
                                element.setType(asset.getType());
                                element.setActive(asset.isActive());
                            });
        } else {
            asset.setId(randomUUID().toString());
            asset.setOrder(component.getNextAssetOrder());
            component.getAssets().add(asset);
        }
        repository.updateLastUpdateAndSave(component);
        return asset;
    }

    /**
     * Save an internal asset
     */
    public Asset save(T component, Asset asset, byte[] content) {
        try {
            assetRepository.save(component.getId(), asset, content);
            return save(component, asset);
        } catch (IOException e) {
            throw new RepositoryException("Error while saving internal asset", e);
        }
    }

    public String getAssetContent(T component, Asset asset) throws IOException {
        return new String(assetRepository.readAllBytes(component.getId(), asset), StandardCharsets.UTF_8);
    }

    /**
     * Duplicate assets when an artifact is duplicated
     */
    public void duplicateAsset(Path artifactSourcePath, Path artifactTargetPath, String sourceArtifactId, String targetArtifactId) {
        checkArgument(isNotEmpty(sourceArtifactId), format("source %s id is required", repository.getComponentName()));
        checkArgument(isNotEmpty(targetArtifactId), format("target %s id is required", repository.getComponentName()));
        checkArgument(artifactSourcePath != null && exists(artifactSourcePath), format("source %s path is required", repository.getComponentName()));
        checkArgument(artifactTargetPath != null && exists(artifactTargetPath), format("target %s path is required", repository.getComponentName()));

        try {
            List<Asset> assets = assetDependencyImporter.load(repository.get(sourceArtifactId), artifactSourcePath);
            for (Asset asset : assets) {
                asset.setScope(null);
                asset.setComponentId(targetArtifactId);
            }
            assetDependencyImporter.save(assets, artifactTargetPath);
        } catch (IOException e) {
            throw new RepositoryException("Error on assets duplication", e);
        }
    }

    /**
     * Delete an external asset
     */
    public void delete(T component, final String assetId) {
        checkArgument(isNotEmpty(assetId), ASSET_ID_IS_REQUIRED);

        // remove asset from inactive asset list
        if(component instanceof Previewable && ((Previewable) component).getInactiveAssets().contains(assetId)){
            ((Previewable) component).getInactiveAssets().remove(assetId);
        }

        deleteComponentAsset(component, asset -> assetId.equals(asset.getId()));
        repository.updateLastUpdateAndSave(component);
    }

    /**
     * Uses to change assset order in the component
     */
    public Asset changeAssetOrderInComponent(T component, String assetId, OrderType ordering) {
        checkArgument(isNotEmpty(assetId), ASSET_ID_IS_REQUIRED);

        //In need an ordered list
        var assets = component.getAssets().stream().sorted(Asset.getComparatorByOrder()).collect(toList());

        Asset previous = null;
        Asset actual = null;
        var i = 0;
        var size = assets.size();

        for (var a : assets) {
            if (actual != null) {
                //We have to break the loop
                if (OrderType.INCREMENT.equals(ordering)) {
                    a.setOrder(a.getOrder() - 1);
                }
                break;
            }
            if (assetId.equals(a.getId())) {
                //If asset is found we change order
                actual = a;
                //If elt is the first we can't decremented it. This is the same if we want to increment the last one
                if ((OrderType.DECREMENT.equals(ordering) && previous == null) ||
                        (OrderType.INCREMENT.equals(ordering) && i == size - 1)) {
                    //If elt is the first or the last it can't be decremented or incremented
                    break;
                }
                a.setOrder(OrderType.DECREMENT.equals(ordering) ? a.getOrder() - 1 : a.getOrder() + 1);
                //If current asset is placed before we change the previous asset
                if (previous != null && OrderType.DECREMENT.equals(ordering)) {
                    previous.setOrder(previous.getOrder() + 1);
                }
            } else {
                previous = a;
            }
            i++;
        }
        repository.updateLastUpdateAndSave(component);
        return actual;
    }

    /**
     * Changes asset state (active/inactive) in prewiable
     */
    public void changeAssetStateInPreviewable(T component, String assetId, boolean active) {
        checkArgument(isNotEmpty(assetId), ASSET_ID_IS_REQUIRED);

        if (component instanceof Previewable) {
            var previewable = (Previewable) component;

            if (previewable.getInactiveAssets().contains(assetId) && active) {
                previewable.getInactiveAssets().remove(assetId);
                repository.updateLastUpdateAndSave(component);
            } else if (!previewable.getInactiveAssets().contains(assetId) && !active) {
                previewable.getInactiveAssets().add(assetId);
                repository.updateLastUpdateAndSave(component);
            }
        }
    }

    private void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void loadDefaultAssets(T content) {
        assetRepository.refreshAssets(content);
    }

    public enum OrderType {
        INCREMENT, DECREMENT
    }
}
