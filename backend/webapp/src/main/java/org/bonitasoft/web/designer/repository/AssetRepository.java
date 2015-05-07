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

import static java.lang.String.format;
import static java.nio.file.Files.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;

/**
 * This Persister is used to attach assets to a component. Each of them are serialized in the same directory
 */
public class AssetRepository<T extends Identifiable> {

    private Repository<T> repository;
    private BeanValidator validator;

    @Inject
    public AssetRepository(Repository<T> repository, BeanValidator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    protected Path resolveAssetPath(Asset asset) {
        validator.validate(asset);
        validator.validate(asset.getComponent());
        return repository.resolvePathFolder((T) asset.getComponent()).resolve(asset.getName());
    }

    protected Path resolveExistingAssetPath(Asset asset) {
        if (!exists(resolveAssetPath(asset))) {
            throw new NotFoundException(format("Error when searching asset %s for %s [%s]: asset not found.",
                    asset.getName(), repository.getComponentName(), asset.getComponent().getId()));
        }
        return resolveAssetPath(asset);
    }

    /**
     * Add a file resource to a component
     */
    public void save(Asset asset, byte[] content) throws IOException {
        write(resolveAssetPath(asset), content);
    }

    /**
     * Remove a component resource
     */
    public void delete(Asset asset) throws IOException {
        Files.delete(resolveExistingAssetPath(asset));
    }

    /**
     * Get resource content
     */
    public byte[] getResourceStream(Asset asset) throws IOException {
        return readAllBytes(resolveExistingAssetPath(asset));
    }

}
