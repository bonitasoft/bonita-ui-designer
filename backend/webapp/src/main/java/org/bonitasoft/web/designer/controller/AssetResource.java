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
package org.bonitasoft.web.designer.controller;

import static java.lang.Boolean.TRUE;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Preconditions;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.utils.HttpFile;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class AssetResource<T extends Assetable> {

    protected static final Logger logger = LoggerFactory.getLogger(AssetResource.class);

    protected AssetService<T> assetService;
    protected AssetVisitor assetVisitor;
    protected Repository<T> repository;

    public AssetResource(AssetService<T> assetService, Repository<T> repository, AssetVisitor assetVisitor) {
        this.assetService = assetService;
        this.assetVisitor = assetVisitor;
        this.repository = repository;
    }

    protected abstract void checkArtifactId(String artifactId);

    // produces = MediaType.TEXT_PLAIN_VALUE to avoid some internet explorer issues
    @RequestMapping(value = "/{artifactId}/assets/{type}", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Asset> saveOrUpdate(@RequestParam("file") MultipartFile file, @PathVariable("artifactId") String id, @PathVariable("type") String type) {
        checkArtifactId(id);
        Asset asset = assetService.upload(file, repository.get(id), type);
        return new ResponseEntity<>(asset, HttpStatus.CREATED);
    }

    @RequestMapping("/{artifactId}/assets/{type}/{filename:.*}")
    public void downloadAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("artifactId") String id,
            @PathVariable("type") String type,
            @PathVariable("filename") String filename,
            @RequestParam(value = "format", required = false) String format) throws IOException {

            Path filePath = assetService.findAssetPath(id, filename, type);

            if("text".equals(format)){
                HttpFile.writeFileInResponseForVisualization(request, response, filePath);
            }else {
                HttpFile.writeFileInResponseForDownload(response, filePath);
            }

    }

    @RequestMapping(value = "/{artifactId}/assets", method = RequestMethod.POST)
    public Asset saveAsset(@RequestBody Asset asset, @PathVariable("artifactId") String id) {
        checkArtifactId(id);
        return assetService.save(repository.get(id), asset);
    }

    @RequestMapping(value = "/{artifactId}/assets/{assetId}", method = RequestMethod.DELETE)
    public void deleteAsset(@PathVariable("artifactId") String id, @PathVariable("assetId") String assetId) throws RepositoryException {
        checkArtifactId(id);
        assetService.delete(repository.get(id), assetId);
    }

    @RequestMapping(value = "/{artifactId}/assets")
    @JsonView(Asset.JsonViewAsset.class)
    public <U extends Previewable & Identifiable> Set<Asset> assets(@PathVariable("artifactId") String id) {
        Preconditions.checkNotNull(assetVisitor, "Not available for widgets");
        return assetVisitor.visit((U) repository.get(id));
    }

    @RequestMapping(value = "/{artifactId}/assets/{assetId}", method = RequestMethod.PUT)
    public void updateAsset(
            @PathVariable("artifactId") String id,
            @PathVariable("assetId") String assetId,
            @RequestParam(value = "increment", required = false) Boolean increment,
            @RequestParam(value = "decrement", required = false) Boolean decrement,
            @RequestParam(value = "active", required = false) Boolean active) {
        checkArtifactId(id);
        if (increment != null || decrement != null) {
            assetService.changeAssetOrderInComponent(repository.get(id), assetId, TRUE.equals(increment) ? INCREMENT : DECREMENT);
        }
        if (active != null) {
            assetService.changeAssetStateInPreviewable(repository.get(id), assetId, active);
        }
    }
}
