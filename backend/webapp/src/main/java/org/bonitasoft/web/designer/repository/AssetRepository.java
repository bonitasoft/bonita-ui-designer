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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.write;
import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;

/**
 * This Persister is used to attach assets to a component. Each of them are serialized in the same directory
 */
public class AssetRepository<T extends Identifiable & Assetable> {

    public static final String COMPONENT_ID_REQUIRED = "The component id is required to add an asset to this component";
    private Repository<T> repository;
    private BeanValidator validator;

    @Inject
    public AssetRepository(Repository<T> repository, BeanValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    protected Path resolveComponentPath(Asset asset) {
        return repository.resolvePathFolder(asset.getComponentId());
    }

    protected Path resolveAssetPath(Asset asset) {
        validator.validate(asset);
        return resolveComponentPath(asset).resolve("assets").resolve(asset.getType().getPrefix()).resolve(asset.getName());
    }

    protected Path resolveExistingAssetPath(Asset asset) {
        Path path = resolveAssetPath(asset);
        if (!exists(path)) {
            throw new NotFoundException(format("Error when searching asset %s for %s [%s]: asset not found.",
                    asset.getName(), repository.getComponentName(), asset.getComponentId()));
        }
        return path;
    }

    /**
     * Add a file asset to a component
     */
    public void save(Asset asset, byte[] content) throws IOException {
        checkNotNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        Path parent = resolveComponentPath(asset);
        if (!exists(parent.resolve("assets").resolve(asset.getType().getPrefix()))) {
            createDirectories(parent.resolve("assets").resolve(asset.getType().getPrefix()));
        }
        write(resolveAssetPath(asset), content);
    }

    /**
     * Add a file asset to a component
     */
    public void save(String componentId, Asset asset, byte[] content) throws IOException {
        Path assetDirectory = resolveAssetDirectory(componentId, asset);
        if (!exists(assetDirectory)) {
            createDirectories(assetDirectory);
        }
        write(assetDirectory.resolve(asset.getName()), content);
    }


    private Path resolveAssetDirectory(String componentId, Asset asset) {
        return repository.resolvePathFolder(componentId).resolve("assets").resolve(asset.getType().getPrefix());
    }

    /**
     * Remove an asset
     */
    public void delete(Asset asset) throws IOException {
        checkNotNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        Files.delete(resolveExistingAssetPath(asset));
    }

    /**
     * Read resource content
     */
    public byte[] readAllBytes(Asset asset) throws IOException {
        checkNotNull(asset.getComponentId(), COMPONENT_ID_REQUIRED);
        return Files.readAllBytes(resolveExistingAssetPath(asset));
    }

    /**
     * Return the asset path used by a component
     *
     * @throws org.bonitasoft.web.designer.repository.exception.NotFoundException when component not exists
     */
    public Path findAssetPath(String componentId, final String filename, final AssetType assetType) throws IOException {
        checkNotNull(filename, "Filename is required");
        checkNotNull(assetType, format("Asset type is required (filename: %s)", filename));

        T component = repository.get(componentId);
        Asset asset = (Asset) find(component.getAssets(), new Predicate<Asset>() {
            @Override
            public boolean apply(Asset asset) {
                return filename.equals(asset.getName()) && assetType.equals(asset.getType());
            }
        });
        asset.setComponentId(componentId);
        if (asset.isExternal()) {
            throw new NotAllowedException("We can't load an external asset. Use the link " + asset.getName());
        }
        return resolveExistingAssetPath(asset);
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
     *  {@linkplain AssetRepository<T>.refreshAssets} need to not set componentId for asset otherwise it makes page creation failing
     *  AssetImporter need it
     *
     *  // TODO : to be refactored
     */
    public List<Asset> findAssetInPathWhithoutComponentId(T component, AssetType type, Path directory) throws IOException {
        List<Asset> objects = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path path : directoryStream) {
                objects.add(new Asset()
                        .setName(path.getFileName().toString())
                        .setType(type)
                        .setScope(AssetScope.forComponent(component))
                        .setComponentId(component instanceof Widget ? component.getId() : null)
                        .setId(UUID.randomUUID().toString()));
            }
        }

        return objects;
    }

    public void refreshAssets(final T component) {

        component.addAssets(newHashSet(concat(transform(newArrayList(AssetType.values()), new Function<AssetType, List<Asset>>() {

            @Override
            public List<Asset> apply(AssetType type) {
                try {
                    Path assetTypePath = repository.resolvePath(component.getId()).resolve(Paths.get("assets", type.getPrefix()));
                    if (exists(assetTypePath)) {
                        return findAssetInPathWhithoutComponentId(component, type, assetTypePath);
                    }
                } catch (IOException e) {
                    throw new RepositoryException(format("Failed to initialized assets for %s %s", repository.getComponentName(), component.getId()), e);
                }
                return emptyList();
            }
        }))));
        repository.updateLastUpdateAndSave(component);
    }
}
