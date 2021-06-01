package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.AssetService.OrderType;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.repository.Repository;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractAssetableArtifactService<R extends Repository<T>, T extends Identifiable & Assetable> extends AbstractArtifactService<R, T> implements AssetableArtifactService<T> {

    protected AssetService<T> assetService;

    protected AbstractAssetableArtifactService(UiDesignerProperties uiDesignerProperties, AssetService<T> assetService, R repository) {
        super(uiDesignerProperties, repository);
        this.assetService = assetService;
    }

    @Override
    public Asset saveAsset(String id, Asset asset) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        return assetService.save(identifiable, asset);
    }

    @Override
    public Asset saveOrUpdateAsset(String id, AssetType assetType, String fileName, byte[] fileContent) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        var asset = new Asset()
                .setName(fileName)
                .setType(assetType)
                .setOrder(identifiable.getNextAssetOrder());

        identifiable.getAssets().stream()
                .filter(asset::equalsWithoutComponentId).findFirst()
                .ifPresent(existingAsset -> asset.setId(existingAsset.getId()));

        return assetService.save(identifiable, asset, fileContent);
    }

    @Override
    public Asset changeAssetOrder(String id, String assetId, OrderType orderType) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        return assetService.changeAssetOrderInComponent(identifiable, assetId, orderType);
    }

    @Override
    public void changeAssetStateInPreviewable(String id, String assetId, boolean active) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        assetService.changeAssetStateInPreviewable(identifiable, assetId, active);
    }

    @Override
    public void deleteAsset(String id, String assetId) {
        checkUpdatable(id);
        var identifiable = this.get(id);
        assetService.delete(identifiable, assetId);
    }

    @Override
    public Path findAssetPath(String id, String filename, String type) throws IOException {
        return assetService.findAssetPath(id, filename, type);
    }

    protected void checkUpdatable(String id) {
        checkNotNull(id);
    }
}
