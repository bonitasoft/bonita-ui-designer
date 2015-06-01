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

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


public class AssetService<T extends Assetable> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    public static final String ASSET_TYPE_IS_REQUIRED = "Asset type is required";
    public static final String ASSET_URL_IS_REQUIRED = "Asset URL is required";

    private Repository<T> repository;
    private AssetRepository<T> assetRepository;


    public AssetService(Repository<T> repository, AssetRepository<T> assetRepository) {
        this.repository = repository;
        this.assetRepository = assetRepository;
    }

    /**
     * Upload a local asset
     */
    public void upload(MultipartFile file, T component, String type) {
        AssetType assetType = AssetType.getAsset(type);

        checkArgument(file != null && !file.isEmpty(), "Part named [file] is needed to successfully import a component");
        checkArgument(assetType != null, ASSET_TYPE_IS_REQUIRED);

        Asset asset = new Asset()
                .setName(file.getOriginalFilename())
                .setComponentId(component.getId())
                .setScope(component instanceof Widget ? AssetScope.WIDGET : AssetScope.PAGE)
                .setType(assetType);

        deleteComponentAsset(component, asset);

        try {
            assetRepository.save(asset, file.getBytes());
            //The component is updated
            component.getAssets().add(asset);
            repository.save(component);

        } catch (IOException e) {
            logger.error("Asset creation" + e);
            throw new ServerImportException(String.format("Error while uploading asset in %s [%s]", file.getOriginalFilename(), repository.getComponentName(), component.getId()), e);
        }
    }

    private void deleteComponentAsset(T component, Asset asset) {
        for (Iterator<Asset> assetIterator = component.getAssets().iterator(); assetIterator.hasNext(); ) {
            Asset existingAsset = assetIterator.next();

            //If the resource exist we delete the file before save the new one
            if (asset.equals(existingAsset)) {
                try {
                    assetRepository.delete(asset);
                } catch (NotFoundException | IOException e) {
                    logger.warn(String.format("Asset to delete %s was not found", asset.getName()), e);
                }
                assetIterator.remove();
                break;
            }
        }
    }

    /**
     * Save an external asset
     */
    public void save(T component, Asset asset) {
        checkArgument(isNotEmpty(asset.getName()), ASSET_URL_IS_REQUIRED);
        checkArgument(asset.getType() != null, ASSET_TYPE_IS_REQUIRED);

        if (!component.getAssets().contains(asset)) {
            component.getAssets().add(asset);
        }
        repository.save(component);
    }

    /**
     * Delete an external asset
     */
    public void delete(T component, Asset asset) {
        checkArgument(isNotEmpty(asset.getName()), ASSET_URL_IS_REQUIRED);
        checkArgument(asset.getType() != null, ASSET_TYPE_IS_REQUIRED);

        deleteComponentAsset(component, asset);
        repository.save(component);
    }
}