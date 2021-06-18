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
package org.bonitasoft.web.designer.repository;

import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

/**
 * This Persister is used to attach assets to a component. Each of them are serialized in the same directory
 */
public class AssetRepository<T extends Identifiable & Assetable> {

    public static final String COMPONENT_ID_REQUIRED = "The component id is required to add an asset to this component";
    public static final String ASSETS = "assets";

    private final Repository<T> repository;
    private final BeanValidator validator;

    public AssetRepository(Repository<T> repository, BeanValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    protected Path resolveComponentPath(String componentId) {
        return repository.resolvePathFolder(componentId);
    }

    protected Path resolveAssetPath(String componentId, Asset asset) {
        validator.validate(asset);
        return resolveComponentPath(componentId).resolve(ASSETS).resolve(asset.getType().getPrefix()).resolve(asset.getName());
    }

    protected Path resolveAssetPath(Asset asset) {
        return resolveAssetPath(asset.getComponentId(), asset);
    }

    protected Path resolveExistingAssetPath(Asset asset) {
        return resolveExistingAssetPath(asset.getComponentId(), asset);
    }

    protected Path resolveExistingAssetPath(String componentId, Asset asset) {
        var path = resolveAssetPath(componentId, asset);
        if (!exists(path)) {
            throw new NotFoundException(format("Error when searching asset %s for %s [%s]: asset not found in path '%s'",
                    asset.getName(), repository.getComponentName(), componentId, path));
        }
        return path;
    }

    /**
     * Add a file asset to a component
     */
    public void save(Asset asset, byte[] content) throws IOException {
        requireNonNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        var parent = resolveComponentPath(asset.getComponentId());
        if (!exists(parent.resolve(ASSETS).resolve(asset.getType().getPrefix()))) {
            createDirectories(parent.resolve(ASSETS).resolve(asset.getType().getPrefix()));
        }
        write(resolveAssetPath(asset), content);
    }

    /**
     * Add a file asset to a component
     */
    public void save(String componentId, Asset asset, byte[] content) throws IOException {
        var assetDirectory = resolveAssetDirectory(componentId, asset);
        if (!exists(assetDirectory)) {
            createDirectories(assetDirectory);
        }
        write(assetDirectory.resolve(asset.getName()), content);
    }


    private Path resolveAssetDirectory(String componentId, Asset asset) {
        return repository.resolvePathFolder(componentId).resolve(ASSETS).resolve(asset.getType().getPrefix());
    }

    /**
     * Remove an asset
     */
    public void delete(Asset asset) throws IOException {
        requireNonNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        Files.delete(resolveExistingAssetPath(asset));
    }

    /**
     * Read resource content
     */
    public byte[] readAllBytes(Asset asset) throws IOException {
        requireNonNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        return Files.readAllBytes(resolveExistingAssetPath(asset));
    }

    /**
     * Read resource content
     */
    public byte[] readAllBytes(String componentId, Asset asset) throws IOException {
        requireNonNull(componentId, COMPONENT_ID_REQUIRED);
        return Files.readAllBytes(resolveExistingAssetPath(componentId, asset));
    }

    /**
     * Return the asset path used by a component
     *
     * @throws NotFoundException when component not exists
     */
    public Path findAssetPath(String componentId, final String filename, final AssetType assetType) {
        requireNonNull(filename, "Filename is required");
        requireNonNull(assetType, format("Asset type is required (filename: %s)", filename));

        var component = repository.get(componentId);
        var existingAsset = component.getAssets().stream()
                .filter(asset -> filename.equals(asset.getName()) && assetType.equals(asset.getType()))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Asset not found"));
        existingAsset.setComponentId(componentId);
        if (existingAsset.isExternal()) {
            throw new NotAllowedException("We can't load an external asset. Use the link " + existingAsset.getName());
        }
        return resolveExistingAssetPath(existingAsset);
    }

    /**
     * Return the list of assets found in a repository
     */
    public List<Asset> findAssetInPath(T component, AssetType type, Path directory) throws IOException {
        List<Asset> objects = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                objects.add(new Asset()
                        .setName(path.getFileName().toString())
                        .setType(type)
                        .setScope(AssetScope.forComponent(component))
                        .setComponentId(component.getId())
                        .setId(UUID.randomUUID().toString()));
            }
        }

        return objects;
    }

    /**
     * {@linkplain #refreshAssets} need to not set componentId for asset otherwise it makes page creation failing
     * AssetImporter need it
     * <p>
     * // TODO : to be refactored
     */
    public List<Asset> findAssetInPathWhithoutComponentId(T component, AssetType type, Path directory) throws IOException {
        List<Asset> objects = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            var componentId = component instanceof Widget ? component.getId() : null;
            for (Path path : directoryStream) {
                objects.add(new Asset()
                        .setName(path.getFileName().toString())
                        .setType(type)
                        .setScope(AssetScope.forComponent(component))
                        .setComponentId(componentId)
                        .setId(UUID.randomUUID().toString()));
            }
        }

        return objects;
    }

    public void refreshAssets(final T component) {
        var assets = stream(AssetType.values()).map(type -> {
            try {
                var assetTypePath = repository.resolvePath(component.getId()).resolve(Paths.get(ASSETS, type.getPrefix()));
                if (exists(assetTypePath)) {
                    return findAssetInPathWhithoutComponentId(component, type, assetTypePath);
                }
            } catch (IOException e) {
                throw new RepositoryException(format("Failed to initialized assets for %s %s", repository.getComponentName(), component.getId()), e);
            }
            return Collections.<Asset>emptyList();
        }).flatMap(Collection::stream).collect(Collectors.toSet());

        component.addAssets(assets);
        repository.updateLastUpdateAndSave(component);
    }
}
