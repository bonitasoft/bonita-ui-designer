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
import java.util.List;
import java.util.Set;

import com.google.common.collect.Ordering;
import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;


public class AssetService<T extends Assetable> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetService.class);
    public static final String ASSET_TYPE_IS_REQUIRED = "Asset type is required";
    public static final String ASSET_URL_IS_REQUIRED = "Asset URL is required";

    public enum OrderType {INCREMENT, DECREMENT}

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
                .setName(HttpFile.getOriginalFilename(file.getOriginalFilename()))
                .setComponentId(component.getId())
                .setScope(component instanceof Widget ? AssetScope.WIDGET : AssetScope.PAGE)
                .setType(assetType)
                .setOrder(getNextOrder(component));

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
            if (asset.equalsWithoutComponentId(existingAsset) ) {
                if(!asset.isExternal()){
                    try {
                        assetRepository.delete(asset);
                    } catch (NotFoundException | IOException e) {
                        logger.warn(String.format("Asset to delete %s was not found", asset.getName()), e);
                    }
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
            asset.setOrder(getNextOrder(component));
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
    public Asset changeAssetOrderInComponent(Asset asset, OrderType ordering) {
        checkArgument(isNotEmpty(asset.getName()), ASSET_URL_IS_REQUIRED);
        checkArgument(asset.getType() != null, ASSET_TYPE_IS_REQUIRED);
        checkArgument(asset.getComponentId() != null, "component id is required");

        T component = repository.get(asset.getComponentId());

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
            if (asset.equalsWithoutComponentId(a)) {
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
        repository.save(component);
        return actual;
    }

}