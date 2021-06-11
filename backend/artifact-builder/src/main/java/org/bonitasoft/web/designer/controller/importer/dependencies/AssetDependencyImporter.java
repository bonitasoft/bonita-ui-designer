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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;

public class AssetDependencyImporter<T extends Identifiable & Assetable> implements DependencyImporter<Asset> {

    public static final String ASSETS_FOLDER_NAME = "assets";

    private final AssetRepository<T> assetRepository;

    public AssetDependencyImporter(AssetRepository<T> assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public List<Asset> load(Identifiable component, Path resources) throws IOException {
        Path assetsPath = component instanceof Widget ? getWidgetAssetsFolderPath(component.getId(), resources) : resources.resolve(ASSETS_FOLDER_NAME);

        if (exists(assetsPath)) {
            List<Asset> assets = new ArrayList<>();
            for (AssetType type : AssetType.values()) {
                var path = assetsPath.resolve(type.getPrefix());
                if (exists(path)) {
                    assets.addAll(assetRepository.findAssetInPath((T) component, type, path));
                }
            }
            return assets;
        }

        return new ArrayList<>();
    }

    /**
     * Return assets path, could be componentId/assets or only assets/
     */
    private Path getWidgetAssetsFolderPath(String componentId, Path resources) {
        var assetsPath =  resources.resolve(componentId).resolve(ASSETS_FOLDER_NAME);
        return Files.exists(assetsPath) ? assetsPath : resources.resolve(ASSETS_FOLDER_NAME);
    }

    @Override
    public void save(List<Asset> elements, Path resources) {
        for (Asset asset : elements) {
            Path assetsPath = AssetScope.WIDGET.equals(asset.getScope()) ? getWidgetAssetsFolderPath(asset.getComponentId(), resources) : resources.resolve("assets");
            Path sourceFile = assetsPath.resolve(asset.getType().getPrefix()).resolve(asset.getName());
            try {
                assetRepository.save(asset, readAllBytes(sourceFile));
            } catch (IOException e) {
                throw new RepositoryException(String.format("Impossible to save the source file [%s] as new %s asset ", sourceFile.getFileName().toString(), asset.getType()), e);
            }
        }
    }
}
