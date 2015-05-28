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

        Preconditions.checkArgument(file != null && !file.isEmpty(), "Part named [file] is needed to successfully import a component");
        Preconditions.checkArgument(assetType != null, "The type is invalid");

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
                } catch (NotFoundException e) {
                    logger.warn(String.format("Asset to delete %s was not found", asset.getName()), e);
                } catch (IOException e) {
                    throw new RepositoryException(String.format("Error while deleting asset in %s [%s]", asset.getName(), repository.getComponentName(), component.getId()), e);
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
        Preconditions.checkArgument(isNotEmpty(asset.getName()), "Asset URL is required");
        Preconditions.checkArgument(asset.getType() != null, "Asset type is required");

        if (!component.getAssets().contains(asset)) {
            component.getAssets().add(asset);
        }
        repository.save(component);
    }

    /**
     * Delete an external asset
     */
    public void delete(T component, Asset asset) {
        Preconditions.checkArgument(isNotEmpty(asset.getName()), "Asset URL is required");
        Preconditions.checkArgument(asset.getType() != null, "Asset type is required");

        deleteComponentAsset(component, asset);
        repository.save(component);
    }
}