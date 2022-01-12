package org.bonitasoft.web.designer.rendering;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.visitor.AssetVisitor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByComponentId;
import static org.bonitasoft.web.designer.model.asset.Asset.getComparatorByOrder;

@RequiredArgsConstructor
@Slf4j
public class AssetHtmlBuilder {

    private final AssetVisitor assetVisitor;
    private final AssetRepository<Page> pageAssetRepository;
    private final AssetRepository<Widget> widgetAssetRepository;
    private final WorkspaceProperties workspaceProperties;

    /**
     * Return the list of the previewable assets sorted with only active assets
     */
    public <P extends Previewable & Identifiable> List<Asset> getSortedAssets(P previewable) {
        return assetVisitor.visit(previewable).stream().filter(Asset::isActive)
                .sorted(getComparatorByComponentId().thenComparing(getComparatorByOrder())
                ).collect(Collectors.toList());
    }

    public List<String> getAssetHtmlSrcList(String previewableId,AssetType assetType, List<Asset> sortedAssets) {
        var assetsSrc = new ArrayList<String>();
        sortedAssets.stream()
                .filter(asset -> assetType.equals(asset.getType()))
                .forEach(asset -> {
                    var widgetPrefix = "";
                    if (asset.isExternal()) {
                        assetsSrc.add(asset.getName());
                    } else {
                        String assetHash;
                        if (AssetScope.WIDGET.equals(asset.getScope())) {
                            widgetPrefix = String.format("widgets/%s/", asset.getComponentId());
                            assetHash = getHash(asset, widgetAssetRepository, previewableId);
                        } else {
                            assetHash = getHash(asset, pageAssetRepository, previewableId);
                        }
                        if (!assetsSrc.contains(asset.getName())) {
                            assetsSrc.add(String.format("%sassets/%s/%s?hash=%s", widgetPrefix, asset.getType().getPrefix(), asset.getName(), assetHash));
                        }
                    }
                });
        return assetsSrc;
    }

    // For now, only list artefact asset
    public List<String> getAssetAngularSrcList(String previewableId,AssetType assetType, List<Asset> sortedAssets) {
        var assetsSrc = new ArrayList<String>();
        sortedAssets.stream()
                .filter(asset -> assetType.equals(asset.getType()))
                .forEach(asset -> {
                    if (asset.isExternal()) {
                        assetsSrc.add(asset.getName());
                    } else {
                        if (!AssetScope.WIDGET.equals(asset.getScope()) && !assetsSrc.contains(asset.getName())) {
                            assetsSrc.add(getAssetPath(previewableId,asset));
                        }
                    }
                });
        return assetsSrc;
    }

    private String getAssetPath(String previewableId, Asset asset){
        return Paths.get(workspaceProperties.getPages().getDir().resolve(previewableId).toUri())
                .resolve("assets")
                .resolve(asset.getType().getPrefix())
                .resolve(asset.getName()).toString();
    }

    public String getHash(Asset asset, AssetRepository<?> assetRepository, String previewableId) {
        try {
            var content = asset.getComponentId() == null ? assetRepository.readAllBytes(previewableId, asset) : assetRepository.readAllBytes(asset);
            return DigestUtils.sha1Hex(content);
        } catch (Exception e) {
            log.warn("Failure to generate hash for asset {}", asset.getName(), e);
            return UUID.randomUUID().toString();
        }
    }
}
