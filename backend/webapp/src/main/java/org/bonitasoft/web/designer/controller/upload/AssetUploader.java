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
package org.bonitasoft.web.designer.controller.upload;

import java.io.IOException;
import java.util.Iterator;

import org.bonitasoft.web.designer.controller.ErrorMessage;
import org.bonitasoft.web.designer.model.WebResourceable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class AssetUploader<T extends WebResourceable> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetUploader.class);

    private Repository<T> repository;
    private AssetRepository<T> assetRepository;

    public AssetUploader(Repository<T> repository, AssetRepository<T> assetRepository) {
        this.repository = repository;
        this.assetRepository = assetRepository;
    }

    public ErrorMessage upload(MultipartFile file, String id, String type) {

        if (file == null || file.isEmpty()) {
            return new ErrorMessage("Argument", "Part named [file] is needed to successfully import a component");
        }

        AssetType assetType = AssetType.valueOf(type);
        if (assetType == null) {
            return new ErrorMessage("Argument", "The type is invalid");
        }

        T component = repository.get(id);

        Asset<T> asset = new Asset<>();
        asset.setName(file.getName());
        asset.setType(assetType);
        asset.setComponent(component);

        for (Iterator<Asset<T>> webResourceIterator = component.getAssets().iterator(); webResourceIterator.hasNext(); ) {
            Asset<T> existingAsset = webResourceIterator.next();

            //If the resource exist we delete the file before save the new one
            if (asset.equals(existingAsset)) {
                try {
                    assetRepository.delete(asset);
                } catch (NotFoundException e) {
                    logger.warn(String.format("Asset to delete %s was not found", asset.getName()), e);
                } catch (IOException e) {
                    return new ErrorMessage("Web resource deletion", String.format("Error while deleting asset in %s [%s]", asset.getName(), repository.getComponentName(), id));
                }
                webResourceIterator.remove();
                break;
            }
        }

        try {
            assetRepository.save(asset, file.getBytes());
            //The component is updated
            component.getAssets().add(asset);
            repository.save(component);

        } catch (IOException e) {
            logger.error("Asset creation" + e);
            return new ErrorMessage("Asset creation", String.format("Error while creating asset in %s [%s]", asset.getName(), repository.getComponentName(), id));
        }

        return null;
    }


}
