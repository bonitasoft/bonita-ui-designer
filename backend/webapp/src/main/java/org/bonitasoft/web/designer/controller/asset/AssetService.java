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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.bonitasoft.web.designer.controller.importer.ServerImportException;
import org.bonitasoft.web.designer.controller.importer.dependencies.AssetImporter;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.exists;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.bonitasoft.web.designer.controller.utils.HttpFile.getOriginalFilename;

public class AssetService<T extends Assetable> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    public static final String ASSET_TYPE_IS_REQUIRED = "Asset type is required";
    public static final String ASSET_URL_IS_REQUIRED = "Asset URL is required";
    public static final String ASSET_ID_IS_REQUIRED = "Asset id is required";

    public enum OrderType {
        INCREMENT, DECREMENT
    }

    private Repository<T> repository;
    private AssetRepository<T> assetRepository;
    private AssetImporter<T> assetImporter;
    private JacksonObjectMapper mapper;

    public AssetService(Repository<T> repository, AssetRepository<T> assetRepository, AssetImporter<T> assetImporter, JacksonObjectMapper mapper) {
        this.repository = repository;
        this.assetRepository = assetRepository;
        this.assetImporter = assetImporter;
        this.mapper = mapper;
    }

    /**
     * Upload a local asset
     */
    public Asset upload(MultipartFile file, T component, String type) {
        AssetType assetType = AssetType.getAsset(type);

        checkArgument(file != null && !file.isEmpty(), "Part named [file] is needed to successfully import a component");
        checkArgument(assetType != null, ASSET_TYPE_IS_REQUIRED);


        try {

            if (AssetType.JSON.getPrefix().equals(type)) {
                checkWellFormedJson(file.getBytes());
            }

            final Asset asset = new Asset()
                    .setId(randomUUID().toString())
                    .setName(getOriginalFilename(file.getOriginalFilename()))
                    .setType(assetType)
                    .setOrder(getNextOrder(component));

            Asset deletedAsset = deleteComponentAsset(component, new Predicate<Asset>() {

                @Override
                public boolean apply(Asset element) {
                    return asset.equalsWithoutComponentId(element);
                }
            });
            if (deletedAsset != null) {
                asset.setId(deletedAsset.getId());
            }

            assetRepository.save(component.getId(), asset, file.getBytes());
            //The component is updated
            component.addAsset(asset);
            repository.updateLastUpdateAndSave(component);
            return asset;

        } catch (IOException e) {
            logger.error("Asset creation" + e);
            throw new ServerImportException(String.format("Error while uploading asset in %s [%s]", file.getOriginalFilename(), repository.getComponentName()),
                    e);
        }
    }

    public Path findAssetPath(String id, String filename, String type) throws IOException {
        return assetRepository.findAssetPath(id, filename, AssetType.getAsset(type));
    }

    private void checkWellFormedJson(byte[] bytes) throws IOException {
        try {
            mapper.checkValidJson(bytes);
        } catch (JsonProcessingException e) {
            throw new MalformedJsonException(e);
        }
    }

    private Asset deleteComponentAsset(T component, Predicate<Asset> assetPredicate) {
        try {
            Asset existingAsset = Iterables.<Asset>find(component.getAssets(), assetPredicate);
            if (!existingAsset.isExternal()) {
                try {
                    existingAsset.setComponentId(component.getId());
                    assetRepository.delete(existingAsset);
                } catch (NotFoundException | IOException e) {
                    logger.warn(String.format("Asset to delete %s was not found", existingAsset.getName()), e);
                }
            }
            component.getAssets().remove(existingAsset);
            return existingAsset;
        } catch (NoSuchElementException e) {
            //For a creation component does not contain the asset
        }
        return null;
    }

    /**
     * Save an external asset
     */
    public Asset save(T component, final Asset asset) {
        checkArgument(isNotEmpty(asset.getName()), ASSET_URL_IS_REQUIRED);
        checkArgument(asset.getType() != null, ASSET_TYPE_IS_REQUIRED);

        if (asset.getId() != null) {
            //We find the existing asset and change the name and the type
            Iterables.<Asset>find(component.getAssets(), new Predicate<Asset>() {


                @Override
                public boolean apply(Asset element) {
                    return asset.getId().equals(element.getId());
                }
            }).setName(asset.getName()).setType(asset.getType()).setActive(asset.isActive());
        } else {
            asset.setId(randomUUID().toString());
            asset.setOrder(getNextOrder(component));
            component.getAssets().add(asset);
        }
        repository.updateLastUpdateAndSave(component);
        return asset;
    }

    /**
     * Duplicate assets when an artifact is duplicated
     */
    public void duplicateAsset(Path artifactSourcePath, Path artifactTargetPath, String sourceArtifactId, String targetArtifactId) {
        checkArgument(isNotEmpty(sourceArtifactId), String.format("source %s id is required", repository.getComponentName()));
        checkArgument(isNotEmpty(targetArtifactId), String.format("target %s id is required", repository.getComponentName()));
        checkArgument(artifactSourcePath != null && exists(artifactSourcePath), String.format("source %s path is required", repository.getComponentName()));
        checkArgument(artifactTargetPath != null && exists(artifactTargetPath), String.format("target %s path is required", repository.getComponentName()));

        try {
            List<Asset> assets = assetImporter.load(repository.get(sourceArtifactId), artifactSourcePath);
            for (Asset asset : assets) {
                asset.setScope(null);
                asset.setComponentId(targetArtifactId);
            }
            assetImporter.save(assets, artifactTargetPath);
        } catch (IOException e) {
            throw new RepositoryException("Error on assets duplication", e);
        }
    }

    /**
     * Delete an external asset
     */
    public void delete(T component, final String assetId) {
        checkArgument(isNotEmpty(assetId), ASSET_ID_IS_REQUIRED);

        deleteComponentAsset(component, new Predicate<Asset>() {

            @Override
            public boolean apply(Asset asset) {
                return assetId.equals(asset.getId());
            }
        });
        repository.updateLastUpdateAndSave(component);
    }

    /**
     * Return the max order of the assets included in component
     */
    private int getNextOrder(T component) {
        int order = 0;
        for (Asset asset : (Set<Asset>) component.getAssets()) {
            order = asset.getOrder() > order ? asset.getOrder() : order;
        }
        return order + 1;
    }

    /**
     * Uses to change assset order in the component
     */
    public Asset changeAssetOrderInComponent(T component, String assetId, OrderType ordering) {
        checkArgument(isNotEmpty(assetId), ASSET_ID_IS_REQUIRED);

        //In need an ordered list
        List<Asset> assets = Ordering.from(Asset.getComparatorByOrder()).sortedCopy(component.getAssets());

        Asset previous = null;
        Asset actual = null;
        int i = 0;
        int size = assets.size();

        for (Asset a : assets) {
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
            Previewable previewable = (Previewable) component;

            if (previewable.getInactiveAssets().contains(assetId) && active) {
                previewable.getInactiveAssets().remove(assetId);
                repository.updateLastUpdateAndSave(component);
            } else if (!previewable.getInactiveAssets().contains(assetId) && !active) {
                previewable.getInactiveAssets().add(assetId);
                repository.updateLastUpdateAndSave(component);
            }
        }
    }

    public void loadDefaultAssets(T content) {
        assetRepository.refreshAssets(content);
    }
}
