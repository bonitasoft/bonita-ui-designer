package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.asset.AssetService.OrderType;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;

import java.io.IOException;
import java.nio.file.Path;

public interface AssetableArtifactService<T extends Identifiable & Assetable> extends ArtifactService<T> {

    Asset saveAsset(String id, Asset asset);

    Asset saveOrUpdateAsset(String id, AssetType assetType, String fileName, byte[] fileContent);

    Asset changeAssetOrder(String id, String assetId, OrderType orderType);

    void changeAssetStateInPreviewable(String id, String assetId, boolean active);

    void deleteAsset(String id, String assetId);

    Path findAssetPath(String id, String filename, String type) throws IOException;

}
